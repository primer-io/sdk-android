package io.primer.android.components.manager.vault

import io.primer.android.components.SdkUninitializedException
import io.primer.android.components.domain.exception.VaultManagerInitException
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.core.extensions.flatMap
import io.primer.android.vault.implementation.vaultedMethods.domain.PrimerVaultedPaymentMethodAdditionalData
import io.primer.android.vault.implementation.vaultedMethods.presentation.delegate.VaultManagerComposerDelegate
import io.primer.android.vault.implementation.vaultedMethods.presentation.delegate.VaultManagerDelegate
import kotlin.properties.Delegates

class PrimerHeadlessUniversalCheckoutVaultManager private constructor() :
    PrimerHeadlessUniversalCheckoutVaultManagerInterface,
    DISdkComponent {
    private var delegate: VaultManagerDelegate by Delegates.notNull()
    private var composerDelegate: VaultManagerComposerDelegate by Delegates.notNull()

    init {
        delegate = resolve()
        delegate.init()

        composerDelegate = resolve()
    }

    override suspend fun fetchVaultedPaymentMethods() = delegate.fetchVaultedPaymentMethods()

    override suspend fun deleteVaultedPaymentMethod(vaultedPaymentMethodId: String) =
        delegate.deletePaymentMethod(vaultedPaymentMethodId)

    override suspend fun validate(
        vaultedPaymentMethodId: String,
        additionalData: PrimerVaultedPaymentMethodAdditionalData,
    ) = delegate.validate(vaultedPaymentMethodId, additionalData)

    override suspend fun startPaymentFlow(vaultedPaymentMethodId: String) =
        delegate.startPaymentFlow(vaultedPaymentMethodId).flatMap { paymentMethodToken ->
            composerDelegate.handlePaymentMethod(paymentMethodToken = paymentMethodToken)
        }.map { }

    override suspend fun startPaymentFlow(
        vaultedPaymentMethodId: String,
        additionalData: PrimerVaultedPaymentMethodAdditionalData,
    ) = delegate.startPaymentFlow(vaultedPaymentMethodId, additionalData).flatMap { paymentMethodToken ->
        composerDelegate.handlePaymentMethod(paymentMethodToken = paymentMethodToken)
    }.map { }

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
