package io.primer.android.card.di

import io.primer.android.card.implementation.payment.delegate.CardPaymentDelegate
import io.primer.android.card.implementation.payment.resume.clientToken.data.CardNative3DSClientTokenParser
import io.primer.android.card.implementation.payment.resume.handler.CardResumeHandler
import io.primer.android.card.implementation.tokenization.data.datasource.CardRemoteTokenizationDataSource
import io.primer.android.card.implementation.tokenization.data.mapper.CardTokenizationParamsMapper
import io.primer.android.card.implementation.tokenization.data.model.CardPaymentInstrumentDataRequest
import io.primer.android.card.implementation.tokenization.data.repository.CardTokenizationDataRepository
import io.primer.android.card.implementation.tokenization.domain.CardTokenizationInteractor
import io.primer.android.card.implementation.tokenization.domain.DefaultCardTokenizationInteractor
import io.primer.android.card.implementation.tokenization.presentation.CardTokenizationDelegate
import io.primer.android.card.implementation.validation.domain.CardInputDataValidator
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.configuration.di.ConfigurationCoreContainer
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.paymentmethods.PaymentInputDataValidator
import io.primer.android.paymentmethods.analytics.delegate.PaymentMethodSdkAnalyticsEventLoggingDelegate
import io.primer.android.payments.core.helpers.PaymentMethodPaymentDelegate
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource
import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.android.core.logging.WhitelistedHttpBodyKeyProviderRegistry
import io.primer.cardShared.binData.data.datasource.InMemoryCardBinMetadataDataSource
import io.primer.cardShared.binData.data.datasource.RemoteCardBinMetadataDataSource
import io.primer.cardShared.binData.data.model.CardBinMetadataDataNetworksResponse
import io.primer.cardShared.binData.data.repository.CardBinMetadataDataRepository
import io.primer.cardShared.binData.domain.CardBinMetadataRepository
import io.primer.cardShared.binData.domain.CardDataMetadataRetriever
import io.primer.cardShared.binData.domain.CardMetadataCacheHelper
import io.primer.cardShared.binData.domain.CardMetadataStateRetriever
import io.primer.cardShared.networks.data.repository.OrderedAllowedCardNetworksDataRepository
import io.primer.cardShared.networks.domain.repository.OrderedAllowedCardNetworksRepository

internal class CardContainer(private val sdk: () -> SdkContainer, private val paymentMethodType: String) :
    DependencyContainer() {

    override fun registerInitialDependencies() {
        sdk().resolve<WhitelistedHttpBodyKeyProviderRegistry>().apply {
            listOf(
                CardBinMetadataDataNetworksResponse.provider
            ).forEach(::register)
        }

        registerSingleton<OrderedAllowedCardNetworksRepository> {
            OrderedAllowedCardNetworksDataRepository(
                sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY)
            )
        }

        registerSingleton {
            InMemoryCardBinMetadataDataSource()
        }

        registerSingleton {
            RemoteCardBinMetadataDataSource(sdk().resolve())
        }

        registerFactory<CardBinMetadataRepository> {
            CardBinMetadataDataRepository(
                localConfigurationDataSource = sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY),
                remoteCardBinMetadataDataSource = resolve(),
                inMemoryCardBinMetadataDataSource = resolve()
            )
        }

        registerFactory {
            CardDataMetadataRetriever()
        }

        registerSingleton { CardMetadataCacheHelper() }

        registerFactory {
            CardMetadataStateRetriever(
                binMetadataDataRepository = resolve(),
                allowedCardNetworksRepository = sdk().resolve(),
                cardMetadataCacheHelper = resolve(),
                analyticsRepository = sdk().resolve(),
                logReporter = sdk().resolve()
            )
        }

        registerFactory<PaymentInputDataValidator<PrimerCardData>>(name = paymentMethodType) {
            CardInputDataValidator(checkoutModuleRepository = sdk().resolve())
        }

        registerFactory(name = paymentMethodType) {
            PaymentMethodSdkAnalyticsEventLoggingDelegate(
                primerPaymentMethodManagerCategory =
                PrimerPaymentMethodManagerCategory.RAW_DATA.name,
                analyticsInteractor = sdk().resolve()
            )
        }

        registerFactory<BaseRemoteTokenizationDataSource<CardPaymentInstrumentDataRequest>>(
            name = paymentMethodType
        ) {
            CardRemoteTokenizationDataSource(primerHttpClient = sdk().resolve())
        }

        registerFactory { CardTokenizationParamsMapper() }

        registerFactory<CardTokenizationInteractor>(name = paymentMethodType) {
            DefaultCardTokenizationInteractor(
                tokenizationRepository = CardTokenizationDataRepository(
                    remoteTokenizationDataSource = sdk().resolve(paymentMethodType),
                    configurationDataSource = sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY),
                    tokenizationParamsMapper = resolve()
                ),
                tokenizedPaymentMethodRepository = sdk().resolve(),
                preTokenizationHandler = sdk().resolve(),
                logReporter = sdk().resolve()
            )
        }

        registerFactory {
            CardTokenizationDelegate(
                tokenizationInteractor = resolve(
                    name = paymentMethodType
                )
            )
        }

        registerFactory {
            CardNative3DSClientTokenParser()
        }

        registerFactory<PaymentMethodPaymentDelegate>(name = paymentMethodType) {
            CardPaymentDelegate(
                paymentMethodTokenHandler = sdk().resolve(),
                resumePaymentHandler = sdk().resolve(),
                successHandler = sdk().resolve(),
                errorHandler = sdk().resolve(),
                baseErrorResolver = sdk().resolve(),
                resumeHandler = resolve()
            )
        }

        registerFactory {
            CardResumeHandler(
                clientTokenParser = resolve(),
                validateClientTokenRepository = sdk().resolve(),
                clientTokenRepository = sdk().resolve(),
                checkoutAdditionalInfoHandler = sdk().resolve()
            )
        }
    }
}
