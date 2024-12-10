package io.primer.android.analytics.data.helper

import io.primer.android.analytics.data.models.MessageProperties
import io.primer.android.core.utils.EventFlowProvider
import kotlinx.coroutines.flow.MutableStateFlow

internal class MessagePropertiesEventProvider : EventFlowProvider<MessageProperties> {
    private val sharedFlow = MutableStateFlow<MessageProperties?>(null)

    override fun getEventProvider(): MutableStateFlow<MessageProperties?> = sharedFlow
}
