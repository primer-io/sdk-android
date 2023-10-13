package io.primer.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.deeplink.async.AsyncPaymentMethodDeeplinkInteractor
import io.primer.android.domain.tokenization.TokenizationInteractor

internal class TokenizationViewModelFactory(
    private val config: PrimerConfig,
    private val tokenizationInteractor: TokenizationInteractor,
    private val asyncPaymentMethodDeeplinkInteractor: AsyncPaymentMethodDeeplinkInteractor
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return TokenizationViewModel(
            config,
            tokenizationInteractor,
            asyncPaymentMethodDeeplinkInteractor
        ) as T
    }
}
