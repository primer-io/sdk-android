package io.primer.android.completion

internal class ResumeHandlerFactory(
    private val threeDsResumeHandler: ResumeHandler,
    private val defaultResumeHandler: ResumeHandler
) {

    fun getResumeHandler(paymentInstrumentType: String): ResumeHandler {
        return when (paymentInstrumentType) {
            CARD_INSTRUMENT_TYPE -> threeDsResumeHandler
            else -> defaultResumeHandler
        }
    }

    private companion object {

        const val CARD_INSTRUMENT_TYPE = "PAYMENT_CARD"
    }
}
