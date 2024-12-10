package io.primer.cardShared.networks.domain.repository

import io.primer.android.configuration.data.model.CardNetwork

fun interface OrderedAllowedCardNetworksRepository {

    fun getOrderedAllowedCardNetworks(): List<CardNetwork.Type>
}
