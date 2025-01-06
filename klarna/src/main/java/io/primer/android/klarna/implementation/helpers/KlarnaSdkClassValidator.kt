package io.primer.android.klarna.implementation.helpers

internal class KlarnaSdkClassValidator {
    fun isKlarnaSdkIncluded(): Boolean =
        try {
            Class.forName(KLARNA_CLASS_NAME)
            true
        } catch (ignored: ClassNotFoundException) {
            false
        }

    companion object {
        const val KLARNA_CLASS_NOT_LOADED_ERROR =
            "WARNING!\n" +
                "Klarna configuration has been found but dependency " +
                "'io.primer:klarna-android' is missing. " +
                "Add `io.primer:klarna-android' in your project so you can perform " +
                "payments with Klarna."
        private const val KLARNA_CLASS_NAME = "io.primer.android.klarna.Placeholder"
    }
}
