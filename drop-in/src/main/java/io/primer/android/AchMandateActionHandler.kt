package io.primer.android

internal fun interface AchMandateActionHandler {
    suspend fun handleAchMandateAction(isAccepted: Boolean)
}
