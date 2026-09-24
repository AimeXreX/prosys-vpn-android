package com.v2ray.ang.handler

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import com.google.gson.JsonObject
import com.v2ray.ang.AppConfig
import com.v2ray.ang.BuildConfig
import com.v2ray.ang.dto.entities.SubscriptionCache
import com.v2ray.ang.dto.entities.SubscriptionItem
import com.v2ray.ang.util.JsonUtil
import com.v2ray.ang.util.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.security.MessageDigest
import java.util.UUID
import java.util.concurrent.TimeUnit

data class FreeAccessState(
    val loading: Boolean = true,
    val ready: Boolean = false,
    val quotaBytes: Long = 512L * 1024 * 1024,
    val usedBytes: Long = 0,
    val remainingBytes: Long = 512L * 1024 * 1024,
    val resetAt: Long = 0,
    val exhausted: Boolean = false,
    val notice: String? = null,
    val error: String? = null
)

object FreeAccessManager {
    const val SUBSCRIPTION_ID = "prosys-managed-free"
    private const val PREFS = "prosys_free_access"
    private const val INSTALLATION_ID = "installation_id"
    private const val DEVICE_TOKEN = "device_token"
    private const val HEADER_NAME = "X-ProSys-Device"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()
    private val _state = MutableStateFlow(FreeAccessState())
    val state: StateFlow<FreeAccessState> = _state.asStateFlow()

    suspend fun initialize(context: Context): Boolean = withContext(Dispatchers.IO) {
        _state.value = _state.value.copy(loading = true, error = null)
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val installationId = stableInstallationId(context, prefs)
        val legacyToken = prefs.getString(DEVICE_TOKEN, null)
        if (!legacyToken.isNullOrBlank() && SecureTokenStore.get(context).isNullOrBlank()) {
            SecureTokenStore.put(context, legacyToken)
            prefs.edit().remove(DEVICE_TOKEN).apply()
        }
        val previousToken = SecureTokenStore.get(context)
        val payload = JsonObject().apply {
            addProperty("installationId", installationId)
            if (!previousToken.isNullOrBlank()) addProperty("deviceToken", previousToken)
        }
        try {
            val request = Request.Builder()
                .url("${AppConfig.PRO_SYS_API_BASE_URL}/api/free/register")
                .post(JsonUtil.toJson(payload).toRequestBody("application/json".toMediaType()))
                .header("User-Agent", "ProSyS-VPN/${BuildConfig.VERSION_NAME}")
                .build()
            val responseText = client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) error("Free access registration failed: ${response.code}")
                response.body.string()
            }
            val result = JsonUtil.parseString(responseText) ?: error("Invalid free access response")
            val token = result.string("deviceToken") ?: error("Missing device token")
            val subscriptionUrl = result.string("subscriptionUrl") ?: error("Missing subscription URL")
            SecureTokenStore.put(context, token)
            applyState(result)
            refreshNotice()
            ensureManagedSubscription(subscriptionUrl, token)
            val update = AngConfigManager.updateConfigViaSub(
                SubscriptionCache(SUBSCRIPTION_ID, MmkvManager.decodeSubscription(SUBSCRIPTION_ID)!!)
            )
            _state.value = _state.value.copy(
                loading = false,
                ready = update.successCount > 0 || MmkvManager.decodeServerList(SUBSCRIPTION_ID).isNotEmpty()
            )
            update.configCount > 0
        } catch (error: Exception) {
            LogUtil.e(AppConfig.TAG, "ProSyS free access initialization failed", error)
            _state.value = _state.value.copy(
                loading = false,
                ready = MmkvManager.decodeServerList(SUBSCRIPTION_ID).isNotEmpty(),
                error = error.message ?: "Free access unavailable"
            )
            false
        }
    }

    suspend fun refreshStatus(context: Context) = withContext(Dispatchers.IO) {
        val token = SecureTokenStore.get(context) ?: return@withContext
        try {
            val request = Request.Builder()
                .url("${AppConfig.PRO_SYS_API_BASE_URL}/api/free/status")
                .get()
                .header(HEADER_NAME, token)
                .header("User-Agent", "ProSyS-VPN/${BuildConfig.VERSION_NAME}")
                .build()
            val responseText = client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) error("Free status failed: ${response.code}")
                response.body.string()
            }
            JsonUtil.parseString(responseText)?.let(::applyState)
            refreshNotice()
        } catch (error: Exception) {
            LogUtil.e(AppConfig.TAG, "ProSyS free status refresh failed", error)
        }
    }

    private fun refreshNotice() {
        runCatching {
            val request = Request.Builder()
                .url("${AppConfig.PRO_SYS_API_BASE_URL}/api/app/notice")
                .get()
                .header("User-Agent", "ProSyS-VPN/${BuildConfig.VERSION_NAME}")
                .build()
            val body = client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return
                response.body.string()
            }
            val notice = JsonUtil.parseString(body)
                ?.takeIf { it.boolean("enabled") == true }
                ?.string("message")
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
            _state.value = _state.value.copy(notice = notice)
        }
    }

    fun isManagedProfile(guid: String): Boolean =
        MmkvManager.decodeServerConfig(guid)?.subscriptionId == SUBSCRIPTION_ID

    private fun ensureManagedSubscription(url: String, token: String) {
        val item = (MmkvManager.decodeSubscription(SUBSCRIPTION_ID) ?: SubscriptionItem()).apply {
            remarks = "ProSyS Free - 500MB Daily"
            this.url = url
            enabled = true
            autoUpdate = true
            updateInterval = 60
            userAgent = "ProSyS-VPN/${BuildConfig.VERSION_NAME}"
            requestHeaders = JsonUtil.toJson(mapOf(HEADER_NAME to token))
        }
        MmkvManager.encodeSubscription(SUBSCRIPTION_ID, item)
        SubscriptionUpdater.syncOne(subId = SUBSCRIPTION_ID)
    }

    private fun stableInstallationId(context: Context, prefs: SharedPreferences): String {
        prefs.getString(INSTALLATION_ID, null)?.let { return it }
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        val source = if (!androidId.isNullOrBlank() && androidId != "9774d56d682e549c") {
            "prosys-device-v1:$androidId"
        } else {
            "prosys-fallback-v1:${UUID.randomUUID()}"
        }
        val anonymousId = MessageDigest.getInstance("SHA-256")
            .digest(source.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        prefs.edit().putString(INSTALLATION_ID, anonymousId).apply()
        return anonymousId
    }

    private fun applyState(json: JsonObject) {
        val quota = json.long("quotaBytes") ?: _state.value.quotaBytes
        val used = json.long("usedBytes") ?: 0
        _state.value = FreeAccessState(
            loading = false,
            ready = true,
            quotaBytes = quota,
            usedBytes = used,
            remainingBytes = json.long("remainingBytes") ?: (quota - used).coerceAtLeast(0),
            resetAt = json.long("resetAt") ?: 0,
            exhausted = json.boolean("exhausted") ?: (used >= quota),
            notice = _state.value.notice,
            error = null
        )
    }

    private fun JsonObject.string(name: String): String? = get(name)?.takeUnless { it.isJsonNull }?.asString
    private fun JsonObject.long(name: String): Long? = get(name)?.takeUnless { it.isJsonNull }?.asLong
    private fun JsonObject.boolean(name: String): Boolean? = get(name)?.takeUnless { it.isJsonNull }?.asBoolean
}
