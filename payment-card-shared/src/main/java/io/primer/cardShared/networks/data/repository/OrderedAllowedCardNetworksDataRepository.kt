package io.primer.cardShared.networks.data.repository

import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.cardShared.networks.domain.repository.OrderedAllowedCardNetworksRepository

class OrderedAllowedCardNetworksDataRepository(
    private val configurationDataSource: CacheConfigurationDataSource,
) : OrderedAllowedCardNetworksRepository {
    override fun getOrderedAllowedCardNetworks(): List<CardNetwork.Type> {
        return configurationDataSource.get().clientSession
            .paymentMethod?.orderedAllowedCardNetworks.orEmpty()
    }
}
