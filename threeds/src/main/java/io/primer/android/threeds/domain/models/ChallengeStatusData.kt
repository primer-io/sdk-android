package io.primer.android.threeds.domain.models

/**
 * Event data carrier for when the challenge status has changed.
 */
internal data class ChallengeStatusData(
    val paymentMethodToken: String,
    val transactionStatus: String,
) {
    companion object {
        const val TRANSACTION_STATUS_SUCCESS = "Y"
    }
}
