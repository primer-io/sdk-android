package io.primer.android.otp.implementation.errors.data.exception

import io.primer.android.errors.data.exception.IllegalValueKey

internal enum class OtpIllegalValueKey(override val key: String) : IllegalValueKey {
    OTP_DATA("otpData"),
}
