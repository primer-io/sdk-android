package io.primer.android.components.domain.payments.paymentMethods.raw.repository.card

import io.primer.android.ui.CardNetwork

internal fun interface OrderedAllowedCardNetworksRepository {

    fun getOrderedAllowedCardNetworks(): List<CardNetwork.Type>
}
