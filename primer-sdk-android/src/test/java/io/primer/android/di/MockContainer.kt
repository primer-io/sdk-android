package io.primer.android.di

internal class MockContainer : DependencyContainer() {

    override fun registerInitialDependencies() {
        registerSingleton { MockDependency() }

        registerSingleton(DependencyName) { MockDependency() }
    }

    companion object {
        const val DependencyName = "MockName"
    }
}
