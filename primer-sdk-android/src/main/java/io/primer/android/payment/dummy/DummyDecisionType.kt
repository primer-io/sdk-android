package io.primer.android.payment.dummy

import kotlinx.serialization.Serializable

@Serializable
internal enum class DummyDecisionType {
    SUCCESS, DECLINE, FAIL
}
