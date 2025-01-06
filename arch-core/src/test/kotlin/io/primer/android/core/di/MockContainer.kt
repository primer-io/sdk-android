package io.primer.android.core.di

internal class MockContainer : DependencyContainer() {
    override fun registerInitialDependencies() {
        registerSingleton { MockDependency() }

        registerSingleton(DEPENDENCY_NAME) { MockDependency() }
    }

    companion object {
        const val DEPENDENCY_NAME = "MockName"
    }
}
