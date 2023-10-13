package io.primer.android.di

import io.primer.android.ui.fragments.bancontact.BancontactCardViewModelFactory

internal class BancontactApmContainer(private val sdk: SdkContainer) : DependencyContainer() {

    override fun registerInitialDependencies() {
        registerFactory { BancontactCardViewModelFactory(sdk.resolve()) }
    }
}
