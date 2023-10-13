package io.primer.android.threeds.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.threeds.domain.interactor.ThreeDsInteractor

internal class ThreeDsViewModelFactory(
    private val threeDsInteractor: ThreeDsInteractor,
    private val analyticsInteractor: AnalyticsInteractor,
    private val config: PrimerConfig
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return ThreeDsViewModel(
            threeDsInteractor,
            analyticsInteractor,
            config
        ) as T
    }
}
