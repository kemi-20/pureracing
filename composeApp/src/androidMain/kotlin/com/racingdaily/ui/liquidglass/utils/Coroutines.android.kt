package com.racingdaily.ui.liquidglass.utils

actual suspend fun awaitFrame() {
    kotlinx.coroutines.android.awaitFrame()
}
