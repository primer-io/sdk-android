package io.primer.android.nolpay.implementation.helpers

internal class NolPaySdkClassValidator {

    fun isSdkIncluded(): Boolean {
        return try {
            Class.forName(NOL_PAY_CLASS_NAME)
            true
        } catch (ignored: ClassNotFoundException) {
            false
        }
    }

    companion object {

        const val NOL_PAY_CLASS_NOT_LOADED_ERROR =
            "Nol pay configuration has been found but dependency " +
                "'io.primer:nol-pay-android is missing. " +
                "Add `io.primer:nol-pay-android' in your project so you can perform " +
                "payments with Nol pay`."
        private const val NOL_PAY_CLASS_NAME = "io.primer.nolpay.api.PrimerNolPay"
    }
}
