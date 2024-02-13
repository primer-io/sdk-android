package io.primer.android.components.data.payments.paymentMethods.raw.repository.card

import io.primer.android.components.domain.payments.paymentMethods.raw.repository.card.OrderedAllowedCardNetworksRepository
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.ui.CardNetwork

internal class OrderedAllowedCardNetworksDataRepository(
    private val localConfigurationDataSource: LocalConfigurationDataSource
) : OrderedAllowedCardNetworksRepository {
    override fun getOrderedAllowedCardNetworks(): List<CardNetwork.Type> {
        return localConfigurationDataSource.getConfiguration().clientSession
            .paymentMethod?.orderedAllowedCardNetworks.orEmpty()
    }
}
