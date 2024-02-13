package io.primer.android.components.domain.core.models.card

import io.primer.android.ui.CardNetwork

/**
 * Represents a card network with metadata including display name, network type, and whether it is allowed.
 *
 * @property network The type of card network (e.g., VISA, MASTERCARD) represented by an enum from [CardNetwork.Type].
 * @property displayName The human-readable name of the card network (e.g., Visa, Mastercard).
 * @property allowed A boolean indicating whether this card network is allowed.
 */
data class PrimerCardNetwork(
    val network: CardNetwork.Type,
    val displayName: String,
    val allowed: Boolean
)
