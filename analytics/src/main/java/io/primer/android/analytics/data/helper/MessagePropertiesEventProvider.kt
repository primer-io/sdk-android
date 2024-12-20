package io.primer.android.analytics.data.helper

import io.primer.android.core.data.network.helpers.MessagePropertiesHelper
import io.primer.android.core.utils.EventFlowProvider
import kotlinx.coroutines.flow.MutableStateFlow

internal class MessagePropertiesEventProvider : EventFlowProvider<MessagePropertiesHelper> {
    private val sharedFlow = MutableStateFlow<MessagePropertiesHelper?>(null)

    override fun getEventProvider(): MutableStateFlow<MessagePropertiesHelper?> = sharedFlow
}
