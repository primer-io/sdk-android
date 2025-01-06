package io.primer.android.core.utils

import kotlinx.coroutines.flow.MutableStateFlow

fun interface EventFlowProvider<T> {
    fun getEventProvider(): MutableStateFlow<T?>
}
