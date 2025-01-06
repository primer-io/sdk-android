package io.primer.android.components.currencyformat.domain.interactors

import io.primer.android.components.currencyformat.domain.repository.CurrencyFormatRepository
import io.primer.android.core.domain.BaseSuspendInteractor
import io.primer.android.core.domain.None
import io.primer.android.core.extensions.onError
import io.primer.android.core.logging.internal.LogReporter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class FetchCurrencyFormatDataInteractor(
    private val currencyFormatRepository: CurrencyFormatRepository,
    private val logReporter: LogReporter,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseSuspendInteractor<Unit, None>() {
    override suspend fun performAction(params: None) =
        currencyFormatRepository.fetchCurrencyFormats()
            .onError { throwable ->
                logReporter.error(FETCH_CURRENCY_FORMAT_ERROR, throwable = throwable)
            }.recover { }

    private companion object {
        const val FETCH_CURRENCY_FORMAT_ERROR =
            "Failed to fetch the currency formatting data, due to a failed network call." +
                "Reverting to the fallback."
    }
}
