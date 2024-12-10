package io.primer.android.analytics.data.datasource

import io.primer.android.analytics.data.models.MessageProperties
import io.primer.android.core.data.datasource.BaseFlowDataSource
import io.primer.android.core.utils.EventFlowProvider
import kotlinx.coroutines.flow.filterNotNull

internal class MessagePropertiesDataSource(
    private val messagePropertiesEventProvider: EventFlowProvider<MessageProperties>
) : BaseFlowDataSource<MessageProperties, Unit> {

    override fun execute(input: Unit) = messagePropertiesEventProvider.getEventProvider().filterNotNull()
}
