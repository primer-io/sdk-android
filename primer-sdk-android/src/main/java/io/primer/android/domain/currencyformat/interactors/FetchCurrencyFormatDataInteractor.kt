package io.primer.android.domain.currencyformat.interactors

import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.domain.base.BaseSuspendInteractor
import io.primer.android.domain.base.None
import io.primer.android.domain.currencyformat.repository.CurrencyFormatRepository
import io.primer.android.extensions.onError
import io.primer.android.extensions.runSuspendCatching
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class FetchCurrencyFormatDataInteractor(
    private val currencyFormatRepository: CurrencyFormatRepository,
    private val logReporter: LogReporter,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseSuspendInteractor<Unit, None>() {

    override suspend fun performAction(params: None) = runSuspendCatching {
        currencyFormatRepository.fetchCurrencyFormats().getOrThrow()
    }.onError { throwable ->
        logReporter.error(FETCH_CURRENCY_FORMAT_ERROR, throwable = throwable)
        throwable
    }

    private companion object {
        const val FETCH_CURRENCY_FORMAT_ERROR =
            "Failed to fetch the currency formatting data, due to a failed network call." +
                "Reverting to the fallback."
    }
}
