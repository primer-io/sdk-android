@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.android.components.di

import io.primer.android.components.data.error.VaultErrorMapper
import io.primer.android.components.data.payments.metadata.card.datasource.InMemoryCardBinMetadataDataSource
import io.primer.android.components.data.payments.metadata.card.datasource.RemoteCardBinMetadataDataSource
import io.primer.android.components.data.payments.metadata.card.repository.CardBinMetadataDataRepository
import io.primer.android.components.data.payments.repository.CheckoutModuleDataRepository
import io.primer.android.components.domain.assets.validation.resolvers.AssetManagerInitValidationRulesResolver
import io.primer.android.components.domain.core.mapper.PrimerHeadlessUniversalCheckoutPaymentMethodMapper
import io.primer.android.components.domain.core.models.PrimerRawData
import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.android.components.domain.inputs.PaymentInputTypesInteractor
import io.primer.android.components.domain.payments.PaymentRawDataChangedInteractor
import io.primer.android.components.domain.payments.PaymentRawDataTypeValidateInteractor
import io.primer.android.components.domain.payments.PaymentTokenizationInteractor
import io.primer.android.components.domain.payments.PaymentsTypesInteractor
import io.primer.android.components.domain.payments.metadata.PaymentRawDataMetadataRetrieverFactory
import io.primer.android.components.domain.payments.metadata.card.CardDataMetadataRetriever
import io.primer.android.components.domain.payments.metadata.card.CardDataMetadataStateRetriever
import io.primer.android.components.domain.payments.metadata.card.CardMetadataCacheHelper
import io.primer.android.components.domain.payments.metadata.card.repository.CardBinMetadataRepository
import io.primer.android.components.domain.payments.paymentMethods.PaymentRawDataValidationInteractor
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.validation.resolvers.PaymentMethodManagerInitValidationRulesResolver
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.validation.resolvers.PaymentMethodManagerSessionIntentRulesResolver
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.validation.rules.SdkInitializedRule
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.validation.rules.ValidPaymentMethodManagerRule
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.validation.rules.ValidPaymentMethodRule
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.validation.rules.ValidSessionIntentRule
import io.primer.android.components.domain.payments.paymentMethods.raw.validation.PaymentInputDataValidatorFactory
import io.primer.android.components.domain.payments.repository.CheckoutModuleRepository
import io.primer.android.components.domain.payments.vault.HeadlessVaultedPaymentMethodInteractor
import io.primer.android.components.domain.payments.vault.HeadlessVaultedPaymentMethodsExchangeInteractor
import io.primer.android.components.domain.payments.vault.HeadlessVaultedPaymentMethodsInteractor
import io.primer.android.components.domain.payments.vault.validation.additionalData.VaultedPaymentMethodAdditionalDataValidatorRegistry
import io.primer.android.components.domain.payments.vault.validation.resolvers.VaultManagerInitValidationRulesResolver
import io.primer.android.components.domain.payments.vault.validation.rules.ValidClientSessionCustomerIdRule
import io.primer.android.components.domain.tokenization.helpers.VaultPostTokenizationEventResolver
import io.primer.android.components.presentation.DefaultHeadlessUniversalCheckoutDelegate
import io.primer.android.components.presentation.assets.DefaultAssetsHeadlessDelegate
import io.primer.android.components.presentation.paymentMethods.base.DefaultHeadlessManagerDelegate
import io.primer.android.components.presentation.paymentMethods.raw.DefaultRawDataManagerDelegate
import io.primer.android.components.presentation.paymentMethods.raw.RawDataDelegate
import io.primer.android.components.presentation.paymentMethods.raw.card.CardRawDataManagerDelegate
import io.primer.android.components.presentation.vault.VaultManagerDelegate
import io.primer.android.components.ui.navigation.Navigator
import io.primer.android.components.ui.views.PrimerPaymentMethodViewFactory
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.di.DependencyContainer
import io.primer.android.di.SdkContainer
import io.primer.android.domain.error.ErrorMapper
import kotlinx.coroutines.ExperimentalCoroutinesApi

internal class ComponentsContainer(private val sdk: SdkContainer) : DependencyContainer() {

    @Suppress("LongMethod")
    override fun registerInitialDependencies() {
        registerSingleton { Navigator(sdk.resolve()) }
        registerSingleton {
            PrimerPaymentMethodViewFactory(sdk.resolve(), sdk.resolve())
        }
        registerSingleton {
            PrimerHeadlessUniversalCheckoutPaymentMethodMapper(sdk.resolve())
        }
        registerSingleton<CheckoutModuleRepository> {
            CheckoutModuleDataRepository(sdk.resolve())
        }

        registerSingleton {
            PaymentTokenizationInteractor(
                sdk.resolve(),
                sdk.resolve()
            )
        }
        registerSingleton {
            PaymentsTypesInteractor(
                sdk.resolve(),
                sdk.resolve(),
                resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve()
            )
        }
        registerSingleton {
            PaymentInputTypesInteractor(
                resolve(),
                sdk.resolve(),
                sdk.resolve()
            )
        }
        registerSingleton {
            PaymentRawDataTypeValidateInteractor(
                resolve(),
                sdk.resolve()
            )
        }
        registerFactory {
            DefaultHeadlessUniversalCheckoutDelegate(
                resolve(),
                sdk.resolve()
            )
        }

        registerSingleton {
            InMemoryCardBinMetadataDataSource()
        }

        registerSingleton {
            RemoteCardBinMetadataDataSource(sdk.resolve())
        }

        registerFactory<CardBinMetadataRepository> {
            CardBinMetadataDataRepository(sdk.resolve(), resolve(), resolve())
        }

        registerFactory {
            PaymentInputDataValidatorFactory(
                resolve(),
                sdk.resolve()
            )
        }

        registerFactory {
            PaymentRawDataMetadataRetrieverFactory()
        }

        registerFactory {
            PaymentRawDataChangedInteractor(
                resolve(),
                resolve(),
                sdk.resolve()
            )
        }

        registerFactory {
            PaymentRawDataValidationInteractor(
                resolve()
            )
        }

        registerFactory {
            SdkInitializedRule(sdk.resolve())
        }

        registerFactory {
            AssetManagerInitValidationRulesResolver(resolve())
        }

        registerFactory {
            DefaultAssetsHeadlessDelegate(
                resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve()
            )
        }

        registerFactory {
            ValidPaymentMethodManagerRule(resolve())
        }

        registerFactory {
            ValidPaymentMethodRule(sdk.resolve())
        }

        registerFactory {
            ValidSessionIntentRule(resolve())
        }

        registerFactory {
            PaymentMethodManagerInitValidationRulesResolver(resolve(), resolve(), resolve())
        }

        registerFactory {
            PaymentMethodManagerSessionIntentRulesResolver(resolve())
        }

        registerSingleton<RawDataDelegate<PrimerRawData>> {
            DefaultRawDataManagerDelegate(
                sdk.resolve(),
                resolve(),
                resolve(),
                resolve(),
                resolve(),
                resolve(),
                sdk.resolve(),
                sdk.resolve()
            )
        }

        registerFactory {
            CardDataMetadataRetriever()
        }

        registerSingleton { CardMetadataCacheHelper() }

        registerFactory {
            CardDataMetadataStateRetriever(
                resolve(),
                sdk.resolve(),
                resolve(),
                sdk.resolve(),
                sdk.resolve()
            )
        }

        registerSingleton<RawDataDelegate<PrimerCardData>>(PaymentMethodType.PAYMENT_CARD.name) {
            CardRawDataManagerDelegate(
                sdk.resolve(),
                resolve(),
                resolve(),
                resolve(),
                resolve(),
                resolve(),
                sdk.resolve(),
                sdk.resolve()
            )
        }

        registerSingleton {
            DefaultHeadlessManagerDelegate(
                resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve(),
                resolve(),
                resolve(),
                sdk.resolve()
            )
        }

        registerFactory { ValidClientSessionCustomerIdRule(sdk.resolve()) }
        registerFactory { VaultManagerInitValidationRulesResolver(resolve(), resolve()) }

        registerFactory {
            HeadlessVaultedPaymentMethodsInteractor(
                sdk.resolve()
            )
        }

        registerFactory {
            HeadlessVaultedPaymentMethodInteractor(
                sdk.resolve()
            )
        }

        registerFactory {
            VaultPostTokenizationEventResolver(
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve()
            )
        }

        registerFactory {
            HeadlessVaultedPaymentMethodsExchangeInteractor(
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve(),
                resolve(),
                sdk.resolve()
            )
        }

        registerFactory {
            VaultedPaymentMethodAdditionalDataValidatorRegistry()
        }

        registerFactory<ErrorMapper>(name = VAULT_ERROR_RESOLVER_NAME) {
            VaultErrorMapper()
        }

        registerSingleton {
            VaultManagerDelegate(
                resolve(),
                resolve(),
                sdk.resolve(),
                resolve(),
                resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve(),
                resolve(),
                sdk.resolve(),
                resolve(name = VAULT_ERROR_RESOLVER_NAME),
                resolve()
            )
        }
    }

    private companion object {

        const val VAULT_ERROR_RESOLVER_NAME = "vaultErrorResolver"
    }
}
