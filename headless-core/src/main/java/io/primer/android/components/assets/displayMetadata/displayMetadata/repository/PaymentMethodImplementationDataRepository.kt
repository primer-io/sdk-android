package io.primer.android.components.assets.displayMetadata.displayMetadata.repository

import io.primer.android.assets.ui.model.getImageAsset
import io.primer.android.assets.ui.registry.BrandRegistry
import io.primer.android.components.assets.displayMetadata.models.PaymentMethodImplementation
import io.primer.android.components.assets.displayMetadata.repository.PaymentMethodImplementationRepository
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.data.datasource.BaseCacheDataSource

internal class PaymentMethodImplementationDataRepository(
    private val localConfigurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>,
    private val brandRegistry: BrandRegistry,
) : PaymentMethodImplementationRepository {
    override fun getPaymentMethodsImplementation(): List<PaymentMethodImplementation> {
        val configurationData = localConfigurationDataSource.get()
        return configurationData.paymentMethods.map { config ->
            PaymentMethodImplementation(
                config.type,
                config.name,
                config.displayMetadata?.buttonData?.let { buttonData ->
                    PaymentMethodImplementation.ButtonMetadata(
                        configurationData.iconsDisplayMetadata.find {
                            it[config.type] != null
                        }?.values.orEmpty().flatten().map {
                            it.copy(iconResId = brandRegistry.getBrand(config.type).getImageAsset(it.imageColor))
                        },
                        buttonData.backgroundColorData?.let {
                            PaymentMethodImplementation.ButtonMetadata.ColorMetadata(
                                it.colored?.trim(),
                                it.light?.trim(),
                                it.dark?.trim(),
                            )
                        },
                        buttonData.borderColorData?.let {
                            PaymentMethodImplementation.ButtonMetadata.ColorMetadata(
                                it.colored?.trim(),
                                it.light?.trim(),
                                it.dark?.trim(),
                            )
                        },
                        buttonData.borderWidthData?.let {
                            PaymentMethodImplementation.ButtonMetadata.BorderWidthMetadata(
                                it.colored,
                                it.light,
                                it.dark,
                            )
                        },
                        buttonData.cornerRadius,
                        buttonData.text,
                        buttonData.textColorData?.let {
                            PaymentMethodImplementation.ButtonMetadata.ColorMetadata(
                                it.colored?.trim(),
                                it.light?.trim(),
                                it.dark?.trim(),
                            )
                        },
                        buttonData.iconPositionRelativeToText,
                    )
                },
            )
        }
    }
}
