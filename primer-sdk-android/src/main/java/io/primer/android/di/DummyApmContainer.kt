package io.primer.android.di

import io.primer.android.ui.fragments.dummy.DummyResultSelectorViewModelFactory

internal class DummyApmContainer(private val sdk: SdkContainer) : DependencyContainer() {

    override fun registerInitialDependencies() {
        registerFactory {
            DummyResultSelectorViewModelFactory(sdk.resolve())
        }
    }
}
