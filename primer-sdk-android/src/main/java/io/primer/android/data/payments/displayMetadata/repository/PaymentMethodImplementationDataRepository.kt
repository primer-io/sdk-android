package io.primer.android.data.payments.displayMetadata.repository

import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.domain.payments.displayMetadata.models.PaymentMethodImplementation
import io.primer.android.domain.payments.displayMetadata.repository.PaymentMethodImplementationRepository

internal class PaymentMethodImplementationDataRepository(
    private val localConfigurationDataSource: LocalConfigurationDataSource
) : PaymentMethodImplementationRepository {
    override fun getPaymentMethodsImplementation(): List<PaymentMethodImplementation> {
        val configurationData = localConfigurationDataSource.getConfiguration()
        return configurationData.paymentMethods.map { config ->
            PaymentMethodImplementation(
                config.type,
                config.name,
                config.displayMetadata?.buttonData?.let { buttonData ->
                    PaymentMethodImplementation.ButtonMetadata(
                        configurationData.iconsDisplayMetadata.find {
                            it[config.type] != null
                        }?.values.orEmpty().flatten(),
                        buttonData.backgroundColorData?.let {
                            PaymentMethodImplementation.ButtonMetadata.ColorMetadata(
                                it.colored?.trim(),
                                it.light?.trim(),
                                it.dark?.trim()
                            )
                        },
                        buttonData.borderColorData?.let {
                            PaymentMethodImplementation.ButtonMetadata.ColorMetadata(
                                it.colored?.trim(),
                                it.light?.trim(),
                                it.dark?.trim()
                            )
                        },

                        buttonData.borderWidthData?.let {
                            PaymentMethodImplementation.ButtonMetadata.BorderWidthMetadata(
                                it.colored,
                                it.light,
                                it.dark
                            )
                        },
                        buttonData.cornerRadius,
                        buttonData.text,
                        buttonData.textColorData?.let {
                            PaymentMethodImplementation.ButtonMetadata.ColorMetadata(
                                it.colored?.trim(),
                                it.light?.trim(),
                                it.dark?.trim()
                            )
                        },
                        buttonData.iconPositionRelativeToText
                    )
                }
            )
        }
    }
}
