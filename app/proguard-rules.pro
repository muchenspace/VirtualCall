# ============================================================================
# VirtualCall 虚拟来电 - ProGuard / R8 规则
# ============================================================================
# R8 full mode 已在 release buildType 中启用（isMinifyEnabled = true）。
# Compose 已自带 consumer-rules，无需手动保留 Compose 内部类。
# Hilt 自带 consumer-rules，生成的类（_HiltInject, _Factory 等）自动保留。
# ============================================================================

# --- 入口类：Activity / Service / Receiver 在 Manifest 中声明，
#     R8 会自动保留，但显式声明更安全 ---

-keep class com.muchen.virtualcall.MainActivity { *; }
-keep class com.muchen.virtualcall.VirtualCallActivity { *; }
-keep class com.muchen.virtualcall.VirtualCallApp { *; }

-keep class com.muchen.virtualcall.service.** { *; }
-keep class com.muchen.virtualcall.receiver.** { *; }

# --- 无障碍服务：系统通过反射实例化 ---
-keep class com.muchen.virtualcall.service.VolumeKeyAccessibilityService { *; }

# --- Domain 模型：被 Compose 使用，保留字段名避免 @Immutable 失效 ---
-keep class com.muchen.virtualcall.domain.model.** { *; }

# --- Hilt @Inject 构造函数：R8 需保留构造函数供 Hilt 运行时使用 ---
-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
}

# --- Kotlin 协程：R8 已内置规则，无需额外配置 ---

# --- SharedPreferences：键值对读写不依赖反射，无需保留 ---
