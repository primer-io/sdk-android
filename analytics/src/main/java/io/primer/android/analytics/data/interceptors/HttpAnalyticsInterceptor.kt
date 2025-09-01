package io.primer.android.analytics.data.interceptors

import io.primer.android.analytics.data.models.NetworkCallProperties
import io.primer.android.analytics.data.models.NetworkCallType
import io.primer.android.analytics.infrastructure.datasource.connectivity.ConnectivityProvider
import io.primer.android.analytics.infrastructure.datasource.connectivity.toNetworkType
import io.primer.android.core.data.datasource.BaseFlowDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.UUID

internal typealias NetworkCallDataSource = HttpAnalyticsInterceptor

internal class HttpAnalyticsInterceptor(
    private val connectivityProvider: ConnectivityProvider,
) : BaseFlowDataSource<NetworkCallProperties, Unit>, Interceptor {
    private val sharedFlow = MutableStateFlow<NetworkCallProperties?>(null)

    override fun intercept(chain: Interceptor.Chain): Response {
        val id = UUID.randomUUID().toString()
        val request =
            chain.request().newBuilder().addHeader(
                name = X_REQUEST_ID_HEADER,
                value = id,
            ).build()
        sharedFlow.tryEmit(
            NetworkCallProperties(
                callType = NetworkCallType.REQUEST_START,
                id = id,
                url = request.url.toString(),
                method = request.method,
                networkType = connectivityProvider.getNetworkState().toNetworkType(),
            ),
        )

        val response: Response?
        val start: Long = System.currentTimeMillis()
        try {
            response = chain.proceed(request)
            sharedFlow.tryEmit(
                NetworkCallProperties(
                    callType = NetworkCallType.REQUEST_END,
                    id = id,
                    url = request.url.toString(),
                    method = request.method,
                    networkType = connectivityProvider.getNetworkState().toNetworkType(),
                    responseCode = response.code,
                    if (response.isSuccessful) {
                        null
                    } else {
                        response.peekBody(Long.MAX_VALUE).string()
                    },
                    duration = System.currentTimeMillis() - start,
                ),
            )
        } catch (e: IOException) {
            sharedFlow.tryEmit(
                NetworkCallProperties(
                    callType = NetworkCallType.REQUEST_END,
                    id = id,
                    url = request.url.toString(),
                    method = request.method,
                    networkType = connectivityProvider.getNetworkState().toNetworkType(),
                    responseCode = null,
                    errorBody = e.stackTraceToString(),
                    duration = System.currentTimeMillis() - start,
                ),
            )
            throw e
        }

        return response
    }

    override fun execute(input: Unit) = sharedFlow.asStateFlow().filterNotNull()

    private companion object {
        const val X_REQUEST_ID_HEADER = "X-Request-ID"
    }
}
