package io.primer.android.di

import io.primer.android.di.exception.SdkContainerUninitializedException

internal interface DISdkComponent : SdkComponent {

    override fun getSdkContainer(): SdkContainer = DISdkContext.sdkContainer
        ?: throw SdkContainerUninitializedException()
}
