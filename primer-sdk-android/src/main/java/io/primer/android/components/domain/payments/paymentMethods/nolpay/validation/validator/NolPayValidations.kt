package io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator

internal object NolPayValidations {

    const val INVALID_OTP_CODE_ERROR_ID = "invalid-otp-code"
    val OTP_CODE_REGEX = Regex("^(\\d){6}$")

    const val INVALID_DIALLING_CODE_ERROR_ID = "invalid-phone-country-dialling-code"
    const val INVALID_MOBILE_NUMBER_ERROR_ID = "invalid-mobile-number"
    val DIALLING_CODE_REGEX = Regex("^\\+?\\d{1,3}(-\\d{1,4})?\$")
    val MOBILE_PHONE_REGEX = Regex("^\\d{7,15}\$")

    const val INVALID_CARD_NUMBER_ERROR_ID = "invalid-card-number"
}
