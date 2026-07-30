package com.racingdaily.util

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

class SuspendResultTest {
    @Test
    fun cancellationIsNeverConvertedToFailureResult() = runTest {
        assertFailsWith<CancellationException> {
            runSuspendCatching<Unit> { throw CancellationException("cancelled") }
        }
    }

    @Test
    fun fatalErrorsAreNeverConvertedToFailureResult() = runTest {
        assertFailsWith<AssertionError> {
            runSuspendCatching<Unit> { throw AssertionError("fatal") }
        }
    }
}
