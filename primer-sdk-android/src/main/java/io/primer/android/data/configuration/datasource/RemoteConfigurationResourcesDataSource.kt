package io.primer.android.data.configuration.datasource

import io.primer.android.analytics.data.helper.TimerEventProvider
import io.primer.android.analytics.data.models.TimerId
import io.primer.android.analytics.data.models.TimerProperties
import io.primer.android.analytics.data.models.TimerType
import io.primer.android.analytics.data.models.UrlAnalyticsContext
import io.primer.android.components.ui.assets.ImageColor
import io.primer.android.data.base.datasource.BaseFlowDataSource
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.configuration.models.getImageAsset
import io.primer.android.data.extensions.await
import io.primer.android.data.payments.displayMetadata.model.IconDisplayMetadata
import io.primer.android.infrastructure.files.ImagesFileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedInputStream
import java.io.FileOutputStream

internal class RemoteConfigurationResourcesDataSource(
    private val okHttpClient: OkHttpClient,
    private val imagesFileProvider: ImagesFileProvider,
    private val timerEventProvider: TimerEventProvider
) : BaseFlowDataSource<
        List<Map<String, List<IconDisplayMetadata>>>, List<PaymentMethodConfigDataResponse>
        > {

    override fun execute(input: List<PaymentMethodConfigDataResponse>) = flow {
        coroutineScope {
            logAnalyticsAllImageDurationTimerEvent(TimerType.START)
            val iconsMetadata = input.map { config ->
                val brand = PaymentMethodType.safeValueOf(config.type).brand
                val iconUrl = config.displayMetadata?.buttonData?.iconUrl
                listOfNotNull(
                    iconUrl?.colored?.to(ImageColor.COLORED),
                    iconUrl?.dark?.to(ImageColor.DARK),
                    iconUrl?.light?.to(ImageColor.LIGHT)
                ).map { urlColor ->
                    async(Dispatchers.IO) {
                        withTimeoutOrNull(DEFAULT_REQUEST_TIMEOUT_MILLIS) {
                            logAnalyticsImageLoadingTimerEvent(
                                TimerType.START,
                                config.type,
                                urlColor.first
                            )

                            val request = Request.Builder()
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
                                            urlColor.first
                                        )
                                        if (it.isSuccessful) {
                                            val bufferedInputStream =
                                                BufferedInputStream(it.body?.byteStream())
                                            val file = imagesFileProvider.getFile(
                                                "${config.type}_${urlColor.second}".lowercase()
                                            )

                                            FileOutputStream(file).use { outputStream ->
                                                outputStream.write(bufferedInputStream.readBytes())
                                                outputStream.flush()
                                                IconDisplayMetadata(
                                                    urlColor.second,
                                                    urlColor.first,
                                                    file.absolutePath,
                                                    brand.getImageAsset(urlColor.second)
                                                )
                                            }
                                        } else {
                                            IconDisplayMetadata(
                                                imageColor = urlColor.second,
                                                url = urlColor.first,
                                                iconResId = brand.getImageAsset(urlColor.second)
                                            )
                                        }
                                    }
                            } catch (_: Exception) {
                                IconDisplayMetadata(
                                    imageColor = urlColor.second,
                                    url = urlColor.first,
                                    iconResId = brand.getImageAsset(urlColor.second)
                                )
                            }
                        } ?: IconDisplayMetadata(
                            imageColor = urlColor.second,
                            url = urlColor.first,
                            iconResId = brand.getImageAsset(urlColor.second)
                        )
                    }
                }.ifEmpty {
                    listOfNotNull(
                        iconUrl?.colored?.to(
                            IconDisplayMetadata(
                                ImageColor.COLORED,
                                iconResId = brand.getImageAsset(ImageColor.COLORED)
                            )
                        ),
                        iconUrl?.dark?.to(
                            IconDisplayMetadata(
                                ImageColor.DARK,
                                iconResId = brand.getImageAsset(ImageColor.DARK)
                            )
                        ),
                        iconUrl?.light?.to(
                            IconDisplayMetadata(
                                ImageColor.LIGHT,
                                iconResId = brand.getImageAsset(ImageColor.LIGHT)
                            )
                        )
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
            emit(iconsMetadata)
        }
    }

    private fun logAnalyticsImageLoadingTimerEvent(
        timerType: TimerType,
        paymentMethodType: String,
        iconUrl: String
    ) = timerEventProvider.getTimerEventProvider().tryEmit(
        TimerProperties(
            id = TimerId.PM_IMAGE_LOADING_DURATION,
            timerType = timerType,
            analyticsContext = UrlAnalyticsContext(
                paymentMethodType = paymentMethodType,
                url = iconUrl
            )
        )
    )

    private fun logAnalyticsAllImageDurationTimerEvent(
        timerType: TimerType
    ) = timerEventProvider.getTimerEventProvider().tryEmit(
        TimerProperties(
            TimerId.PM_ALL_IMAGES_LOADING_DURATION,
            timerType
        )
    )

    private companion object {
        const val DEFAULT_REQUEST_TIMEOUT_MILLIS = 2000L
    }
}
