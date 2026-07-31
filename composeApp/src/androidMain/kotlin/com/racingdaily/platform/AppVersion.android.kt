package com.racingdaily.platform

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

@Volatile
private var installedVersionName: String? = null

fun initializeAndroidPlatform(context: Context) {
    val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.PackageInfoFlags.of(0)
        )
    } else {
        @Suppress("DEPRECATION")
        context.packageManager.getPackageInfo(context.packageName, 0)
    }
    installedVersionName = packageInfo.versionName
}

actual val appVersionName: String
    get() = checkNotNull(installedVersionName) { "Android platform is not initialized" }
