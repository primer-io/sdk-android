package io.primer.android.threeds.data.models.common

@Suppress("EnumEntryName", "EnumNaming", "EnumEntryNameCase")
internal enum class SkippedCode {
    GATEWAY_UNAVAILABLE,
    DISABLED_BY_MERCHANT,
    NOT_SUPPORTED_BY_ISSUER,
    FAILED_TO_NEGOTIATE,
    UNKNOWN_ACS_RESPONSE,
    `3DS_SERVER_ERROR`,
    ACQUIRER_NOT_CONFIGURED,
    ACQUIRER_NOT_PARTICIPATING,
}
