package io.primer.android.threeds.domain.models

/**
 * Event data carrier for when the challenge status has changed.
 */
internal class ChallengeStatusData constructor(
    val sdkTransId: String,
    val transactionStatus: String,
) {

    companion object {

        const val TRANSACTION_STATUS_SUCCESS = "Y"
        const val TRANSACTION_STATUS_FAILURE = "N"
    }
}
