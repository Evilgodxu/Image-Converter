package com.tpgszhq.jh.update

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tpgszhq.jh.data.repository.UpdateCheckRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 更新弹窗阶段：无 / 发现新版本 / 下载中 / 下载失败 / 待安装
sealed interface UpdatePhase {
    data object Idle : UpdatePhase

    data class Available(val info: UpdateInfo) : UpdatePhase

    data class Downloading(val percent: Int) : UpdatePhase

    data class DownloadFailed(val failure: UpdateDownloadFailure) : UpdatePhase

    data object InstallReady : UpdatePhase
}

// 更新 UI 状态：阶段 + 弹窗显隐（隐藏弹窗不中断下载）
data class UpdateUiState(
    val phase: UpdatePhase = UpdatePhase.Idle,
    val dialogVisible: Boolean = false,
)

// 检查层面的一次性提示：自动检查静默、手动检查以 Toast 反馈
sealed interface UpdateMessage {
    data object UpToDate : UpdateMessage

    data object CheckFailed : UpdateMessage

    data object IncompleteRelease : UpdateMessage
}

// 应用级（Activity 作用域）更新持有者：统一处理每日自动检查、手动检查、下载与安装
class AppUpdateViewModel(
    private val updateCheckRepository: UpdateCheckRepository,
    private val coordinator: UpdateDownloadCoordinator,
    private val installer: ApkInstaller,
    private val appVersion: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UpdateUiState())
    val uiState: StateFlow<UpdateUiState> = _uiState.asStateFlow()

    // 无回放 SharedFlow：自动检查时没有订阅者，提示自然丢弃，避免下次进入设置页误弹
    private val _messages = MutableSharedFlow<UpdateMessage>(extraBufferCapacity = 1)
    val messages: Flow<UpdateMessage> = _messages.asSharedFlow()

    // 待处理的更新包信息，供下载失败后重试
    private var pendingInfo: UpdateInfo? = null
    private var checking = false
    // 本次会话内是否已把安装交给系统安装器：已交付则不再自动弹出待安装对话框
    private var installHandedOff = false

    init {
        viewModelScope.launch {
            coordinator.state.collect { state ->
                when (state) {
                    is UpdateDownloadState.Downloading ->
                        _uiState.update { it.copy(phase = UpdatePhase.Downloading(state.percent)) }

                    is UpdateDownloadState.Completed ->
                        _uiState.update { it.copy(phase = UpdatePhase.InstallReady, dialogVisible = true) }

                    is UpdateDownloadState.Failed ->
                        _uiState.update {
                            it.copy(phase = UpdatePhase.DownloadFailed(state.failure), dialogVisible = true)
                        }

                    UpdateDownloadState.Idle -> Unit
                }
            }
        }
        // 进程被杀后重启：从磁盘恢复待安装态，避免"下载完成"提示丢失
        viewModelScope.launch { coordinator.restorePendingInstall(appVersion) }
    }

    // 进入首页时调用：当天已自动检查过则跳过
    fun checkDailyOnce() {
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            if (updateCheckRepository.lastAutoCheckDate() == today) return@launch
            updateCheckRepository.setLastAutoCheckDate(today)
            check()
        }
    }

    // 设置页手动触发：每次执行并反馈
    fun checkNow() {
        viewModelScope.launch { check() }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(dialogVisible = false) }
    }

    fun startDownload() {
        val info = (_uiState.value.phase as? UpdatePhase.Available)?.info ?: return
        installHandedOff = false
        coordinator.start(info)
    }

    fun retryDownload() {
        pendingInfo?.let(coordinator::start)
    }

    // 交给系统安装器完成安装；应用不介入授权与安装过程
    fun install() {
        viewModelScope.launch {
            val file = coordinator.installableFile(appVersion) ?: return@launch
            installer.install(file)
            installHandedOff = true
            coordinator.clearNotification()
            coordinator.reset()
            _uiState.value = UpdateUiState()
        }
    }

    // 回到前台：恢复磁盘上的待安装包并重新弹出安装确认对话框（不直接触发安装）
    fun onForeground() {
        if (!installHandedOff) {
            viewModelScope.launch { coordinator.restorePendingInstall(appVersion) }
        }
        _uiState.update { state ->
            if (state.phase is UpdatePhase.InstallReady) state.copy(dialogVisible = true) else state
        }
    }

    private suspend fun check() {
        if (checking) return
        checking = true
        try {
            when (val result = AppUpdateChecker.fetch(appVersion)) {
                UpdateCheckResult.UpToDate -> _messages.emit(UpdateMessage.UpToDate)

                UpdateCheckResult.CheckFailed -> _messages.emit(UpdateMessage.CheckFailed)

                UpdateCheckResult.IncompleteRelease -> _messages.emit(UpdateMessage.IncompleteRelease)

                is UpdateCheckResult.Available -> {
                    // 本地已有该版本已校验的安装包时直接进入待安装，避免重复下载
                    if (coordinator.hasPendingInstall(result.info.version)) {
                        _uiState.update {
                            it.copy(phase = UpdatePhase.InstallReady, dialogVisible = true)
                        }
                    } else {
                        pendingInfo = result.info
                        _uiState.update {
                            it.copy(phase = UpdatePhase.Available(result.info), dialogVisible = true)
                        }
                    }
                }
            }
        } finally {
            checking = false
        }
    }
}

// 供界面树消费的 CompositionLocal，由宿主 Activity 提供
val LocalAppUpdateViewModel = staticCompositionLocalOf<AppUpdateViewModel> {
    error("AppUpdateViewModel is not provided")
}
