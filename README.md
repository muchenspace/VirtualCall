# 虚拟来电 VirtualCall

> 双击音量键，一秒脱身。

一个用于「体面离场」的 Android 工具：在后台常驻一个无障碍服务监听音量键双击事件，触发一次以假乱真的来电界面（含铃声、振动、锁屏唤醒、通话计时、DTMF 拨号盘、接通后固定录音播放），帮你从无聊会议、尴尬约会、沉闷饭局中优雅脱身。

## 功能特性

- **双击音量上键触发**：无需解锁屏幕，任何界面下双击即可触发虚拟来电
- **全屏来电 / 顶部悬浮窗**：两种展示模式可选，悬浮窗更隐蔽
- **锁屏唤醒**：屏幕熄灭或锁屏状态下也能点亮屏幕并显示来电
- **自定义联系人与铃声**：姓名、号码、运营商、来电铃声均可自定义
- **接通后录音播放**：通话接通后循环播放预设录音，模拟真实通话声响
- **逼真通话界面**：滑动接听、通话计时、静音/保持/扬声器、DTMF 拨号盘
- **Slate 冷灰配色**：极简现代风格，统一的视觉语言
- **后台保活**：前台服务 + 开机自启 + 崩溃恢复，尽量不被系统杀死
- **Clean Architecture**：分层清晰、依赖注入、可测试的现代化架构（表现层使用 Presenter 模式）



## 系统要求

- Android 8.0 (API 26) 及以上
- 需开启无障碍服务（用于监听音量键）
- 全屏来电需授予「悬浮窗 / 显示在其他应用上层」权限
- 建议加入电池优化白名单与自启动白名单

## 快速开始

### 构建

```bash
# 需要 JDK 17（Android Studio 自带的 jbr）
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew assembleRelease --no-daemon
```

生成的 APK 位于 `app/build/outputs/apk/release/app-release.apk`。

> 在 Windows 环境下必须使用 Bash（如 Git Bash）执行上述命令，`gradlew.bat` 可能因路径问题失败。

### 首次使用

1. 安装应用并打开
2. 根据引导前往系统设置开启「虚拟来电」无障碍服务
3. 回到应用，点击「启动服务」
4. 双击音量上键即可触发虚拟来电

## 项目结构

```
app/src/main/java/com/muchen/virtualcall/
├── VirtualCallApp.kt                          # @HiltAndroidApp Application 入口，启动时按需恢复服务
├── MainActivity.kt                        # @AndroidEntryPoint, 瘦 Activity
├── VirtualCallActivity.kt                    # @AndroidEntryPoint, 来电全屏 Activity
│
├── data/                                  # Data 层：数据源与 Repository 实现
│   ├── local/
│   │   ├── PrefsDataSource.kt             # SharedPreferences 封装（apply + commit 双通道）
│   │   ├── PrefsKeys.kt                   # 键常量集中管理（含 recording_uri）
│   │   └── VirtualCallSession.kt          # @Singleton 线程安全状态机（IDLE/RINGING/INCALL）
│   └── repository/
│       ├── VirtualCallRepositoryImpl.kt      # 来电状态机实现，委托 VirtualCallSession
│       ├── SettingsRepositoryImpl.kt      # 配置数据读写（含录音 URI）
│       └── SystemRepositoryImpl.kt        # 系统状态查询与跨进程标志管理
│
├── domain/                                # Domain 层：纯 Kotlin，无 Android 依赖
│   ├── model/
│   │   ├── Caller.kt                      # 联系人信息实体（含 carrier、recordingUri）
│   │   ├── CallState.kt                   # 通话状态枚举
│   │   ├── PresentationMode.kt            # 展示模式枚举
│   │   └── ServiceStatus.kt              # 综合服务状态（含 recordingUri、batteryOptimization）
│   ├── repository/
│   │   ├── VirtualCallRepository.kt          # 来电状态机契约
│   │   ├── SettingsRepository.kt          # 配置数据契约（含录音 URI 读写）
│   │   └── SystemRepository.kt            # 系统状态契约
│   ├── service/
│   │   ├── ServiceController.kt           # 前台服务控制契约（抽象 service 启动细节）
│   │   └── RecordingPlayer.kt            # 接通录音播放器契约（抽象 MediaPlayer）
│   ├── usecase/                           # 用例层（单一职责）
│   │   ├── ArmServiceUseCase.kt
│   │   ├── DisarmServiceUseCase.kt
│   │   ├── TriggerVirtualCallUseCase.kt
│   │   ├── AnswerCallUseCase.kt
│   │   ├── EndCallUseCase.kt
│   │   ├── LoadCallerInfoUseCase.kt
│   │   ├── SaveCallerInfoUseCase.kt
│   │   ├── SaveRecordingUriUseCase.kt    # 保存/清除接通录音 URI
│   │   ├── GetServiceStatusUseCase.kt
│   │   ├── SavePresentationModeUseCase.kt
│   │   └── RestoreDefaultsUseCase.kt
│   └── util/
│       └── PhoneFormatter.kt              # 电话号码格式化（3-4-4 分段）
│
├── presentation/                          # Presentation 层：Presenter + Compose UI
│   ├── main/
│   │   ├── MainPresenter.kt              # @Inject, 依赖 9 UseCase + ServiceController + Formatter
│   │   ├── MainEvent.kt                  # 一次性事件（Toast）
│   │   ├── MainUiState.kt                # @Immutable UI 状态（含 recordingLabel/hasRecording）
│   │   ├── MainStatusFormatter.kt        # @Singleton, 状态文本格式化
│   │   ├── MainScreen.kt                 # 主界面 Composable
│   │   └── components/
│   │       ├── TopAppBar.kt
│   │       ├── HeroCard.kt
│   │       ├── PresentationCard.kt
│   │       ├── CustomizeCard.kt          # 含「选择接通录音」入口
│   │       └── PowerCard.kt             # 权限卡片（无障碍→电池→锁屏→自启动）
│   ├── call/
│   │   ├── VirtualCallPresenter.kt        # @Inject, 通话状态 + 计时器 + 录音播放
│   │   ├── VirtualCallUiState.kt            # @Immutable UI 状态
│   │   ├── VirtualCallScreen.kt             # 来电/通话界面 Composable
│   │   ├── DtmfPlayer.kt                    # DTMF 拨号音播放器（封装 ToneGenerator）
│   │   └── components/
│   │       ├── IncomingContent.kt        # 来电滑屏接听/挂断
│   │       ├── InCallContent.kt          # 通话中 3×3 操作网格
│   │       ├── SlideCallButton.kt        # 滑动按钮 + 弹簧动画
│   │       └── DtmfKeypad.kt             # DTMF 拨号盘
│   └── overlay/
│       └── IncomingCallOverlay.kt        # 悬浮窗来电卡片
│
├── service/                               # Android Framework 层（基础设施）
│   ├── VirtualCallService.kt                # @AndroidEntryPoint 前台服务
│   ├── IncomingCallOverlayService.kt     # @AndroidEntryPoint 悬浮窗服务
│   ├── VolumeKeyAccessibilityService.kt  # 无障碍服务（EntryPointAccessors DI）
│   ├── ServiceControllerImpl.kt          # ServiceController 实现，封装 Service 启动细节
│   ├── RecordingPlayerImpl.kt            # RecordingPlayer 实现，封装 MediaPlayer 与音频路由
│   ├── ServiceActions.kt                # Action 常量集中管理
│   ├── notification/
│   │   └── CallNotificationHelper.kt     # @Singleton, 通知渠道/常驻通知/全屏来电通知
│   └── recovery/
│       └── ServiceRecoveryHelper.kt      # @Singleton, 即时恢复 + AlarmManager 延迟重启
│
├── receiver/
│   ├── BootReceiver.kt                   # @AndroidEntryPoint 开机/包更新自启
│   ├── ServiceRestartReceiver.kt         # @AndroidEntryPoint 崩溃恢复
│   └── VolumeKeyReceiver.kt             # @AndroidEntryPoint 音量键广播（备用通道）
│
├── di/                                    # Hilt 依赖注入
│   ├── DataModule.kt                    # Repository / ServiceController / RecordingPlayer @Binds 绑定
│   └── AccessibilityEntryPoint.kt       # 无障碍服务 EntryPoint
│
├── util/
│   ├── ServiceStarter.kt                # 安全服务启动包装器
│   ├── ServiceComposeOwner.kt           # Service ComposeView 生命周期管理
│   └── RingtonePlayer.kt                # 铃声与振动统一播放
│
└── ui/                                    # 共享 UI 层
    ├── theme/
    │   ├── Color.kt                     # Slate 配色系统 + AppThemeColors
    │   └── Theme.kt                     # Compose 主题入口（主界面/来电/悬浮窗）
    └── components/
        ├── StatusBadge.kt
        ├── PulsingDot.kt
        ├── GlassCard.kt
        ├── SectionCard.kt
        ├── SettingRow.kt
        ├── Buttons.kt
        └── RadioOption.kt
```

## 技术栈

- **Kotlin** 2.0.21
- **AGP** 8.13.2 / **Gradle** 9.3.0
- **Jetpack Compose** (BOM 2024.12.01) + Material 3
- **Hilt** 2.51.1（依赖注入，含 KSP 编译器处理）
- **StateFlow** + UiState 模式（响应式状态管理）
- **版本目录** (gradle/libs.versions.toml) 统一依赖管理
- **AndroidX** (Core / Activity / Lifecycle / SavedState)
- **最低 SDK** 26 / **目标 SDK** 36 / **编译 SDK** 36

## 核心架构

采用 **Clean Architecture** 分层架构，通过 Hilt 实现依赖反转，表现层使用 **Presenter 模式**：

- **Domain 层**（纯 Kotlin）：定义业务实体、Repository 接口、服务抽象（`ServiceController` / `RecordingPlayer`）和 UseCase，不依赖任何 Android 组件
- **Data 层**：实现 Repository 接口，封装 SharedPreferences 和系统 API 调用
- **Service 层**：实现 Domain 层服务抽象（`ServiceControllerImpl` / `RecordingPlayerImpl`），封装 Android Framework 细节
- **Presentation 层**：`Presenter` + `StateFlow<UiState>` + Compose，Activity 持有 Presenter 实例（`@Inject`），**仅依赖 Domain 层抽象**
- **DI 层**：Hilt 模块绑定接口与实现，受限组件（AccessibilityService）使用 EntryPointAccessors

依赖方向严格遵循 Clean Architecture：`presentation → domain ← data/service`，内层不依赖外层。

详细架构说明见 [docs/架构说明.md](docs/架构说明.md)。

### 触发链路

```
双击音量上键（700ms 内两次按下）
  → VolumeKeyAccessibilityService.onKeyEvent (独立进程 :accessibility)
  → 检测双击 + 检查 armed 标志
  → safeStartForegroundService(ACTION_TRIGGER_CALL)
  → VirtualCallService.triggerVirtualCall()
  → 检查 armed → tryMarkRinging → 读取 PresentationMode
  → 根据 mode 启动 VirtualCallActivity 或 IncomingCallOverlayService
  → Activity 启动失败时回退到全屏来电通知（CallNotificationHelper）
```

> 无障碍服务在 `onServiceConnected` / `onUnbind` 中自动维护 armed 标志，并拉起 / 停止 `VirtualCallService`。

### 服务保活

- **前台服务**：`VirtualCallService` 以 `specialUse` 类型常驻通知栏
- **崩溃恢复**：`ServiceRecoveryHelper` 在 `onDestroy` / `onTaskRemoved` 时立即拉起 + 通过 `AlarmManager` 延迟 1.5s 重启
- **开机自启**：`BootReceiver` 监听 `BOOT_COMPLETED` / `LOCKED_BOOT_COMPLETED` / `MY_PACKAGE_REPLACED`
- **应用启动恢复**：`VirtualCallApp.onCreate()` 检查 armed 标志，若已武装则拉起服务
- **armed 标志**：由无障碍服务的 `onServiceConnected` / `onUnbind` 自动维护，跨进程使用 `commit()` 写入确保可见性
- **停止仅解除武装**：用户主动停止服务时仅置 armed=false 并刷新通知，**不杀死前台服务进程**，通知栏持续显示状态



## 权限说明

| 权限 | 用途 |
|---|---|
| `FOREGROUND_SERVICE` | 保持虚拟来电服务常驻 |
| `FOREGROUND_SERVICE_SPECIAL_USE` | 前台服务类型（虚拟来电生命周期管理） |
| `POST_NOTIFICATIONS` | 显示服务状态与来电通知 |
| `SCHEDULE_EXACT_ALARM` | AlarmManager 精确闹钟用于崩溃恢复 |
| `USE_FULL_SCREEN_INTENT` | 锁屏时全屏显示来电 |
| `SYSTEM_ALERT_WINDOW` | 顶部悬浮窗来电 |
| `VIBRATE` | 来电振动 |
| `WAKE_LOCK` | 唤醒屏幕 |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | 引导加入电池白名单 |
| `RECEIVE_BOOT_COMPLETED` | 开机自启 |
| `BIND_ACCESSIBILITY_SERVICE` | 监听音量键（系统绑定，非主动申请） |

> 无障碍服务**仅监听音量键事件**，不读取屏幕内容、不收集任何数据。

## 许可证

[MIT License](LICENSE)
