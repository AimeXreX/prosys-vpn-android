package com.v2ray.ang.handler

import com.v2ray.ang.util.JsonUtil

data class ConnectionHealth(
    val guid: String,
    var successes: Int = 0,
    var failures: Int = 0,
    var consecutiveFailures: Int = 0,
    var lastSuccessAt: Long = 0L
)

object ConnectionHealthManager {
    private const val STORAGE_KEY = "prosys_connection_health_v1"

    private fun read(): MutableList<ConnectionHealth> {
        val raw = MmkvManager.decodeSettingsString(STORAGE_KEY).orEmpty()
        return JsonUtil.fromJsonSafe(raw, Array<ConnectionHealth>::class.java)
            ?.toMutableList()
            ?: mutableListOf()
    }

    @Synchronized
    fun snapshot(): Map<String, ConnectionHealth> = read().associateBy { it.guid }

    @Synchronized
    fun recordSuccess(guid: String?) {
        if (guid.isNullOrBlank()) return
        val values = read()
        val health = values.firstOrNull { it.guid == guid }
            ?: ConnectionHealth(guid).also(values::add)
        health.successes = (health.successes + 1).coerceAtMost(10_000)
        health.consecutiveFailures = 0
        health.lastSuccessAt = System.currentTimeMillis()
        MmkvManager.encodeSettings(STORAGE_KEY, JsonUtil.toJson(values))
    }

    @Synchronized
    fun recordFailure(guid: String?) {
        if (guid.isNullOrBlank()) return
        val values = read()
        val health = values.firstOrNull { it.guid == guid }
            ?: ConnectionHealth(guid).also(values::add)
        health.failures = (health.failures + 1).coerceAtMost(10_000)
        health.consecutiveFailures = (health.consecutiveFailures + 1).coerceAtMost(20)
        MmkvManager.encodeSettings(STORAGE_KEY, JsonUtil.toJson(values))
    }
}
