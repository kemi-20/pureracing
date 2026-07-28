package com.racingdaily.platform

expect val appVersionName: String

val appVersionLabel: String
    get() {
        val parts = appVersionName.split('.')
        return if (parts.size == 3 && parts.last() == "0") {
            parts.take(2).joinToString(".")
        } else {
            appVersionName
        }
    }
