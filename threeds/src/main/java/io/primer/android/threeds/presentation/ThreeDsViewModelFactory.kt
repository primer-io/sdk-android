package io.primer.android.threeds.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.threeds.domain.interactor.ThreeDsInteractor

internal class ThreeDsViewModelFactory(
    private val threeDsInteractor: ThreeDsInteractor,
    private val analyticsInteractor: AnalyticsInteractor,
    private val settings: PrimerSettings,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: CreationExtras,
    ): T {
        return ThreeDsViewModel(
            threeDsInteractor,
            analyticsInteractor,
            settings,
        ) as T
    }
}
