package io.primer.android.components.manager.vault

import io.primer.android.components.SdkUninitializedException
import io.primer.android.components.domain.exception.VaultManagerInitException
import io.primer.android.components.domain.payments.vault.PrimerVaultedPaymentMethodAdditionalData
import io.primer.android.components.presentation.vault.VaultManagerDelegate
import io.primer.android.di.DIAppComponent
import org.koin.core.component.get
import kotlin.properties.Delegates

class PrimerHeadlessUniversalCheckoutVaultManager private constructor() :
    PrimerHeadlessUniversalCheckoutVaultManagerInterface,
    DIAppComponent {

    private var delegate: VaultManagerDelegate by Delegates.notNull()

    init {
        delegate = get()
        delegate.init()
    }

    override suspend fun fetchVaultedPaymentMethods() = delegate.fetchVaultedPaymentMethods()

    override suspend fun deleteVaultedPaymentMethod(vaultedPaymentMethodId: String) =
        delegate.deletePaymentMethod(vaultedPaymentMethodId)

    override suspend fun validate(
        vaultedPaymentMethodId: String,
        additionalData: PrimerVaultedPaymentMethodAdditionalData
    ) = delegate.validate(vaultedPaymentMethodId, additionalData)

    override suspend fun startPaymentFlow(vaultedPaymentMethodId: String) =
        delegate.startPaymentFlow(vaultedPaymentMethodId)

    override suspend fun startPaymentFlow(
        vaultedPaymentMethodId: String,
        additionalData: PrimerVaultedPaymentMethodAdditionalData
    ) = delegate.startPaymentFlow(vaultedPaymentMethodId, additionalData)

    companion object {

        /**
         * Creates vault manager tied to current client session.
         *
         * @throws SdkUninitializedException in case [io.primer.android.components.PrimerHeadlessUniversalCheckout]
         * was not initialized.
         * @throws VaultManagerInitException in case `customerId` was not passed when creating client session.
         */
        @JvmStatic
        @Throws(SdkUninitializedException::class, VaultManagerInitException::class)
        fun newInstance(): PrimerHeadlessUniversalCheckoutVaultManagerInterface =
            PrimerHeadlessUniversalCheckoutVaultManager()
    }
}
