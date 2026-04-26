# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# 排除 Kotlin 协程调试文件 DebugProbesKt.bin
-dontwarn kotlinx.coroutines.debug.*
-keep class kotlinx.coroutines.debug.DebugProbesKt { *; }

# 如果项目使用 WebView 与 JS，取消以下注释并指定 JavaScript 接口的完全限定类名:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# 取消此注释以保留行号信息用于调试堆栈跟踪
#-keepattributes SourceFile,LineNumberTable

# 如果保留行号信息，取消此注释以隐藏原始源文件名
#-renamesourcefileattribute SourceFile
