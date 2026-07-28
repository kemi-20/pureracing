package com.racingdaily.platform

actual val appVersionName: String
    get() = System.getProperty("jpackage.app-version").orEmpty().ifBlank { "Development" }
