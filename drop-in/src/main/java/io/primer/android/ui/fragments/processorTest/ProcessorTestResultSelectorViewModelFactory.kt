package io.primer.android.ui.fragments.processorTest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import io.primer.android.analytics.domain.AnalyticsInteractor

internal class ProcessorTestResultSelectorViewModelFactory(
    private val analyticsInteractor: AnalyticsInteractor
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return ProcessorTestResultSelectorViewModel(
            analyticsInteractor
        ) as T
    }
}
