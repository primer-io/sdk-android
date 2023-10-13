package io.primer.android.ui.fragments.dummy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import io.primer.android.analytics.domain.AnalyticsInteractor

internal class DummyResultSelectorViewModelFactory(
    private val analyticsInteractor: AnalyticsInteractor
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return DummyResultSelectorViewModel(
            analyticsInteractor
        ) as T
    }
}
