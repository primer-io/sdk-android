package io.primer.android.viewmodel

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner

interface ViewModelAssistedFactory<T : ViewModel> {

    fun create(handle: SavedStateHandle): T
}

class GenericSavedStateViewModelFactory<out V : ViewModel>(
    private val viewModelFactory: ViewModelAssistedFactory<V>,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null,
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle,
    ): T {
        return viewModelFactory.create(handle) as T
    }
}

/* TODO manual di with/for fragments
typealias ActivityViewModelFactoryProvider = (Fragment) -> ViewModelProvider.Factory

val activityViewModelFactoryProvider: ActivityViewModelFactoryProvider = {
    it.requireActivity().defaultViewModelProviderFactory
}

internal inline fun <reified VM : ViewModel> Fragment.viewModelBuilder(
    useActivityStore: Boolean = false,
    noinline viewModelInitializer: () -> VM,
): Lazy<VM> {
    return ViewModelLazy(
        viewModelClass = VM::class,
        storeProducer = {
            if (useActivityStore) requireActivity().viewModelStore else viewModelStore
        },
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return viewModelInitializer.invoke() as T
                }
            }
        }
    )
}
*/
