package io.primer.android.analytics.extensions

private const val KB_IN_MB = 1048576L

internal fun Runtime.getMemoryUsage(): Long {
    val usedMemInMB = (totalMemory() - freeMemory()) / KB_IN_MB
    val maxHeapSizeInMB = maxMemory() / KB_IN_MB
    return maxHeapSizeInMB - usedMemInMB
}
