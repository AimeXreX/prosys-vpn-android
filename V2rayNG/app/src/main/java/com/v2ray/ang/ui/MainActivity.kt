package com.v2ray.ang.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.VpnService
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.Lifecycle
import com.v2ray.ang.AppConfig
import com.v2ray.ang.R
import com.v2ray.ang.compose.AppDivider
import com.v2ray.ang.compose.AppTheme
import com.v2ray.ang.compose.AppTopBar
import com.v2ray.ang.compose.ConfirmDialog
import com.v2ray.ang.compose.SelectListDialog
import com.v2ray.ang.compose.LocalDarkTheme
import com.v2ray.ang.compose.QRCodeDialog
import com.v2ray.ang.compose.ReorderableGridItem
import com.v2ray.ang.compose.ReorderableListItem
import com.v2ray.ang.compose.colorConfigType
import com.v2ray.ang.compose.colorFabActive
import com.v2ray.ang.compose.colorFabInactiveDark
import com.v2ray.ang.compose.colorFabInactiveLight
import com.v2ray.ang.compose.colorPing
import com.v2ray.ang.compose.colorPingRed
import com.v2ray.ang.compose.verticalScrollbar
import com.v2ray.ang.core.CoreServiceManager
import com.v2ray.ang.dto.GroupMapItem
import com.v2ray.ang.dto.entities.ProfileItem
import com.v2ray.ang.dto.entities.ServersCache
import com.v2ray.ang.enums.EConfigType
import com.v2ray.ang.enums.PermissionType
import com.v2ray.ang.extension.isComplexType
import com.v2ray.ang.extension.nullIfBlank
import com.v2ray.ang.extension.toast
import com.v2ray.ang.extension.toastError
import com.v2ray.ang.extension.toastSuccess
import com.v2ray.ang.handler.AngConfigManager
import com.v2ray.ang.handler.ConnectionHealthManager
import com.v2ray.ang.handler.FreeAccessManager
import com.v2ray.ang.handler.FreeAccessState
import com.v2ray.ang.handler.MmkvManager
import com.v2ray.ang.handler.SettingsChangeManager
import com.v2ray.ang.handler.SettingsManager
import com.v2ray.ang.handler.SubscriptionUpdater
import com.v2ray.ang.util.LogUtil
import com.v2ray.ang.util.Utils
import com.v2ray.ang.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.withContext
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState
import sh.calvin.reorderable.rememberReorderableLazyListState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.yield
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

class MainActivity : HelperBaseComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    private var failoverAttempts = 0
    private val failedQuickServers = mutableSetOf<String>()
    private var quickConnectSession = false
    private var quickConnectJob: Job? = null

    private val requestVpnPermission =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) startV2Ray()
        }

    // Launcher for profile editor activities (ServerActivity, ServerCustomConfigActivity, etc.)
    private val profileEditorLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult

            val data = result.data ?: return@registerForActivityResult
            val action = data.getStringExtra(ProfileEditorResult.EXTRA_ACTION)
                ?: return@registerForActivityResult

            if (action != ProfileEditorResult.ACTION_SAVED &&
                action != ProfileEditorResult.ACTION_DELETED
            ) {
                return@registerForActivityResult
            }

            val restartService = data.getBooleanExtra(
                ProfileEditorResult.EXTRA_RESTART_SERVICE,
                false
            )

            mainViewModel.setupGroupTab(forceRefresh = true)

            if (restartService && mainViewModel.uiState.value.isRunning) {
                restartV2Ray()
            }
        }

    // Launcher for settings, subscription, routing, etc. (non-editor sever pages)
    private val settingsActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val restartService = SettingsChangeManager.consumeRestartService()
            val refreshGroups = SettingsChangeManager.consumeSetupGroupTab()

            mainViewModel.refreshUiSettings()

            if (refreshGroups) {
                mainViewModel.setupGroupTab(forceRefresh = true)
            }

            if (restartService && mainViewModel.uiState.value.isRunning) {
                restartV2Ray()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel.initialize()

        lifecycleScope.launch {
            FreeAccessManager.initialize(this@MainActivity)
            mainViewModel.setupGroupTab(forceRefresh = true)
            mainViewModel.refreshSelectedGuid()
            while (true) {
                delay(5 * 60_000L)
                FreeAccessManager.refreshStatus(this@MainActivity)
            }
        }

        lifecycleScope.launch {
            mainViewModel.connectionEvents.collect { connected ->
                if (connected) {
                    quickConnectSession = false
                    failoverAttempts = 0
                    failedQuickServers.clear()
                    window.decorView.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    checkAndRequestPermission(PermissionType.POST_NOTIFICATIONS) {}
                } else if (
                    lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED) &&
                    quickConnectSession &&
                    failoverAttempts < 2
                ) {
                    MmkvManager.getSelectServer()?.let(failedQuickServers::add)
                    val next = mainViewModel.selectBestMeasuredServer(
                        allowManagedFree = !FreeAccessManager.state.value.exhausted,
                        excluded = failedQuickServers
                    )
                    if (next != null) {
                        failoverAttempts++
                        toast(R.string.prosys_switching_server)
                        window.decorView.performHapticFeedback(HapticFeedbackConstants.REJECT)
                        delay(650L)
                        beginVpnConnection()
                    }
                }
            }
        }

    }

    @Composable
    override fun ScreenContent() {
        val freeAccessState by FreeAccessManager.state.collectAsStateWithLifecycle()
        val language = LocalConfiguration.current.locales[0]?.language.orEmpty()
        val direction = if (language == "fa" || language == "ar") {
            LayoutDirection.Rtl
        } else {
            LayoutDirection.Ltr
        }
        CompositionLocalProvider(LocalLayoutDirection provides direction) {
        MainScreen(
            mainViewModel = mainViewModel,
            freeAccessState = freeAccessState,
            onRefreshFreeAccess = {
                lifecycleScope.launch {
                    FreeAccessManager.initialize(this@MainActivity)
                    mainViewModel.setupGroupTab(forceRefresh = true)
                    mainViewModel.refreshSelectedGuid()
                }
            },
            onOpenStore = { Utils.openUri(this, AppConfig.PRO_SYS_SITE_URL) },
            onOpenBot = { Utils.openUri(this, AppConfig.PRO_SYS_BOT_URL) },
            onQuickConnect = ::handleQuickConnect,
            onSelectedConnect = ::handleSelectedConnection,
            onTestClick = ::handleLayoutTestClick,
            onNavigate = ::navigateTo,
            onImportManually = ::importManually,
            onImportQRcode = ::importQRcode,
            onImportClipboard = ::importClipboard,
            onImportLocal = ::importConfigLocal,
            onSubUpdate = ::importConfigViaSub,
            onExportAll = ::exportAll,
            onRealPingAll = mainViewModel::testAllRealPing,
            onRestartService = ::restartV2Ray,
            onDelAllConfig = ::delAllConfig,
            onDelDuplicateConfig = ::delDuplicateConfig,
            onDelInvalidConfig = ::delInvalidConfig,
            onSortByTestResults = ::sortByTestResults,
            onEditServer = ::editServer,
            onRemoveServer = ::removeServer,
            onSelectServer = ::setSelectServer,
            onShareQRCode = ::getShareQRCodeBitmap,
            onShareClipboard = ::shareToClipboard,
            onShareFullContent = ::shareFullContentAsync,
            onSubscriptionIdChanged = mainViewModel::subscriptionIdChanged,
            onLocateSelectedServer = mainViewModel::triggerLocateSelectedServer,
            shareMethodEntries = resources.getStringArray(R.array.share_method).toList(),
            shareMethodMoreEntries = resources.getStringArray(R.array.share_method_more).toList()
        )
    }
    }

    fun getShareQRCodeBitmap(guid: String): Bitmap? =
        if (FreeAccessManager.isManagedProfile(guid)) null else AngConfigManager.share2QRCode(guid)
    fun shareToClipboard(guid: String): Boolean =
        !FreeAccessManager.isManagedProfile(guid) && AngConfigManager.share2Clipboard(this, guid) == 0

    fun shareFullContentAsync(guid: String) {
        if (FreeAccessManager.isManagedProfile(guid)) return
        lifecycleScope.launch(Dispatchers.IO) {
            val result = AngConfigManager.shareFullContent2Clipboard(this@MainActivity, guid)
            withContext(Dispatchers.Main) {
                if (result == 0) toastSuccess(R.string.toast_success)
                else toastError(R.string.toast_failure)
            }
        }
    }

    private fun navigateTo(destination: String) {
        val intent = when (destination) {
            "sub_setting" -> Intent(this, SubSettingActivity::class.java)
            "per_app_proxy" -> Intent(this, PerAppProxyActivity::class.java)
            "routing_setting" -> Intent(this, RoutingSettingActivity::class.java)
            "user_asset" -> Intent(this, UserAssetActivity::class.java)
            "settings" -> Intent(this, SettingsActivity::class.java)
            "logcat" -> Intent(this, LogcatActivity::class.java)
            "check_update" -> Intent(this, CheckUpdateActivity::class.java)
            "backup_restore" -> Intent(this, BackupActivity::class.java)
            "about" -> Intent(this, AboutActivity::class.java)
            "promotion" -> {
                Utils.openUri(
                    this,
                    "${Utils.decode(AppConfig.APP_PROMOTION_URL)}?t=${System.currentTimeMillis()}"
                )
                return
            }
            "sales_bot" -> {
                Utils.openUri(this, AppConfig.PRO_SYS_BOT_URL)
                return
            }
            "official_channel" -> {
                Utils.openUri(this, AppConfig.PRO_SYS_CHANNEL_URL)
                return
            }
            else -> return
        }
        settingsActivityLauncher.launch(intent)
    }

    private fun handleQuickConnect() {
        if (mainViewModel.uiState.value.isTesting) {
            quickConnectJob?.cancel()
            quickConnectJob = null
            quickConnectSession = false
            mainViewModel.cancelAllPing()
            toast(R.string.prosys_quick_cancelled)
            return
        }
        if (mainViewModel.uiState.value.isRunning) {
            quickConnectSession = false
            CoreServiceManager.stopVService(this)
            return
        }

        quickConnectJob = lifecycleScope.launch {
            quickConnectSession = true
            failoverAttempts = 0
            failedQuickServers.clear()
            window.decorView.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            val hasServers = mainViewModel.testAllForQuickConnect()
            if (!hasServers) {
                quickConnectSession = false
                toastError(R.string.prosys_quick_empty)
                return@launch
            }

            val testFinished = withTimeoutOrNull(12_000L) {
                while (mainViewModel.uiState.value.isTesting) {
                    delay(120L)
                }
                true
            } ?: false
            if (!testFinished) {
                mainViewModel.cancelAllPing()
            }

            val selected = mainViewModel.selectBestMeasuredServer(
                allowManagedFree = !FreeAccessManager.state.value.exhausted
            )
            if (selected == null) {
                quickConnectSession = false
                toastError(R.string.prosys_quick_failed)
                return@launch
            }
            beginVpnConnection()
        }
        quickConnectJob?.invokeOnCompletion { quickConnectJob = null }
    }

    /**
     * Connects exactly the profile selected by the user, without pinging,
     * re-sorting, or replacing that selection.
     */
    private fun handleSelectedConnection() {
        quickConnectSession = false
        failoverAttempts = 0
        failedQuickServers.clear()
        if (mainViewModel.uiState.value.isRunning) {
            CoreServiceManager.stopVService(this)
        } else {
            beginVpnConnection()
        }
    }

    private fun beginVpnConnection() {
        if (SettingsManager.isVpnMode()) {
            val intent = VpnService.prepare(this)
            if (intent == null) startV2Ray() else requestVpnPermission.launch(intent)
        } else {
            startV2Ray()
        }
    }

    private fun handleLayoutTestClick() {
        if (mainViewModel.uiState.value.isRunning) mainViewModel.testCurrentServerRealPing()
    }

    private fun startV2Ray() {
        if (MmkvManager.getSelectServer().isNullOrEmpty()) {
            toast(R.string.title_file_chooser); return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN &&
            MmkvManager.decodeSettingsBool(AppConfig.PREF_PROXY_SHARING)
        ) {
            checkAndRequestPermission(PermissionType.ACCESS_LOCAL_NETWORK) {}
        }
        CoreServiceManager.startVService(this)
    }

    private fun restartV2Ray() {
        if (mainViewModel.uiState.value.isRunning) CoreServiceManager.stopVService(this)
        lifecycleScope.launch { delay(500); startV2Ray() }
    }

    private fun importManually(createConfigType: Int) {
        val intent = when (createConfigType) {
            EConfigType.POLICYGROUP.value -> Intent(this, ServerGroupActivity::class.java)
            EConfigType.PROXYCHAIN.value -> Intent(this, ServerProxyChainActivity::class.java)
            else -> Intent(this, ServerActivity::class.java).apply {
                putExtra("createConfigType", createConfigType)
            }
        }.apply {
            putExtra("subscriptionId", mainViewModel.subscriptionId.takeUnless { it == FreeAccessManager.SUBSCRIPTION_ID }.orEmpty())
        }
        profileEditorLauncher.launch(intent)
    }

    private fun importQRcode() {
        launchQRCodeScanner { scanResult ->
            if (scanResult != null) importBatchConfig(scanResult)
        }
    }

    private fun importClipboard() {
        try {
            importBatchConfig(Utils.getClipboard(this))
        } catch (e: Exception) {
            LogUtil.e(AppConfig.TAG, "Failed to import config from clipboard", e)
        }
    }

    private fun importBatchConfig(server: String?) {
        mainViewModel.setLoading(true)
        lifecycleScope.launch {
            try {
                val (count, countSub) = withContext(Dispatchers.IO) {
                    AngConfigManager.importBatchConfig(
                        server,
                        mainViewModel.subscriptionId.takeUnless { it == FreeAccessManager.SUBSCRIPTION_ID }.orEmpty(),
                        true
                    )
                }
                when {
                    count > 0 -> {
                        toast(getString(R.string.title_import_config_count, count))
                        mainViewModel.setupGroupTab(forceRefresh = true)
                    }
                    countSub > 0 -> mainViewModel.setupGroupTab(forceRefresh = true)
                    else -> toastError(R.string.toast_failure)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                LogUtil.e(AppConfig.TAG, "Failed to import batch config", e)
                toastError(R.string.toast_failure)
            } finally {
                mainViewModel.setLoading(false)
            }
        }
    }

    private fun importConfigLocal() {
        launchFileChooser { uri ->
            if (uri == null) return@launchFileChooser
            try {
                contentResolver.openInputStream(uri)
                    .use { input -> importBatchConfig(input?.bufferedReader()?.readText()) }
            } catch (e: Exception) {
                LogUtil.e(AppConfig.TAG, "Failed to read content from URI", e)
            }
        }
    }

    private fun importConfigViaSub() {
        mainViewModel.setLoading(true)
        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    mainViewModel.updateConfigViaSubAll()
                }
                when {
                    result.successCount + result.failureCount + result.skipCount == 0 ->
                        toast(R.string.title_update_subscription_no_subscription)
                    result.successCount > 0 && result.failureCount + result.skipCount == 0 ->
                        toast(getString(R.string.title_update_config_count, result.configCount))
                    else ->
                        toast(
                            getString(
                                R.string.title_update_subscription_result,
                                result.configCount,
                                result.successCount,
                                result.failureCount,
                                result.skipCount
                            )
                        )
                }
                if (result.configCount > 0) {
                    mainViewModel.setupGroupTab(forceRefresh = true)
                    mainViewModel.refreshSelectedGuid()
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                LogUtil.e(AppConfig.TAG, "Subscription update failed", e)
                toastError(R.string.toast_failure)
            } finally {
                mainViewModel.setLoading(false)
            }
        }
    }

    private fun exportAll() {
        mainViewModel.setLoading(true)
        lifecycleScope.launch {
            try {
                val ret = withContext(Dispatchers.IO) {
                    mainViewModel.exportAllServer()
                }
                if (ret > 0) toast(getString(R.string.title_export_config_count, ret))
                else toastError(R.string.toast_failure)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                LogUtil.e(AppConfig.TAG, "Export failed", e)
                toastError(R.string.toast_failure)
            } finally {
                mainViewModel.setLoading(false)
            }
        }
    }

    private fun delAllConfig() {
        mainViewModel.setLoading(true)
        lifecycleScope.launch {
            try {
                val ret = withContext(Dispatchers.IO) {
                    mainViewModel.removeAllServer()
                }
                mainViewModel.setupGroupTab(forceRefresh = true)
                toast(getString(R.string.title_del_config_count, ret))
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                LogUtil.e(AppConfig.TAG, "Delete all failed", e)
                toastError(R.string.toast_failure)
            } finally {
                mainViewModel.setLoading(false)
            }
        }
    }

    private fun delDuplicateConfig() {
        mainViewModel.setLoading(true)
        lifecycleScope.launch {
            try {
                val ret = withContext(Dispatchers.IO) {
                    mainViewModel.removeDuplicateServer()
                }
                mainViewModel.setupGroupTab(forceRefresh = true)
                toast(getString(R.string.title_del_duplicate_config_count, ret))
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                LogUtil.e(AppConfig.TAG, "Delete duplicate failed", e)
                toastError(R.string.toast_failure)
            } finally {
                mainViewModel.setLoading(false)
            }
        }
    }

    private fun delInvalidConfig() {
        mainViewModel.setLoading(true)
        lifecycleScope.launch {
            try {
                val ret = withContext(Dispatchers.IO) {
                    mainViewModel.removeInvalidServer()
                }
                mainViewModel.setupGroupTab(forceRefresh = true)
                toast(getString(R.string.title_del_config_count, ret))
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                LogUtil.e(AppConfig.TAG, "Delete invalid failed", e)
                toastError(R.string.toast_failure)
            } finally {
                mainViewModel.setLoading(false)
            }
        }
    }

    private fun sortByTestResults() {
        mainViewModel.setLoading(true)
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    mainViewModel.sortByTestResults()
                }
                mainViewModel.setupGroupTab(forceRefresh = true)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                LogUtil.e(AppConfig.TAG, "Sort by test results failed", e)
                toastError(R.string.toast_failure)
            } finally {
                mainViewModel.setLoading(false)
            }
        }
    }

    private fun editServer(guid: String, profile: ProfileItem) {
        val activityClass = when (profile.configType) {
            EConfigType.CUSTOM -> ServerCustomConfigActivity::class.java
            EConfigType.POLICYGROUP -> ServerGroupActivity::class.java
            EConfigType.PROXYCHAIN -> ServerProxyChainActivity::class.java
            else -> ServerActivity::class.java
        }
        val intent = Intent(this, activityClass).apply {
            putExtra("guid", guid)
            putExtra("isRunning", mainViewModel.uiState.value.isRunning)
            putExtra("createConfigType", profile.configType.value)
            putExtra("subscriptionId", mainViewModel.subscriptionId)
        }
        profileEditorLauncher.launch(intent)
    }

    private fun removeServer(guid: String) {
        if (guid == MmkvManager.getSelectServer()) {
            toast(R.string.toast_action_not_allowed); return
        }
        mainViewModel.removeServerAndRefresh(guid)
    }

    private fun setSelectServer(guid: String) {
        val selected = MmkvManager.getSelectServer()
        if (guid != selected) {
            mainViewModel.updateSelectedGuid(guid)
            if (mainViewModel.uiState.value.isRunning) restartV2Ray()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_BUTTON_B) {
            moveTaskToBack(false)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}

@Composable
private fun MainDialogs(
    showDelAllConfirm: Boolean,
    onDismissDelAll: () -> Unit,
    onConfirmDelAll: () -> Unit,
    showDelDuplicateConfirm: Boolean,
    onDismissDelDuplicate: () -> Unit,
    onConfirmDelDuplicate: () -> Unit,
    showDelInvalidConfirm: Boolean,
    onDismissDelInvalid: () -> Unit,
    onConfirmDelInvalid: () -> Unit,
    showRemoveConfirm: String?,
    onDismissRemove: () -> Unit,
    onConfirmRemove: (String) -> Unit,
) {
    if (showDelAllConfirm) {
        ConfirmDialog(
            message = stringResource(R.string.del_config_comfirm),
            confirmText = stringResource(android.R.string.ok),
            dismissText = stringResource(android.R.string.cancel),
            onConfirm = onConfirmDelAll,
            onDismiss = onDismissDelAll
        )
    }
    if (showDelDuplicateConfirm) {
        ConfirmDialog(
            message = stringResource(R.string.del_config_comfirm),
            confirmText = stringResource(android.R.string.ok),
            dismissText = stringResource(android.R.string.cancel),
            onConfirm = onConfirmDelDuplicate,
            onDismiss = onDismissDelDuplicate
        )
    }
    if (showDelInvalidConfirm) {
        ConfirmDialog(
            message = stringResource(R.string.del_invalid_config_comfirm),
            confirmText = stringResource(android.R.string.ok),
            dismissText = stringResource(android.R.string.cancel),
            onConfirm = onConfirmDelInvalid,
            onDismiss = onDismissDelInvalid
        )
    }
    if (showRemoveConfirm != null) {
        val guid = showRemoveConfirm
        ConfirmDialog(
            message = stringResource(R.string.del_config_comfirm),
            confirmText = stringResource(android.R.string.ok),
            dismissText = stringResource(android.R.string.cancel),
            onConfirm = { onConfirmRemove(guid) },
            onDismiss = onDismissRemove
        )
    }
}

@Composable
private fun MainBottomBar(
    displayText: String,
    isRunning: Boolean,
    isDarkTheme: Boolean,
    onTestClick: () -> Unit,
    onFabClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (isRunning) stringResource(R.string.prosys_connected_simple)
                else stringResource(R.string.prosys_disconnected_simple),
                color = if (isRunning) colorFabActive else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Button(
                onClick = onFabClick,
                modifier = Modifier.fillMaxWidth().height(58.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(
                    painter = if (isRunning) painterResource(R.drawable.ic_stop_24dp)
                    else painterResource(R.drawable.ic_play_24dp),
                    contentDescription = null,
                    modifier = Modifier.size(25.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = if (isRunning) stringResource(R.string.prosys_disconnect)
                    else stringResource(R.string.prosys_quick_connect),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Text(
                text = if (isRunning) displayText else stringResource(R.string.prosys_help_hint),
                modifier = Modifier.clickable(onClick = onTestClick),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProSysPlansCard(
    onOpenStore: () -> Unit,
    onOpenBot: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.48f)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                stringResource(R.string.prosys_plans_title),
                fontWeight = FontWeight.Black,
                fontSize = 15.sp
            )
            Text(
                stringResource(R.string.prosys_plans_subtitle),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onOpenStore, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.prosys_open_website))
                }
                OutlinedButton(onClick = onOpenBot, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.prosys_open_channel))
                }
            }
        }
    }
}

@Composable
private fun ProSysHomeDashboard(
    modifier: Modifier = Modifier,
    isRunning: Boolean,
    isTesting: Boolean,
    qualityText: String,
    pingMillis: Long,
    stabilityPercent: Int,
    hasStabilityData: Boolean,
    lastTestAt: Long,
    freeAccessState: FreeAccessState,
    onConnect: () -> Unit,
    onConnections: () -> Unit,
    onMore: () -> Unit,
    onDiagnostics: () -> Unit,
    onRefresh: () -> Unit,
    onOpenStore: () -> Unit,
    onOpenBot: () -> Unit
) {
    val accent = Color(0xFFA9FF5B)
    val remainingMb = (freeAccessState.remainingBytes / (1024L * 1024L)).coerceAtLeast(0L)
    val quotaMb = (freeAccessState.quotaBytes / (1024L * 1024L)).coerceAtLeast(1L)
    val progress = (remainingMb.toFloat() / quotaMb.toFloat()).coerceIn(0f, 1f)
    val usedMb = (freeAccessState.usedBytes / (1024L * 1024L)).coerceAtLeast(0L)
    val resetTime = if (freeAccessState.resetAt > 0L) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(freeAccessState.resetAt))
    } else "—"
    val minutesSinceTest = if (lastTestAt > 0L) {
        ((System.currentTimeMillis() - lastTestAt).coerceAtLeast(0L) / 60_000L).toInt()
    } else -1
    val lastTestText = when {
        minutesSinceTest < 0 -> stringResource(R.string.prosys_never_tested)
        minutesSinceTest == 0 -> stringResource(R.string.prosys_just_now)
        else -> stringResource(R.string.prosys_minutes_ago, minutesSinceTest)
    }
    val neonTransition = rememberInfiniteTransition(label = "prosys-neon")
    val neonPulse by neonTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse),
        label = "neon-pulse"
    )
    val buttonScale by neonTransition.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse),
        label = "connect-breathe"
    )

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val compact = maxHeight < 680.dp
        val connectSize = if (compact) 164.dp else 190.dp
        val outerPadding = if (compact) 12.dp else 18.dp
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = maxHeight)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = outerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(if (compact) 40.dp else 46.dp),
                    shape = RoundedCornerShape(15.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Image(
                        painter = painterResource(R.drawable.prosys_logo_mark),
                        contentDescription = stringResource(R.string.prosys_cd_logo),
                        modifier = Modifier.padding(5.dp)
                    )
                }
                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(stringResource(R.string.app_name), fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Text(
                        stringResource(R.string.prosys_home_subtitle),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = onMore) {
                    Icon(painterResource(R.drawable.ic_more_vert_24dp), stringResource(R.string.prosys_more))
                }
            }

            Column(
                modifier = Modifier.padding(vertical = if (compact) 12.dp else 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        Modifier
                            .size(connectSize + 22.dp)
                            .graphicsLayer {
                                alpha = neonPulse * if (isRunning || isTesting) 0.55f else 0.24f
                                scaleX = buttonScale
                                scaleY = buttonScale
                            }
                            .clip(CircleShape)
                            .background(accent.copy(alpha = 0.28f))
                    )
                    Surface(
                        onClick = onConnect,
                        modifier = Modifier
                            .size(connectSize)
                            .graphicsLayer {
                                scaleX = if (isRunning || isTesting) buttonScale else 1f
                                scaleY = if (isRunning || isTesting) buttonScale else 1f
                            }
                            .shadow(if (isRunning) 22.dp else 10.dp, CircleShape)
                            .animateContentSize(animationSpec = tween(durationMillis = 280))
                            .heightIn(min = 48.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = BorderStroke(
                            if (isRunning || isTesting) 3.dp else 2.dp,
                            if (isRunning || isTesting) accent.copy(alpha = neonPulse)
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                        ),
                        shadowElevation = if (isRunning) 18.dp else 8.dp
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                        Icon(
                            painter = if (isRunning) painterResource(R.drawable.ic_stop_24dp)
                            else painterResource(R.drawable.ic_play_24dp),
                            contentDescription = if (isRunning) {
                                stringResource(R.string.prosys_cd_disconnect)
                            } else {
                                stringResource(R.string.prosys_cd_connect)
                            },
                            modifier = Modifier.size(if (compact) 42.dp else 50.dp),
                            tint = if (isRunning || isTesting) accent else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = when {
                                isTesting -> stringResource(R.string.prosys_cancel_test)
                                isRunning -> stringResource(R.string.prosys_connected_simple)
                                else -> stringResource(R.string.prosys_quick_connect)
                            },
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Black,
                            fontSize = if (compact) 16.sp else 18.sp
                        )
                        if (!isTesting) {
                            Text(
                                if (isRunning) stringResource(R.string.prosys_tap_to_disconnect)
                                else stringResource(R.string.prosys_tap_to_connect),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                        }
                    }
                }
                Spacer(Modifier.height(if (compact) 10.dp else 16.dp))
                Text(
                    text = if (isRunning) stringResource(R.string.prosys_safe)
                    else stringResource(R.string.prosys_help_hint),
                    color = if (isRunning) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(qualityText, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                Spacer(Modifier.height(10.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            stringResource(R.string.prosys_connection_stage),
                            color = accent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            when {
                                isTesting -> stringResource(R.string.prosys_stage_testing)
                                isRunning -> stringResource(R.string.prosys_stage_connected)
                                freeAccessState.ready -> stringResource(R.string.prosys_stage_ready)
                                else -> stringResource(R.string.prosys_stage_disconnected)
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            when {
                                isTesting -> stringResource(R.string.prosys_next_action_wait)
                                isRunning -> stringResource(R.string.prosys_next_action_connected)
                                else -> stringResource(R.string.prosys_next_action_connect)
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProSysMetric(
                        Modifier.weight(1f),
                        stringResource(R.string.prosys_metric_ping),
                        if (pingMillis > 0L) stringResource(R.string.prosys_ping_value, pingMillis)
                        else stringResource(R.string.prosys_no_ping)
                    )
                    ProSysMetric(
                        Modifier.weight(1f),
                        stringResource(R.string.prosys_metric_stability),
                        if (hasStabilityData) stringResource(R.string.prosys_stability_value, stabilityPercent)
                        else stringResource(R.string.prosys_insufficient_data)
                    )
                    ProSysMetric(
                        Modifier.weight(1f),
                        stringResource(R.string.prosys_metric_last_test),
                        lastTestText
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 11.dp)
            ) {
                freeAccessState.notice?.let { notice ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
                    ) {
                        Text(notice, Modifier.padding(11.dp), fontSize = 13.sp, textAlign = TextAlign.Center)
                    }
                }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Column(Modifier.padding(horizontal = 15.dp, vertical = 11.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(stringResource(R.string.prosys_free_compact), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(
                                    if (freeAccessState.exhausted) stringResource(R.string.prosys_free_exhausted)
                                    else stringResource(R.string.prosys_free_remaining, "$remainingMb MB"),
                                    color = if (freeAccessState.exhausted) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                            TextButton(onClick = if (freeAccessState.exhausted) onOpenStore else onRefresh) {
                                Text(
                                    if (freeAccessState.exhausted) stringResource(R.string.prosys_free_site)
                                    else stringResource(R.string.prosys_free_refresh)
                                )
                            }
                        }
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(10.dp)),
                            color = accent,
                            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ProSysQuotaValue(
                                stringResource(R.string.prosys_quota_total),
                                stringResource(R.string.prosys_usage_value, quotaMb.toString())
                            )
                            ProSysQuotaValue(
                                stringResource(R.string.prosys_quota_used),
                                stringResource(R.string.prosys_usage_value, usedMb.toString())
                            )
                            ProSysQuotaValue(
                                stringResource(R.string.prosys_quota_remaining),
                                stringResource(R.string.prosys_usage_value, remainingMb.toString())
                            )
                        }
                        Text(
                            stringResource(R.string.prosys_reset_time, resetTime),
                            modifier = Modifier.fillMaxWidth().padding(top = 7.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    Button(
                        onClick = onConnections,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(painterResource(R.drawable.ic_subscriptions_24dp), null, Modifier.size(19.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.prosys_my_connections))
                    }
                    OutlinedButton(
                        onClick = onDiagnostics,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(stringResource(R.string.prosys_diagnostics), textAlign = TextAlign.Center)
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    Button(
                        onClick = onOpenStore,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(stringResource(R.string.prosys_buy))
                    }
                    OutlinedButton(
                        onClick = onDiagnostics,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(stringResource(R.string.prosys_open_help))
                    }
                }
                if (freeAccessState.exhausted ||
                    freeAccessState.remainingBytes <= freeAccessState.quotaBytes / 5
                ) {
                    Surface(
                        onClick = onOpenStore,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.38f)
                    ) {
                        Row(
                            Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.prosys_plans_title), Modifier.weight(1f), fontWeight = FontWeight.Bold)
                            TextButton(onClick = onOpenBot) {
                                Text(stringResource(R.string.prosys_open_channel))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProSysMetric(modifier: Modifier, label: String, value: String) {
    Surface(modifier = modifier.heightIn(min = 72.dp), shape = RoundedCornerShape(14.dp)) {
        Column(
            Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            Text(
                value,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun ProSysQuotaValue(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

private fun buildDiagnosticMessage(context: Context, state: FreeAccessState): String {
    val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivity.activeNetwork
    val capabilities = network?.let(connectivity::getNetworkCapabilities)
    val hasInternet = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    val hasConfigs = MmkvManager.decodeAllServerList().isNotEmpty()
    val automaticTime = Settings.Global.getInt(context.contentResolver, Settings.Global.AUTO_TIME, 0) == 1
    val mark = { ok: Boolean -> if (ok) "✓" else "!" }
    return buildString {
        appendLine("${mark(hasInternet)} ${context.getString(R.string.prosys_diag_internet)}: ${context.getString(if (hasInternet) R.string.prosys_diag_ok else R.string.prosys_diag_failed)}")
        appendLine("${mark(hasConfigs)} ${context.getString(R.string.prosys_diag_connection)}: ${context.getString(if (hasConfigs) R.string.prosys_diag_ok else R.string.prosys_diag_failed)}")
        appendLine("${mark(!state.exhausted)} ${context.getString(R.string.prosys_diag_quota)}: ${context.getString(if (!state.exhausted) R.string.prosys_diag_ok else R.string.prosys_diag_failed)}")
        appendLine("${mark(automaticTime)} ${context.getString(R.string.prosys_diag_time)}: ${context.getString(if (automaticTime) R.string.prosys_diag_ok else R.string.prosys_diag_failed)}")
        appendLine()
        append(context.getString(R.string.prosys_diag_advice))
    }
}

@Composable
private fun ProSysEmptyConnections(
    onRefresh: () -> Unit,
    onPaste: () -> Unit,
    onQr: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painterResource(R.drawable.ic_prosys_notification),
            contentDescription = null,
            modifier = Modifier.size(58.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(18.dp))
        Text(stringResource(R.string.prosys_empty_title), fontSize = 20.sp, fontWeight = FontWeight.Black)
        Text(
            stringResource(R.string.prosys_empty_body),
            modifier = Modifier.padding(vertical = 10.dp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(onClick = onRefresh, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.prosys_free_refresh))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onPaste, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.prosys_paste_link))
            }
            OutlinedButton(onClick = onQr, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.prosys_scan_qr))
            }
        }
    }
}

@Composable
private fun ProSysAccessCard(
    state: FreeAccessState,
    onRefresh: () -> Unit,
    onOpenStore: () -> Unit,
    onOpenBot: () -> Unit
) {
    val accent = Color(0xFFA9FF5B)
    val usedRatio = if (state.quotaBytes > 0) {
        (state.usedBytes.toFloat() / state.quotaBytes.toFloat()).coerceIn(0f, 1f)
    } else 0f
    val resetTime = if (state.resetAt > 0) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(state.resetAt))
    } else "00:00"
    val status = when {
        state.loading -> stringResource(R.string.prosys_free_loading)
        state.exhausted -> stringResource(R.string.prosys_free_exhausted)
        state.error != null && !state.ready -> stringResource(R.string.prosys_free_error)
        else -> stringResource(R.string.prosys_free_ready)
    }
    val remainingGb = String.format(Locale.US, "%.2f GB", state.remainingBytes / (1024.0 * 1024.0 * 1024.0))

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        shape = RoundedCornerShape(26.dp),
        color = Color(0xFF0A1B14),
        contentColor = Color(0xFFEAF6EF),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.24f)),
        shadowElevation = 6.dp
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF10271D)
                ) {
                    Image(
                        painter = painterResource(R.drawable.prosys_logo_mark),
                        contentDescription = stringResource(R.string.app_name),
                        modifier = Modifier.padding(5.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(
                        text = stringResource(R.string.prosys_free_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = stringResource(R.string.prosys_free_subtitle),
                        color = Color(0xFFA9BDB2),
                        fontSize = 10.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(if (state.exhausted || state.error != null) Color(0xFFFF7A7A) else accent)
                )
            }

            Text(text = status, color = if (state.exhausted) Color(0xFFFFA3A3) else accent, fontSize = 11.sp)
            LinearProgressIndicator(
                progress = { usedRatio },
                modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(10.dp)),
                color = if (state.exhausted) Color(0xFFFF6B6B) else accent,
                trackColor = Color(0xFF20372C)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.prosys_free_remaining, remainingGb), fontSize = 10.sp)
                Text(stringResource(R.string.prosys_free_resets, resetTime), color = Color(0xFFA9BDB2), fontSize = 10.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = if (state.exhausted) onOpenStore else onRefresh,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(13.dp)
                ) {
                    Text(if (state.exhausted) stringResource(R.string.prosys_free_site) else stringResource(R.string.prosys_free_refresh))
                }
                OutlinedButton(
                    onClick = onOpenBot,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(13.dp),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.45f))
                ) {
                    Text(stringResource(R.string.prosys_free_bot), color = accent)
                }
            }
        }
    }
}

private suspend fun PagerState.navigateToPageOptimized(
    targetPage: Int,
    animateAdjacentPage: Boolean = true
) {
    if (pageCount <= 0) return

    val target = targetPage.coerceIn(0, pageCount - 1)
    val current = settledPage.coerceIn(0, pageCount - 1)

    if (target == current) return

    val distance = abs(target - current)

    when {
        distance == 1 && animateAdjacentPage -> animateScrollToPage(target)
        animateAdjacentPage -> {
            val adjacent = if (target > current) target - 1 else target + 1
            scrollToPage(adjacent)
            yield()
            animateScrollToPage(target)
        }
        else -> scrollToPage(target)
    }
}

@Composable
private fun GroupTabBar(
    groups: List<GroupMapItem>,
    selectedTabIndex: Int,
    mainViewModel: MainViewModel,
    onTabClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    PrimaryScrollableTabRow(
        selectedTabIndex = selectedTabIndex.coerceIn(0, groups.lastIndex),
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        edgePadding = 16.dp,
        minTabWidth = 56.dp,
        indicator = {
            TabRowDefaults.PrimaryIndicator(
                modifier = Modifier
                    .tabIndicatorOffset(
                        selectedTabIndex = selectedTabIndex.coerceIn(0, groups.lastIndex),
                        matchContentSize = true
                    )
                    .clip(RoundedCornerShape(3.dp)),
                width = Dp.Unspecified,
                color = colorFabActive
            )
        },
        divider = {}
    ) {
        groups.forEachIndexed { index, group ->
            GroupTabItem(
                group = group,
                selected = index == selectedTabIndex,
                serverFlowProvider = {
                    mainViewModel.serversForGroup(group.id)
                },
                onClick = { onTabClick(index) }
            )
        }
    }
}

@Composable
private fun GroupTabItem(
    group: GroupMapItem,
    selected: Boolean,
    serverFlowProvider: () -> StateFlow<List<ServersCache>>,
    onClick: () -> Unit
) {
    val serverFlow = remember(group.id) {
        serverFlowProvider()
    }
    val servers by serverFlow.collectAsStateWithLifecycle()

    Tab(
        selected = selected,
        onClick = onClick,
        text = {
            val text = if (group.id.isEmpty()) {
                group.remarks
            } else {
                "${group.remarks} (${servers.size})"
            }
            Text(
                text = text,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    )
}

@Composable
private fun GroupPagerPage(
    groupId: String,
    mainViewModel: MainViewModel,
    selectedGuid: String?,
    doubleColumnDisplay: Boolean,
    confirmRemove: Boolean,
    searchQuery: String,
    lazyListStates: MutableMap<String, LazyListState>,
    lazyGridStates: MutableMap<String, LazyGridState>,
    onSelectServer: (String) -> Unit,
    onEditServer: (String, ProfileItem) -> Unit,
    onShareServer: (String, ProfileItem) -> Unit,
    onMoreServer: (String, ProfileItem) -> Unit,
    onRemoveServer: (String) -> Unit,
    contentPadding: PaddingValues
) {
    val serverFlow = remember(groupId) {
        mainViewModel.serversForGroup(groupId)
    }
    val servers by serverFlow.collectAsStateWithLifecycle()

    val canReorder = groupId.isNotEmpty() && searchQuery.isEmpty()

    ServerListPage(
        servers = servers,
        selectedGuid = selectedGuid,
        canReorder = canReorder,
        doubleColumnDisplay = doubleColumnDisplay,
        subscriptionId = groupId,
        confirmRemove = confirmRemove,
        groupId = groupId,
        lazyListStates = lazyListStates,
        lazyGridStates = lazyGridStates,
        onSelectServer = onSelectServer,
        onEditServer = onEditServer,
        onShareServer = onShareServer,
        onMoreServer = onMoreServer,
        onRemoveServer = onRemoveServer,
        onSwapServer = mainViewModel::swapServer,
        contentPadding = contentPadding
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    freeAccessState: FreeAccessState,
    onRefreshFreeAccess: () -> Unit,
    onOpenStore: () -> Unit,
    onOpenBot: () -> Unit,
    onQuickConnect: () -> Unit,
    onSelectedConnect: () -> Unit,
    onTestClick: () -> Unit,
    onNavigate: (String) -> Unit,
    onImportManually: (Int) -> Unit,
    onImportQRcode: () -> Unit,
    onImportClipboard: () -> Unit,
    onImportLocal: () -> Unit,
    onSubUpdate: () -> Unit,
    onExportAll: () -> Unit,
    onRealPingAll: () -> Unit,
    onRestartService: () -> Unit,
    onDelAllConfig: () -> Unit,
    onDelDuplicateConfig: () -> Unit,
    onDelInvalidConfig: () -> Unit,
    onSortByTestResults: () -> Unit,
    onEditServer: (String, ProfileItem) -> Unit,
    onRemoveServer: (String) -> Unit,
    onSelectServer: (String) -> Unit,
    onShareQRCode: (String) -> Bitmap?,
    onShareClipboard: (String) -> Boolean,
    onShareFullContent: (String) -> Unit,
    onSubscriptionIdChanged: (String) -> Unit,
    onLocateSelectedServer: () -> Unit,
    shareMethodEntries: List<String>,
    shareMethodMoreEntries: List<String>
) {
    val context = LocalContext.current
    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    val groups = uiState.groups
    val isLoading = uiState.isLoading
    val isRunning = uiState.isRunning
    val displayText = uiState.statusText
    val selectedGuid = uiState.selectedGuid
    val selectedDelay = selectedGuid
        ?.let(MmkvManager::decodeServerAffiliationInfo)
        ?.testDelayMillis
        ?: 0L
    val qualityText = stringResource(
        when {
            selectedDelay in 1..180 -> R.string.prosys_quality_excellent
            selectedDelay in 181..450 -> R.string.prosys_quality_good
            selectedDelay > 450 -> R.string.prosys_quality_weak
            else -> R.string.prosys_quality_unknown
        }
    )
    val connectionHealth = selectedGuid
        ?.let { ConnectionHealthManager.snapshot()[it] }
    val healthAttempts = (connectionHealth?.successes ?: 0) + (connectionHealth?.failures ?: 0)
    val stabilityPercent = if (healthAttempts > 0) {
        ((connectionHealth?.successes ?: 0) * 100 / healthAttempts).coerceIn(0, 100)
    } else 0
    val doubleColumnDisplay = uiState.doubleColumnDisplay
    val confirmRemove = uiState.confirmRemove

    val isDarkTheme = LocalDarkTheme.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showSearch by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var showImportMenu by remember { mutableStateOf(false) }
    var showDelAllConfirm by remember { mutableStateOf(false) }
    var showDelDuplicateConfirm by remember { mutableStateOf(false) }
    var showDelInvalidConfirm by remember { mutableStateOf(false) }
    var showRemoveConfirm by remember { mutableStateOf<String?>(null) }
    var showConnections by rememberSaveable { mutableStateOf(false) }
    var showDiagnostics by remember { mutableStateOf(false) }
    var showSimpleHelp by remember {
        mutableStateOf(!MmkvManager.decodeSettingsBool("prosys_v2_help_seen", false))
    }

    var shareTarget by remember { mutableStateOf<Triple<String, ProfileItem, Boolean>?>(null) }
    var showQRCodeBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { groups.size.coerceAtLeast(1) }
    )

    val lazyListStates = remember { mutableStateMapOf<String, LazyListState>() }
    val lazyGridStates = remember { mutableStateMapOf<String, LazyGridState>() }

    val drawerScrollState = rememberScrollState()
    val importMenuScrollState = rememberScrollState()
    val moreMenuScrollState = rememberScrollState()

    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val maxMenuHeight = LocalConfiguration.current.screenHeightDp.dp - statusBarHeight - navBarHeight - 20.dp

    var locateInProgress by remember { mutableStateOf(false) }

    LaunchedEffect(groups) {
        val validGroupIds = groups.map { it.id }.toSet()
        lazyListStates.keys.retainAll(validGroupIds)
        lazyGridStates.keys.retainAll(validGroupIds)
    }

    val latestDoubleColumnDisplay by rememberUpdatedState(doubleColumnDisplay)

    LaunchedEffect(groups, uiState.selectedGroupId) {
        if (groups.isEmpty()) return@LaunchedEffect
        val selectedIndex = groups.indexOfFirst { it.id == uiState.selectedGroupId }
            .takeIf { it >= 0 } ?: 0
        if (!pagerState.isScrollInProgress && pagerState.settledPage != selectedIndex) {
            pagerState.scrollToPage(selectedIndex)
        }
    }

    val latestGroups by rememberUpdatedState(groups)
    val latestLocateInProgress by rememberUpdatedState(locateInProgress)

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
                val currentGroups = latestGroups
                if (!latestLocateInProgress && page in currentGroups.indices) {
                    onSubscriptionIdChanged(currentGroups[page].id)
                }
            }
    }

    LaunchedEffect(mainViewModel, pagerState) {
        mainViewModel.locateEvent.collect { target ->
            if (target.groupIndex !in 0 until pagerState.pageCount) return@collect

            locateInProgress = true
            try {
                if (pagerState.settledPage != target.groupIndex) {
                    pagerState.navigateToPageOptimized(
                        targetPage = target.groupIndex,
                        animateAdjacentPage = false
                    )
                }
                onSubscriptionIdChanged(target.groupId)

                repeat(10) {
                    val ready = if (latestDoubleColumnDisplay) {
                        lazyGridStates[target.groupId] != null
                    } else {
                        lazyListStates[target.groupId] != null
                    }
                    if (ready) return@repeat
                    delay(16L)
                }

                if (latestDoubleColumnDisplay) {
                    lazyGridStates[target.groupId]?.let { gridState ->
                        gridState.scrollToItem(
                            index = target.itemPosition,
                            scrollOffset = -gridState.layoutInfo.viewportSize.height / 3
                        )
                    }
                } else {
                    lazyListStates[target.groupId]?.let { listState ->
                        listState.scrollToItem(
                            index = target.itemPosition,
                            scrollOffset = -listState.layoutInfo.viewportSize.height / 3
                        )
                    }
                }
            } finally {
                delay(32L)
                locateInProgress = false
            }
        }
    }

    MainDialogs(
        showDelAllConfirm = showDelAllConfirm,
        onDismissDelAll = { showDelAllConfirm = false },
        onConfirmDelAll = { showDelAllConfirm = false; onDelAllConfig() },
        showDelDuplicateConfirm = showDelDuplicateConfirm,
        onDismissDelDuplicate = { showDelDuplicateConfirm = false },
        onConfirmDelDuplicate = { showDelDuplicateConfirm = false; onDelDuplicateConfig() },
        showDelInvalidConfirm = showDelInvalidConfirm,
        onDismissDelInvalid = { showDelInvalidConfirm = false },
        onConfirmDelInvalid = { showDelInvalidConfirm = false; onDelInvalidConfig() },
        showRemoveConfirm = showRemoveConfirm,
        onDismissRemove = { showRemoveConfirm = null },
        onConfirmRemove = { guid -> showRemoveConfirm = null; onRemoveServer(guid) }
    )
    if (showSimpleHelp) {
        ConfirmDialog(
            message = stringResource(R.string.prosys_help_body),
            confirmText = stringResource(R.string.prosys_got_it),
            dismissText = "",
            onConfirm = {
                MmkvManager.encodeSettings("prosys_v2_help_seen", true)
                showSimpleHelp = false
            },
            onDismiss = {
                MmkvManager.encodeSettings("prosys_v2_help_seen", true)
                showSimpleHelp = false
            }
        )
    }
    if (showDiagnostics) {
        ConfirmDialog(
            message = buildDiagnosticMessage(context, freeAccessState),
            confirmText = stringResource(R.string.prosys_got_it),
            dismissText = "",
            onConfirm = { showDiagnostics = false },
            onDismiss = { showDiagnostics = false }
        )
    }

    if (shareTarget != null) {
        val (guid, profile, more) = shareTarget!!
        val isCustom = profile.configType.isComplexType()
        val (shareOptions, skip) = if (more) {
            val options = if (isCustom) shareMethodMoreEntries.takeLast(3) else shareMethodMoreEntries
            options to if (isCustom) 2 else 0
        } else {
            val options = if (isCustom) shareMethodEntries.takeLast(1) else shareMethodEntries
            options to if (isCustom) 2 else 0
        }
        SelectListDialog(
            options = shareOptions,
            onSelected = { index, _ ->
                shareTarget = null
                when (index + skip) {
                    0 -> showQRCodeBitmap = onShareQRCode(guid)
                    1 -> onShareClipboard(guid)
                    2 -> onShareFullContent(guid)
                    3 -> onEditServer(guid, profile)
                    4 -> onRemoveServer(guid)
                }
            },
            onDismiss = { shareTarget = null }
        )
    }
    if (showQRCodeBitmap != null) {
        QRCodeDialog(bitmap = showQRCodeBitmap, onDismiss = { showQRCodeBitmap = null })
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .navigationBarsPadding(),
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(drawerScrollState)
                        .verticalScrollbar(drawerScrollState)
                        .padding(bottom = 80.dp)
                ) {
                    Surface(modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontFamily = FontFamily(Font(R.font.montserrat_thin)),
                                    fontWeight = FontWeight.Thin
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    listOf(
                        Triple(
                            R.drawable.ic_subscriptions_24dp,
                            R.string.title_sub_setting,
                            "sub_setting"
                        ),
                        Triple(
                            R.drawable.ic_per_apps_24dp,
                            R.string.per_app_proxy_settings,
                            "per_app_proxy"
                        ),
                        Triple(
                            R.drawable.ic_routing_24dp,
                            R.string.routing_settings_title,
                            "routing_setting"
                        ),
                        Triple(
                            R.drawable.ic_file_24dp,
                            R.string.title_user_asset_setting,
                            "user_asset"
                        ),
                        Triple(R.drawable.ic_settings_24dp, R.string.title_settings, "settings"),
                    ).forEach { (iconRes, labelRes, route) ->
                        DrawerMenuItem(
                            icon = painterResource(iconRes),
                            label = stringResource(labelRes),
                            onClick = { scope.launch { drawerState.close() }; onNavigate(route) }
                        )
                    }
                    AppDivider(modifier = Modifier.padding(vertical = 4.dp))
                    listOf(
                        Triple(
                            R.drawable.ic_promotion_24dp,
                            R.string.title_pref_promotion,
                            "promotion"
                        ),
                        Triple(
                            R.drawable.ic_telegram_24dp,
                            R.string.prosys_sales_bot,
                            "sales_bot"
                        ),
                        Triple(
                            R.drawable.ic_telegram_24dp,
                            R.string.prosys_official_channel,
                            "official_channel"
                        ),
                        Triple(R.drawable.ic_logcat_24dp, R.string.title_logcat, "logcat"),
                        Triple(
                            R.drawable.ic_restore_24dp,
                            R.string.title_configuration_backup_restore,
                            "backup_restore"
                        ),
                        Triple(R.drawable.ic_about_24dp, R.string.title_about, "about"),
                    ).forEach { (iconRes, labelRes, route) ->
                        DrawerMenuItem(
                            icon = painterResource(iconRes),
                            label = stringResource(labelRes),
                            onClick = { scope.launch { drawerState.close() }; onNavigate(route) }
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
            topBar = {
                if (showConnections) AppTopBar(
                    title = stringResource(R.string.prosys_my_connections),
                    onBackClick = {},
                    isLoading = isLoading,
                    isSearchActive = showSearch,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { query ->
                        searchQuery = query
                        mainViewModel.filterConfig(query)
                    },
                    onSearchClose = {
                        searchQuery = ""
                        mainViewModel.filterConfig("")
                        showSearch = false
                    },
                    searchPlaceholder = stringResource(R.string.menu_item_search),
                    navigationIcon = {
                        if (showSearch) {
                            IconButton(onClick = {
                                searchQuery = ""
                                mainViewModel.filterConfig("")
                                showSearch = false
                            }) {
                                Icon(
                                    painterResource(R.drawable.ic_arrow_back_24dp),
                                    contentDescription = stringResource(R.string.prosys_cd_back)
                                )
                            }
                        } else {
                            IconButton(onClick = { showConnections = false }) {
                                Icon(
                                    painterResource(R.drawable.ic_arrow_back_24dp),
                                    contentDescription = stringResource(R.string.prosys_cd_back)
                                )
                            }
                        }
                    },
                    actions = {
                        if (!showSearch) {
                            IconButton(onClick = { showSearch = true }) {
                                Icon(
                                    painterResource(R.drawable.ic_search_24dp),
                                    contentDescription = stringResource(R.string.prosys_cd_search)
                                )
                            }
                        }
                        Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                            IconButton(onClick = { showImportMenu = true }) {
                                Icon(
                                    painterResource(R.drawable.ic_add_24dp),
                                    contentDescription = stringResource(R.string.prosys_cd_add)
                                )
                            }
                            DropdownMenu(
                                expanded = showImportMenu,
                                onDismissRequest = { showImportMenu = false },
                                scrollState = importMenuScrollState,
                                containerColor = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .heightIn(max = maxMenuHeight)
                                    .verticalScrollbar(importMenuScrollState)
                            ) {
                                listOf(
                                    R.string.menu_item_import_config_qrcode to {
                                        showImportMenu = false; onImportQRcode()
                                    },
                                    R.string.menu_item_import_config_clipboard to {
                                        showImportMenu = false; onImportClipboard()
                                    },
                                    R.string.menu_item_import_config_local to {
                                        showImportMenu = false; onImportLocal()
                                    },
                                    R.string.menu_item_import_config_policy_group to {
                                        showImportMenu = false; onImportManually(EConfigType.POLICYGROUP.value)
                                    },
                                    R.string.menu_item_import_config_proxy_chain to {
                                        showImportMenu = false; onImportManually(EConfigType.PROXYCHAIN.value)
                                    },
                                    R.string.menu_item_import_config_manually_vmess to {
                                        showImportMenu = false; onImportManually(EConfigType.VMESS.value)
                                    },
                                    R.string.menu_item_import_config_manually_vless to {
                                        showImportMenu = false; onImportManually(EConfigType.VLESS.value)
                                    },
                                    R.string.menu_item_import_config_manually_ss to {
                                        showImportMenu = false; onImportManually(EConfigType.SHADOWSOCKS.value)
                                    },
                                    R.string.menu_item_import_config_manually_socks to {
                                        showImportMenu = false; onImportManually(EConfigType.SOCKS.value)
                                    },
                                    R.string.menu_item_import_config_manually_http to {
                                        showImportMenu = false; onImportManually(EConfigType.HTTP.value)
                                    },
                                    R.string.menu_item_import_config_manually_trojan to {
                                        showImportMenu = false; onImportManually(EConfigType.TROJAN.value)
                                    },
                                    R.string.menu_item_import_config_manually_wireguard to {
                                        showImportMenu = false; onImportManually(EConfigType.WIREGUARD.value)
                                    },
                                    R.string.menu_item_import_config_manually_hysteria2 to {
                                        showImportMenu = false; onImportManually(EConfigType.HYSTERIA2.value)
                                    },
                                ).forEach { (stringRes, action) ->
                                    DropdownMenuItem(
                                        text = { Text(stringResource(stringRes)) },
                                        onClick = action
                                    )
                                }
                            }
                        }
                        Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    painterResource(R.drawable.ic_more_vert_24dp),
                                    contentDescription = null
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                scrollState = moreMenuScrollState,
                                containerColor = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .heightIn(max = maxMenuHeight)
                                    .verticalScrollbar(moreMenuScrollState)
                            ) {
                                listOf(
                                    R.string.title_service_restart to {
                                        showMenu = false; onRestartService()
                                    },
                                    R.string.title_del_all_config to {
                                        showMenu = false; showDelAllConfirm = true
                                    },
                                    R.string.title_del_duplicate_config to {
                                        showMenu = false; showDelDuplicateConfirm = true
                                    },
                                    R.string.title_del_invalid_config to {
                                        showMenu = false; showDelInvalidConfirm = true
                                    },
                                    R.string.title_export_all to {
                                        showMenu = false; onExportAll()
                                    },
                                    R.string.title_real_ping_all_server to {
                                        showMenu = false; onRealPingAll()
                                    },
                                    R.string.title_locate_selected_config to {
                                        showMenu = false; onLocateSelectedServer()
                                    },
                                    R.string.title_sort_by_test_results to {
                                        showMenu = false; onSortByTestResults()
                                    },
                                    R.string.title_sub_update to {
                                        showMenu = false; onSubUpdate()
                                    },
                                ).forEach { (stringRes, action) ->
                                    DropdownMenuItem(
                                        text = { Text(stringResource(stringRes)) },
                                        onClick = action
                                    )
                                }
                            }
                        }
                    }
                )
            },
            bottomBar = {
                if (showConnections) MainBottomBar(
                    displayText = displayText,
                    isRunning = isRunning,
                    isDarkTheme = isDarkTheme,
                    onTestClick = onTestClick,
                    onFabClick = onSelectedConnect
                )
            },
            floatingActionButton = {},
        ) { innerPadding ->
            if (!showConnections) {
                ProSysHomeDashboard(
                    modifier = Modifier.padding(innerPadding),
                    isRunning = isRunning,
                    isTesting = uiState.isTesting,
                    qualityText = qualityText,
                    pingMillis = selectedDelay,
                    stabilityPercent = stabilityPercent,
                    hasStabilityData = healthAttempts >= 3,
                    lastTestAt = connectionHealth?.lastSuccessAt ?: 0L,
                    freeAccessState = freeAccessState,
                    onConnect = onQuickConnect,
                    onConnections = { showConnections = true },
                    onMore = { scope.launch { drawerState.open() } },
                    onDiagnostics = { showDiagnostics = true },
                    onRefresh = onRefreshFreeAccess,
                    onOpenStore = onOpenStore,
                    onOpenBot = onOpenBot
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    Text(
                        text = stringResource(R.string.prosys_manual_mode),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )

                    if (MmkvManager.decodeAllServerList().isEmpty()) {
                        ProSysEmptyConnections(
                            onRefresh = onRefreshFreeAccess,
                            onPaste = onImportClipboard,
                            onQr = onImportQRcode
                        )
                    } else if (groups.isNotEmpty()) {
                    if (groups.size > 1) {
                        GroupTabBar(
                            groups = groups,
                            selectedTabIndex = pagerState.currentPage.coerceIn(0, groups.lastIndex),
                            mainViewModel = mainViewModel,
                            onTabClick = { targetIndex ->
                                scope.launch {
                                    pagerState.navigateToPageOptimized(
                                        targetPage = targetIndex,
                                        animateAdjacentPage = true
                                    )
                                }
                            }
                        )
                    }

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.weight(1f),
                        userScrollEnabled = true,
                        beyondViewportPageCount = 1,
                        key = { page -> groups.getOrNull(page)?.id ?: "group-page-$page" }
                    ) { page ->
                        val group = groups.getOrNull(page) ?: return@HorizontalPager

                        GroupPagerPage(
                            groupId = group.id,
                            mainViewModel = mainViewModel,
                            selectedGuid = selectedGuid,
                            doubleColumnDisplay = doubleColumnDisplay,
                            confirmRemove = confirmRemove,
                            searchQuery = searchQuery,
                            lazyListStates = lazyListStates,
                            lazyGridStates = lazyGridStates,
                            onSelectServer = onSelectServer,
                            onEditServer = onEditServer,
                            onShareServer = { guid, profile ->
                                if (profile.subscriptionId != FreeAccessManager.SUBSCRIPTION_ID) {
                                    shareTarget = Triple(guid, profile, false)
                                }
                            },
                            onMoreServer = { guid, profile ->
                                if (profile.subscriptionId != FreeAccessManager.SUBSCRIPTION_ID) {
                                    shareTarget = Triple(guid, profile, true)
                                }
                            },
                            onRemoveServer = { guid ->
                                if (!FreeAccessManager.isManagedProfile(guid)) {
                                    if (confirmRemove) showRemoveConfirm = guid
                                    else onRemoveServer(guid)
                                }
                            },
                            contentPadding = PaddingValues(
                                start = 0.dp,
                                top = 0.dp,
                                end = 0.dp,
                                bottom = 80.dp
                            )
                        )
                    }
                    }
                }
            }
        }
    }
}

@Composable
private fun ServerListPage(
    servers: List<ServersCache>,
    selectedGuid: String?,
    canReorder: Boolean,
    doubleColumnDisplay: Boolean,
    subscriptionId: String,
    confirmRemove: Boolean,
    groupId: String,
    lazyListStates: MutableMap<String, LazyListState>,
    lazyGridStates: MutableMap<String, LazyGridState>,
    onSelectServer: (String) -> Unit,
    onEditServer: (String, ProfileItem) -> Unit,
    onShareServer: (String, ProfileItem) -> Unit,
    onMoreServer: (String, ProfileItem) -> Unit,
    onRemoveServer: (String) -> Unit,
    onSwapServer: (Int, Int) -> Unit,
    contentPadding: PaddingValues
) {
    if (doubleColumnDisplay) {
        val gridState = remember(groupId) {
            lazyGridStates.getOrPut(groupId) { LazyGridState() }
        }
        val reorderableGridState = if (canReorder) {
            rememberReorderableLazyGridState(gridState) { from, to ->
                onSwapServer(from.index, to.index)
            }
        } else null

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = gridState,
            modifier = Modifier
                .fillMaxSize()
                .verticalScrollbar(gridState),
            contentPadding = contentPadding
        ) {
            itemsIndexed(items = servers, key = { _, item -> item.guid }) { _, serverCache ->
                val content: @Composable () -> Unit = {
                    ServerItemColumn(
                        serverCache = serverCache,
                        selectedGuid = selectedGuid,
                        subscriptionId = subscriptionId,
                        doubleColumnDisplay = true,
                        onSelectServer = onSelectServer,
                        onEditServer = onEditServer,
                        onShareServer = onShareServer,
                        onMoreServer = onMoreServer,
                        onRemoveServer = onRemoveServer
                    )
                }
                if (canReorder && reorderableGridState != null) {
                    ReorderableItem(
                        reorderableGridState,
                        key = serverCache.guid
                    ) { isDragging ->
                        ReorderableGridItem(
                            scope = this,
                            isDragging = isDragging
                        ) { content() }
                    }
                } else {
                    content()
                }
            }
        }
    } else {
        val listState = remember(groupId) {
            lazyListStates.getOrPut(groupId) { LazyListState() }
        }
        val reorderableState = if (canReorder) {
            rememberReorderableLazyListState(listState) { from, to ->
                onSwapServer(from.index, to.index)
            }
        } else null

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .verticalScrollbar(listState),
            contentPadding = contentPadding
        ) {
            itemsIndexed(items = servers, key = { _, item -> item.guid }) { _, serverCache ->
                if (canReorder && reorderableState != null) {
                    ReorderableItem(
                        reorderableState,
                        key = serverCache.guid
                    ) { isDragging ->
                        ReorderableListItem(
                            scope = this,
                            isDragging = isDragging
                        ) {
                            ServerItemRow(
                                serverCache = serverCache,
                                selectedGuid = selectedGuid,
                                subscriptionId = subscriptionId,
                                onSelectServer = onSelectServer,
                                onEditServer = onEditServer,
                                onShareServer = onShareServer,
                                onMoreServer = onMoreServer,
                                onRemoveServer = onRemoveServer
                            )
                        }
                        AppDivider(modifier = Modifier.padding(horizontal = 12.dp))
                    }
                } else {
                    ServerItemRow(
                        serverCache = serverCache,
                        selectedGuid = selectedGuid,
                        subscriptionId = subscriptionId,
                        onSelectServer = onSelectServer,
                        onEditServer = onEditServer,
                        onShareServer = onShareServer,
                        onMoreServer = onMoreServer,
                        onRemoveServer = onRemoveServer
                    )
                    AppDivider(modifier = Modifier.padding(horizontal = 12.dp))
                }
            }
        }
    }
}

@Composable
private fun ServerItemRow(
    serverCache: ServersCache,
    selectedGuid: String?,
    subscriptionId: String,
    onSelectServer: (String) -> Unit,
    onEditServer: (String, ProfileItem) -> Unit,
    onShareServer: (String, ProfileItem) -> Unit,
    onMoreServer: (String, ProfileItem) -> Unit,
    onRemoveServer: (String) -> Unit
) {
    val profile = serverCache.profile
    val subRemarks = if (subscriptionId.isEmpty()) {
        MmkvManager.decodeSubscription(profile.subscriptionId)?.remarks?.firstOrNull()
            ?.toString() ?: ""
    } else ""

    ServerListItem(
        remarks = profile.remarks,
        statistics = profile.description.nullIfBlank()
            ?: AngConfigManager.generateDescription(profile),
        typeDescription = getProtocolDescription(profile),
        testResult = serverCache.testDelayString,
        testDelayMillis = serverCache.testDelayMillis,
        isSelected = serverCache.guid == selectedGuid,
        isManaged = profile.subscriptionId == FreeAccessManager.SUBSCRIPTION_ID,
        subscriptionRemarks = subRemarks,
        doubleColumnDisplay = false,
        onClick = { onSelectServer(serverCache.guid) },
        onShare = { onShareServer(serverCache.guid, profile) },
        onEdit = { onEditServer(serverCache.guid, profile) },
        onRemove = { onRemoveServer(serverCache.guid) },
        onMore = { onMoreServer(serverCache.guid, profile) }
    )
}

@Composable
private fun ServerItemColumn(
    serverCache: ServersCache,
    selectedGuid: String?,
    subscriptionId: String,
    doubleColumnDisplay: Boolean,
    onSelectServer: (String) -> Unit,
    onEditServer: (String, ProfileItem) -> Unit,
    onShareServer: (String, ProfileItem) -> Unit,
    onMoreServer: (String, ProfileItem) -> Unit,
    onRemoveServer: (String) -> Unit
) {
    val profile = serverCache.profile
    val subRemarks = if (subscriptionId.isEmpty()) {
        MmkvManager.decodeSubscription(profile.subscriptionId)?.remarks?.firstOrNull()?.toString() ?: ""
    } else ""

    Column {
        ServerListItem(
            remarks = profile.remarks,
            statistics = profile.description.nullIfBlank() ?: AngConfigManager.generateDescription(profile),
            typeDescription = getProtocolDescription(profile),
            testResult = serverCache.testDelayString,
            testDelayMillis = serverCache.testDelayMillis,
            isSelected = serverCache.guid == selectedGuid,
            isManaged = profile.subscriptionId == FreeAccessManager.SUBSCRIPTION_ID,
            subscriptionRemarks = subRemarks,
            doubleColumnDisplay = doubleColumnDisplay,
            onClick = { onSelectServer(serverCache.guid) },
            onEdit = { onEditServer(serverCache.guid, profile) },
            onShare = { onShareServer(serverCache.guid, profile) },
            onRemove = { onRemoveServer(serverCache.guid) },
            onMore = { onMoreServer(serverCache.guid, profile) }
        )
        AppDivider(modifier = Modifier.padding(horizontal = 12.dp))
    }
}

@Composable
fun ServerListItem(
    remarks: String,
    statistics: String,
    typeDescription: String,
    testResult: String,
    testDelayMillis: Long,
    isSelected: Boolean,
    isManaged: Boolean,
    subscriptionRemarks: String,
    doubleColumnDisplay: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onShare: () -> Unit,
    onRemove: () -> Unit,
    onMore: () -> Unit,
    modifier: Modifier = Modifier,
    dragModifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .semantics { selected = isSelected }
            .clickable(role = Role.RadioButton, onClick = onClick)
            .then(dragModifier)
    ) {
        Box(Modifier.width(10.dp).fillMaxHeight()) {
            if (isSelected) {
                Row {
                    Spacer(Modifier.width(6.dp))
                    Box(Modifier.width(4.dp).fillMaxHeight().padding(vertical = 10.dp).background(MaterialTheme.colorScheme.primary))
                }
            }
        }

        Column(Modifier.weight(1f).padding(start = 8.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(remarks, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge.copy(lineBreak = LineBreak.Paragraph), maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (isManaged) {
                    Icon(
                        painter = painterResource(R.drawable.ic_lock_24dp),
                        contentDescription = stringResource(R.string.prosys_managed_config),
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else if (doubleColumnDisplay) {
                    IconButton(onClick = onMore, Modifier.size(48.dp)) {
                        Icon(painterResource(R.drawable.ic_more_vert_24dp), stringResource(R.string.prosys_cd_server_more, remarks), Modifier.size(24.dp))
                    }
                } else {
                    IconButton(onClick = onShare, Modifier.size(48.dp)) { Icon(painterResource(R.drawable.ic_share_24dp), stringResource(R.string.prosys_cd_server_share, remarks), Modifier.size(24.dp)) }
                    IconButton(onClick = onEdit, Modifier.size(48.dp)) { Icon(painterResource(R.drawable.ic_edit_24dp), stringResource(R.string.prosys_cd_server_edit, remarks), Modifier.size(24.dp)) }
                    IconButton(onClick = onRemove, Modifier.size(48.dp)) { Icon(painterResource(R.drawable.ic_delete_24dp), stringResource(R.string.prosys_cd_server_delete, remarks), Modifier.size(24.dp)) }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (subscriptionRemarks.isNotBlank()) {
                    Box(Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)), Alignment.Center) {
                        Text(subscriptionRemarks.take(1).uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
                Text(statistics, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(typeDescription, style = MaterialTheme.typography.bodySmall, color = colorConfigType, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(testResult, style = MaterialTheme.typography.bodySmall, color = if (testDelayMillis < 0L) colorPingRed else colorPing, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun DrawerMenuItem(
    icon: Painter,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(onClick = onClick)
            .background(
                if (selected) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                else Color.Transparent
            )
            .padding(horizontal = 16.dp, vertical = 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun getProtocolDescription(profile: ProfileItem): String {
    if (profile.configType.isComplexType()) return profile.configType.name
    val parts = mutableListOf(profile.configType.name)
    profile.network?.let { net ->
        if (net.isNotBlank() && !net.equals("tcp", ignoreCase = true)) parts.add(net)
    }
    profile.security?.let { sec ->
        if (sec.isNotBlank()) {
            if (profile.insecure == true && sec.equals("tls", ignoreCase = true)) {
                parts.add("$sec insecure")
            } else {
                parts.add(sec)
            }
        }
    }
    return parts.joinToString(" / ")
}
