package io.primer.android.completion

internal class ResumeHandlerFactory(
    private val threeDsResumeHandler: ResumeHandler,
    private val offSessionResumeHandler: ResumeHandler,
    private val defaultResumeHandler: ResumeHandler
) {

    fun getResumeHandler(paymentInstrumentType: String): ResumeHandler {
        return when (paymentInstrumentType) {
            CARD_INSTRUMENT_TYPE -> threeDsResumeHandler
            ASYNC_PAYMENT_METHOD -> offSessionResumeHandler
            else -> defaultResumeHandler
        }
    }

    private companion object {

        const val CARD_INSTRUMENT_TYPE = "PAYMENT_CARD"
        const val ASYNC_PAYMENT_METHOD = "OFF_SESSION_PAYMENT"
    }
}
