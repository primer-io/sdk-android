package io.primer.android.analytics.data.helper

import io.primer.android.analytics.data.models.MessageProperties
import kotlinx.coroutines.flow.MutableStateFlow

internal class MessagePropertiesEventProvider {
    private val sharedFlow = MutableStateFlow<MessageProperties?>(null)

    fun getMessageEventProvider() = sharedFlow
}
