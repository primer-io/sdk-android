package io.primer.android.analytics.data.datasource

import io.primer.android.analytics.data.extensions.toMessageType
import io.primer.android.analytics.data.extensions.toSeverity
import io.primer.android.analytics.data.models.MessageProperties
import io.primer.android.core.data.datasource.BaseFlowDataSource
import io.primer.android.core.data.network.helpers.MessagePropertiesHelper
import io.primer.android.core.utils.EventFlowProvider
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

internal class MessagePropertiesDataSource(
    private val messagePropertiesEventProvider: EventFlowProvider<MessagePropertiesHelper>
) : BaseFlowDataSource<MessageProperties, Unit> {

    override fun execute(input: Unit) = messagePropertiesEventProvider.getEventProvider().filterNotNull()
        .map { messagePropertiesHelper ->
            MessageProperties(
                message = messagePropertiesHelper.message,
                messageType = messagePropertiesHelper.messageTypeHelper.toMessageType(),
                severity = messagePropertiesHelper.severity.toSeverity()
            )
        }
}
