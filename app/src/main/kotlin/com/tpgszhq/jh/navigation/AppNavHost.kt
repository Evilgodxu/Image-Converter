package com.tpgszhq.jh.navigation

import android.net.Uri
import android.os.SystemClock
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.tpgszhq.jh.R
import com.tpgszhq.jh.screens.home.HomeScreen
import com.tpgszhq.jh.screens.privacy.PrivacyScreen
import com.tpgszhq.jh.screens.settings.SettingsScreen

// 双击退出确认窗口
private const val EXIT_CONFIRM_WINDOW_MS = 2000L

// 导航宿主：统一走路由栈。起始页由调用方决定（首启未同意隐私政策时为隐私页）
@Composable
fun AppNavHost(
    startKey: NavKey,
    externalImageUris: List<Uri>,
    onExitApp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backStack = rememberNavBackStack(startKey)
    val context = LocalContext.current
    var lastRootBackAt by rememberSaveable { mutableLongStateOf(0L) }
    val exitHint = stringResource(R.string.press_back_again_to_exit)

    // 页面返回：只在非根页面弹栈（NavDisplay 的系统返回回调与设置页顶栏返回按钮共用）
    val goBack: () -> Unit = {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
    }

    // 根页面（首页/隐私页）的系统返回：双击退出。
    // NavDisplay 内置返回处理仅在场景有上级条目（previousEntries 非空）时启用，
    // 根页面不会回调 onBack，因此退出逻辑必须在此单独拦截，否则按一次返回即退出。
    BackHandler(enabled = backStack.size == 1) {
        val now = SystemClock.uptimeMillis()
        if (now - lastRootBackAt <= EXIT_CONFIRM_WINDOW_MS) {
            onExitApp()
        } else {
            lastRootBackAt = now
            Toast.makeText(context, exitHint, Toast.LENGTH_SHORT).show()
        }
    }

    NavDisplay(
        backStack = backStack,
        onBack = goBack,
        modifier = modifier,
        // 保留默认的可保存状态装饰器，并叠加 ViewModel 作用域装饰器，
        // 使页面级 ViewModel 的生命周期与导航条目对齐
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = { key ->
            when (key) {
                is Home -> NavEntry(key) {
                    HomeScreen(
                        externalImageUris = externalImageUris,
                        onOpenSettings = { backStack.add(Settings) },
                    )
                }

                is Settings -> NavEntry(key) {
                    SettingsScreen(onBack = goBack)
                }

                is Privacy -> NavEntry(key) {
                    PrivacyScreen(
                        // 同意后以首页替换隐私页，返回栈中不再保留隐私页
                        onAccepted = {
                            backStack.clear()
                            backStack.add(Home)
                        },
                        onRejected = onExitApp,
                    )
                }

                else -> error("Unknown NavKey: $key")
            }
        },
    )
}
