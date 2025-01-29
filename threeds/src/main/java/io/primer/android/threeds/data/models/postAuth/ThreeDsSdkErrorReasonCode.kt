package io.primer.android.threeds.data.models.postAuth

@Suppress("EnumEntryName", "EnumNaming", "EnumEntryNameCase")
internal enum class ThreeDsSdkErrorReasonCode {
    MISSING_SDK_DEPENDENCY,
    INVALID_3DS_SDK_VERSION,
    MISSING_3DS_CONFIGURATION,
    `3DS_SDK_INIT_FAILED`,
    MISSING_DS_RID,
    UNKNOWN_PROTOCOL_VERSION,
    INVALID_CHALLENGE_STATUS,
    CHALLENGE_CANCELLED_BY_USER,
    CHALLENGE_TIMED_OUT,
    `3DS_SDK_RUNTIME_ERROR`,
    `3DS_SDK_PROTOCOL_ERROR`,
    `3DS_UNKNOWN_ERROR`,
}
