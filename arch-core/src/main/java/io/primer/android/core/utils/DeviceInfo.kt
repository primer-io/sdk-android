package io.primer.android.core.utils

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

object DeviceInfo {
    @ChecksSdkIntAtLeast(parameter = 0)
    fun isSdkVersionAtLeast(sdkVersion: Int): Boolean = Build.VERSION.SDK_INT >= sdkVersion
}
