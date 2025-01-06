package io.primer.android.di

import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.ui.fragments.processorTest.ProcessorTestResultSelectorViewModelFactory

internal class ProcessorTestContainer(private val sdk: SdkContainer) : DependencyContainer() {
    override fun registerInitialDependencies() {
        registerFactory {
            ProcessorTestResultSelectorViewModelFactory(sdk.resolve())
        }
    }
}
