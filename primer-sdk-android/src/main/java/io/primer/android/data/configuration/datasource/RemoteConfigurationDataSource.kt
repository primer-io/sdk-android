package io.primer.android.data.configuration.datasource

import io.primer.android.data.base.datasource.BaseFlowDataSource
import io.primer.android.data.configuration.models.ConfigurationDataResponse
import io.primer.android.data.configuration.models.IconPosition
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.data.configuration.models.PaymentMethodDisplayMetadataResponse
import io.primer.android.data.configuration.models.PaymentMethodImplementationType
import io.primer.android.data.configuration.models.PaymentMethodRemoteConfigOptions
import io.primer.android.di.ApiVersion
import io.primer.android.di.SDK_API_VERSION_HEADER
import io.primer.android.http.PrimerHttpClient
import kotlinx.coroutines.flow.mapLatest
import java.util.*

internal class RemoteConfigurationDataSource(private val httpClient: PrimerHttpClient) :
    BaseFlowDataSource<ConfigurationDataResponse, String> {
    override fun execute(input: String) = httpClient.get<ConfigurationDataResponse>(
        input,
        mapOf(SDK_API_VERSION_HEADER to ApiVersion.CONFIGURATION_VERSION.version)
    ).mapLatest {
        // TODO remove when payment method specs are added
        it.copy(
            paymentMethods = it.paymentMethods.plus(
                PaymentMethodConfigDataResponse(
                    UUID.randomUUID().toString(),
                    "Nol Pay",
                    PaymentMethodImplementationType.NATIVE_SDK,
                    "NOL_PAY",
                    PaymentMethodRemoteConfigOptions(null, null, merchantAppId = "1301", false),
                    PaymentMethodDisplayMetadataResponse(
                        PaymentMethodDisplayMetadataResponse.ButtonDataResponse(
                            PaymentMethodDisplayMetadataResponse.ButtonDataResponse.IconUrlDataResponse(
                                colored = "https://play-lh.googleusercontent.com/oY3gNF-Gy3VivNw-b2zMqXCd1wuILim5M16hHX-6tKd63fY5AwYQhQz0lr0cO3zhfeY",
                                null,
                                null
                            ),
                            backgroundColorData = null,
                            borderColorData = null,
                            borderWidthData = null,
                            cornerRadius = 4f,
                            iconPositionRelativeToText = IconPosition.START,
                            text = null,
                            textColorData = null
                        )
                    )
                )
            )
        )
    }
}
