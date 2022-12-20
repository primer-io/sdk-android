package io.primer.android.payment.async.ipay88.helpers

internal class IPay88SdkClassValidator {

    fun isIPaySdkIncluded(): Boolean {
        return try {
            Class.forName(I_PAY_CLASS_NAME)
            true
        } catch (ignored: ClassNotFoundException) {
            false
        }
    }

    companion object {

        const val I_PAY_CLASS_NOT_LOADED_ERROR =
            "WARNING!\n" +
                "iPay88 card configuration has been found but dependency " +
                "'io.primer:ipay88-android' is missing. " +
                "Add `io.primer:ipa88-android' in your project so you can perform " +
                "payments with iPay88."
        private const val I_PAY_CLASS_NAME = "io.primer.ipay88.api.ui.NativeIPay88Activity"
    }
}
