package io.primer.android.analytics.data.interceptors

import io.primer.android.analytics.data.models.NetworkCallProperties
import io.primer.android.analytics.data.models.NetworkCallType
import io.primer.android.data.base.datasource.BaseFlowDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.UUID

internal typealias NetworkCallDataSource = HttpAnalyticsInterceptor

internal class HttpAnalyticsInterceptor :
    BaseFlowDataSource<NetworkCallProperties, Unit>, Interceptor {

    private val sharedFlow = MutableStateFlow<NetworkCallProperties?>(null)

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val id = UUID.randomUUID().toString()
        sharedFlow.tryEmit(
            NetworkCallProperties(
                NetworkCallType.REQUEST_START,
                id,
                request.url().toString(),
                request.method()
            )
        )

        val response: Response?
        try {
            response = chain.proceed(request)
            sharedFlow.tryEmit(
                NetworkCallProperties(
                    NetworkCallType.REQUEST_END,
                    id,
                    request.url().toString(),
                    request.method(),
                    response.code(),
                    if (response?.isSuccessful == true) null else response.peekBody(Long.MAX_VALUE)
                        .string()
                )
            )
        } catch (e: IOException) {
            sharedFlow.tryEmit(
                NetworkCallProperties(
                    NetworkCallType.REQUEST_END,
                    id,
                    request.url().toString(),
                    request.method(),
                    null,
                    e.stackTraceToString()
                )
            )
            throw e
        }

        return response
    }

    override fun execute(input: Unit) = sharedFlow.asStateFlow().filterNotNull()
}
