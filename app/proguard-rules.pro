-keep class com.muchen.virtualcall.VirtualCallApp { *; }

-keep class com.muchen.virtualcall.service.VirtualCallService { *; }
-keep class com.muchen.virtualcall.service.IncomingCallOverlayService { *; }
-keep class com.muchen.virtualcall.service.VolumeKeyAccessibilityService { *; }
-keep class com.muchen.virtualcall.service.recovery.ServiceWatchdogJobService { *; }

-keep class com.muchen.virtualcall.receiver.BootReceiver { *; }
-keep class com.muchen.virtualcall.receiver.ServiceRestartReceiver { *; }

-keep class com.muchen.virtualcall.domain.model.** { *; }

-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
}
