package io.primer.android

internal enum class SessionState {
    AWAITING_APP,
    AWAITING_API,
    AWAITING_USER,
    DONE,
    ERROR,
    ;

    val showLoadingUi: Boolean
        get() = this != AWAITING_USER && this != ERROR && this != DONE
}
