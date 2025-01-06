package io.primer.android.core.di

interface DISdkComponent : SdkComponent {
    override fun getSdkContainer(): SdkContainer {
        return DISdkContext.container()
    }
}
