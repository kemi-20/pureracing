package com.racingdaily.ui.components.util

actual suspend fun awaitFrame() {
    kotlinx.coroutines.android.awaitFrame()
}
