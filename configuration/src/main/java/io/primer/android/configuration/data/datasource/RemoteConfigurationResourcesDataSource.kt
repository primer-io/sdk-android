package io.primer.android.configuration.data.datasource

import io.primer.android.analytics.data.models.TimerId
import io.primer.android.analytics.data.models.TimerProperties
import io.primer.android.analytics.data.models.TimerType
import io.primer.android.analytics.data.models.UrlAnalyticsContext
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.infrastructure.FileProvider
import io.primer.android.core.data.network.extensions.await
import io.primer.android.core.utils.EventFlowProvider
import io.primer.android.displayMetadata.domain.model.IconDisplayMetadata
import io.primer.android.displayMetadata.domain.model.ImageColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedInputStream
import java.io.FileOutputStream

class RemoteConfigurationResourcesDataSource(
    private val okHttpClient: OkHttpClient,
    private val imagesFileProvider: FileProvider,
    private val timerEventProvider: EventFlowProvider<TimerProperties>,
) : BaseSuspendDataSource<
        List<Map<String, List<IconDisplayMetadata>>>,
        List<PaymentMethodConfigDataResponse>,
        > {
    override suspend fun execute(input: List<PaymentMethodConfigDataResponse>) =
        coroutineScope {
            logAnalyticsAllImageDurationTimerEvent(TimerType.START)
            val iconsMetadata =
                input.map { config ->
                    val iconUrl = config.displayMetadata?.buttonData?.iconUrl
                    listOfNotNull(
                        iconUrl?.colored?.to(ImageColor.COLORED),
                        iconUrl?.dark?.to(ImageColor.DARK),
                        iconUrl?.light?.to(ImageColor.LIGHT),
                    ).map { urlColor ->
                        async(Dispatchers.IO) {
                            withTimeoutOrNull(DEFAULT_REQUEST_TIMEOUT_MILLIS) {
                                logAnalyticsImageLoadingTimerEvent(
                                    TimerType.START,
                                    config.type,
                                    urlColor.first,
                                )

                                val request =
                                    Request.Builder()
                                        .url(urlColor.first)
                                        .get()
                                        .build()
                                try {
                                    okHttpClient
                                        .newCall(request)
                                        .await().use {
                                            logAnalyticsImageLoadingTimerEvent(
                                                TimerType.END,
                                                config.type,
                                                urlColor.first,
                                            )
                                            if (it.isSuccessful) {
                                                val bufferedInputStream =
                                                    BufferedInputStream(it.body?.byteStream())
                                                val file =
                                                    imagesFileProvider.getFile(
                                                        "${config.type}_${urlColor.second}".lowercase(),
                                                    )

                                                FileOutputStream(file).use { outputStream ->
                                                    outputStream.write(bufferedInputStream.readBytes())
                                                    outputStream.flush()
                                                    IconDisplayMetadata(
                                                        imageColor = urlColor.second,
                                                        url = urlColor.first,
                                                        filePath = file.absolutePath,
                                                    )
                                                }
                                            } else {
                                                IconDisplayMetadata(
                                                    imageColor = urlColor.second,
                                                    url = urlColor.first,
                                                )
                                            }
                                        }
                                } catch (_: Exception) {
                                    IconDisplayMetadata(
                                        imageColor = urlColor.second,
                                        url = urlColor.first,
                                    )
                                }
                            } ?: IconDisplayMetadata(
                                imageColor = urlColor.second,
                                url = urlColor.first,
                            )
                        }
                    }.ifEmpty {
                        listOfNotNull(
                            iconUrl?.colored?.to(
                                IconDisplayMetadata(
                                    imageColor = ImageColor.COLORED,
                                ),
                            ),
                            iconUrl?.dark?.to(
                                IconDisplayMetadata(
                                    imageColor = ImageColor.DARK,
                                ),
                            ),
                            iconUrl?.light?.to(
                                IconDisplayMetadata(
                                    imageColor = ImageColor.LIGHT,
                                ),
                            ),
                        ).map { it.second }.map {
                            coroutineScope {
                                async { it }
                            }
                        }
                    }
                }.map { it.awaitAll() }.mapIndexed { index, list ->
                    mapOf(input[index].type to list)
                }

            logAnalyticsAllImageDurationTimerEvent(TimerType.END)
            iconsMetadata
        }

    private fun logAnalyticsImageLoadingTimerEvent(
        timerType: TimerType,
        paymentMethodType: String,
        iconUrl: String,
    ) = timerEventProvider.getEventProvider().tryEmit(
        TimerProperties(
            id = TimerId.PM_IMAGE_LOADING_DURATION,
            timerType = timerType,
            analyticsContext =
                UrlAnalyticsContext(
                    paymentMethodType = paymentMethodType,
                    url = iconUrl,
                ),
        ),
    )

    private fun logAnalyticsAllImageDurationTimerEvent(timerType: TimerType) =
        timerEventProvider.getEventProvider().tryEmit(
            TimerProperties(
                TimerId.PM_ALL_IMAGES_LOADING_DURATION,
                timerType,
            ),
        )

    private companion object {
        const val DEFAULT_REQUEST_TIMEOUT_MILLIS = 2000L
    }
}
