package io.primer.android.components.di

import android.content.Context
import io.primer.android.clientSessionActions.di.ActionsContainer
import io.primer.android.clientToken.core.token.data.model.ClientToken
import io.primer.android.clientToken.di.ClientTokenCoreContainer
import io.primer.android.configuration.di.ConfigurationCoreContainer
import io.primer.android.configuration.mock.di.MockContainer
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.DISdkContext
import io.primer.android.core.di.SdkContainer
import io.primer.android.core.di.plus
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.errors.di.ErrorResolverContainer
import io.primer.android.payments.core.helpers.ManualFlowSuccessHandler
import io.primer.android.payments.di.PaymentsContainer
import io.primer.android.vault.di.VaultManagerContainer
import io.primer.paymentMethodCoreUi.core.ui.mock.di.PaymentMethodsMockContainer

object DISdkContextInitializer : DISdkComponent {
    fun initHeadless(
        config: PrimerConfig,
        context: Context,
    ) {
        SdkContainer().let { container ->
            container.init(config, context)
            DISdkContext.headlessSdkContainer?.clear()
            DISdkContext.headlessSdkContainer = container
        }
    }

    fun initDropIn(
        config: PrimerConfig,
        context: Context,
    ) {
        DISdkContext.isDropIn = true
        SdkContainer().let { container ->
            container.init(config, context)
            // Remove headless implementation
            container.unregisterType<ManualFlowSuccessHandler>()
            DISdkContext.dropInSdkContainer?.clear()
            DISdkContext.dropInSdkContainer = container
        }
    }

    fun clearHeadless() {
        DISdkContext.headlessSdkContainer?.clear()
        DISdkContext.headlessSdkContainer = null
    }

    fun clearDropIn() {
        DISdkContext.dropInSdkContainer?.clear()
        DISdkContext.dropInSdkContainer = null
        DISdkContext.isDropIn = false
    }
    // endregion

    private fun SdkContainer.init(
        config: PrimerConfig,
        context: Context,
    ) {
        apply {
            val container = { requireNotNull(getSdkContainer() + this) }

            val apiVersion = { container().resolve<PrimerSettings>().apiVersion }

            registerContainer(ErrorResolverContainer { container() })

            registerContainer(ClientTokenCoreContainer(sdk = { container() }))

            registerContainer(
                SharedContainer(
                    context = context,
                    config = config,
                    clientToken = ClientToken.fromString(config.clientTokenBase64.orEmpty()),
                ) { container() },
            )

            registerContainer(
                ConfigurationCoreContainer(
                    sdk = { container() },
                ),
            )

            registerContainer(MockContainer { container() })

            registerContainer(ImageLoaderContainer { container() })

            registerContainer(NetworkContainer { container() })

            registerContainer(ComponentsContainer { container() })

            registerContainer(AssetManagerContainer { container() })

            registerContainer(PaymentMethodDescriptorContainer(container()))

            registerContainer(PaymentMethodsContainer { container() })

            registerContainer(PaymentsContainer(sdk = { container() }))

            registerContainer(CurrencyFormatContainer { container() })

            registerContainer(ActionsContainer(sdk = { container() }))

            registerContainer(VaultManagerContainer(sdk = { container() }))

            registerContainer(PaymentMethodsMockContainer { container() })
        }
    }
}
