package io.primer.android.analytics.data.datasource

import io.primer.android.analytics.data.helper.MessagePropertiesEventProvider
import io.primer.android.analytics.data.models.MessageProperties
import io.primer.android.data.base.datasource.BaseFlowDataSource
import kotlinx.coroutines.flow.filterNotNull

internal class MessagePropertiesDataSource(private val messagePropertiesEventProvider: MessagePropertiesEventProvider) :
    BaseFlowDataSource<MessageProperties, Unit> {

    override fun execute(input: Unit) = messagePropertiesEventProvider.getMessageEventProvider().filterNotNull()
}
