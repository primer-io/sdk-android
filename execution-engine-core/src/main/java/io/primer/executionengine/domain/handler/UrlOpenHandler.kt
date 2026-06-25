package io.primer.executionengine.domain.handler

import io.primer.executionengine.domain.executor.ComponentEventSource
import kotlinx.coroutines.flow.SharedFlow

data class UrlOpenLaunchRequest(
    val url: String,
    val redirectUrls: List<String>?,
)

interface UrlOpenHandler : ComponentEventSource {
    val launchRequest: SharedFlow<UrlOpenLaunchRequest>
    fun launch(url: String, redirectUrls: List<String>?)
    fun onResultOk()
    fun onResultCancelled()
    fun onResultError(uri: String)
}
