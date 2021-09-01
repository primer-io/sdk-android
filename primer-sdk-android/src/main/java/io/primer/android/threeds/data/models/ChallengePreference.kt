package io.primer.android.threeds.data.models

import kotlinx.serialization.Serializable

@Serializable
internal enum class ChallengePreference {

    NO_PREFERENCE,
    REQUESTED_BY_REQUESTOR,
    REQUESTED_DUE_TO_MANDATE
}
