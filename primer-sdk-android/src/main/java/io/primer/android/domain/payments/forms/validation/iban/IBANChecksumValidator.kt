package io.primer.android.domain.payments.forms.validation.iban

import io.primer.android.domain.payments.forms.validation.Validator
import java.lang.Integer.parseInt
import kotlin.text.StringBuilder

internal class IBANChecksumValidator : Validator {

    override fun validate(input: String?): Boolean {
        val transformedIban = input?.replace("\\s+", "")
        if (transformedIban == null || transformedIban.length < IBAN_MIN_LENGTH) {
            return false
        }
        return try {
            isValidIBANChecksum(transformedIban)
        } catch (t: Throwable) {
            false
        }
    }

    private fun isValidIBANChecksum(iban: String): Boolean {
        // get checksum
        val controlNumber = parseInt(iban.slice(CONTROL_NUMBER_START until CONTROL_NUMBER_END))
        // swap first 2 chars
        val swappedIban = iban.slice(3 until iban.length) + iban.slice(0 until 2) + "00"
        val validationStringBuilder = StringBuilder()
        // convert chars to int
        for (n in 1 until swappedIban.length) {
            val c = swappedIban.codePointAt(n)
            if (c >= 'A'.code) {
                validationStringBuilder.append(c - 55)
            } else {
                validationStringBuilder.append(swappedIban[n])
            }
        }
        var validationString = validationStringBuilder.toString()
        // keep getting module 97 until we get 2 digits
        while (validationString.length > CONTROL_NUMBER_LENGTH) {
            val part = validationString.take(6)
            validationString =
                getModulo97(part).toString() + validationString.substring(part.length)
        }
        return IBAN_REMAINDER - getModulo97(validationString) == controlNumber
    }

    private fun getModulo97(part: String) = parseInt(part) % IBAN_MODULO

    private companion object {
        const val CONTROL_NUMBER_START = 2
        const val CONTROL_NUMBER_END = 4
        const val CONTROL_NUMBER_LENGTH = 2
        const val IBAN_MODULO = 97
        const val IBAN_REMAINDER = 98
        const val IBAN_MIN_LENGTH = 5
    }
}
