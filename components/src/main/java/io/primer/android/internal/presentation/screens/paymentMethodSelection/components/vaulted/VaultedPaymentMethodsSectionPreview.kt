@file:Suppress("UnusedPrivateMember")

package io.primer.android.internal.presentation.screens.paymentMethodSelection.components.vaulted

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.primer.android.LocalPrimerTheme
import io.primer.android.PrimerTheme
import io.primer.android.components.PrimerVaultedComponents
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import io.primer.android.scope.PrimerVaultedScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

@Preview(showBackground = true, name = "Vaulted Payment Methods Section")
@Composable
private fun VaultedPaymentMethodsSectionPreview() {
    CompositionLocalProvider(LocalPrimerTheme provides PrimerTheme()) {
        MaterialTheme {
            val previewMethods = remember { createPreviewVaultedPaymentMethods() }
            val previewState = remember {
                PrimerVaultedScope.State(
                    paymentMethods = previewMethods,
                    selectedPaymentMethodId = previewMethods.first().id,
                    isLoading = false,
                    stage = PrimerVaultedScope.State.Stage.Selection,
                )
            }
            val previewScope = remember { PreviewVaultedScope(initialState = previewState) }

            VaultedPaymentMethodsSection(
                vaultedState = previewState,
                vaultedScope = previewScope,
                vaultedComponents = PrimerVaultedComponents(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            )
        }
    }
}

internal fun createPreviewVaultedPaymentMethods(): List<PrimerVaultedPaymentMethod> {
    return listOf(
        previewVaultedPaymentMethod(
            PreviewVaultedCard(
                id = "preview-card-mastercard",
                network = "MASTERCARD",
                last4 = 1234,
                cardholderName = "John Appleseed",
            ),
        ),
        previewVaultedPaymentMethod(
            PreviewVaultedCard(
                id = "preview-card-visa",
                network = "VISA",
                last4 = 9876,
                cardholderName = "Jane Smith",
            ),
        ),
    )
}

private fun previewVaultedPaymentMethod(
    card: PreviewVaultedCard,
): PrimerVaultedPaymentMethod {
    return PrimerVaultedPaymentMethod(
        id = card.id,
        analyticsId = "preview-analytics-${card.id}",
        paymentMethodType = "PAYMENT_CARD",
        paymentInstrumentType = "PAYMENT_CARD",
        paymentInstrumentData = io.primer.android.data.tokenization.models.PaymentInstrumentData(
            network = card.network,
            cardholderName = card.cardholderName,
            first6Digits = 543210,
            last4Digits = card.last4,
            accountNumberLast4Digits = null,
            expirationMonth = card.expirationMonth,
            expirationYear = card.expirationYear,
            externalPayerInfo = null,
            klarnaCustomerToken = null,
            sessionData = null,
            paymentMethodType = null,
            sessionInfo = null,
            binData = null,
            bankName = null,
        ),
    )
}

private data class PreviewVaultedCard(
    val id: String,
    val network: String,
    val last4: Int,
    val cardholderName: String,
    val expirationMonth: Int = 12,
    val expirationYear: Int = 2026,
)

internal class PreviewVaultedScope(
    initialState: PrimerVaultedScope.State = PrimerVaultedScope.State(),
) : PrimerVaultedScope {

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<PrimerVaultedScope.State> = _state

    override suspend fun submit() {
        Unit
    }

    override suspend fun cvvRecapture() {
        Unit
    }

    override fun updateCvv(cvv: String) {
        _state.update {
            it.copy(
                cvvValue = cvv,
                isCvvValid = cvv.length == it.expectedCvvLength && cvv.all(Char::isDigit),
            )
        }
    }

    override fun selectPaymentMethod(paymentMethodId: String) {
        _state.update { it.copy(selectedPaymentMethodId = paymentMethodId) }
    }

    override fun clearSelection() {
        _state.update { it.copy(selectedPaymentMethodId = null) }
    }

    override fun cancelCvvRecapture() {
        _state.update {
            it.copy(
                isCvvRequired = false,
                isProcessing = false,
                isLoading = it.paymentMethods.isEmpty(),
            )
        }
    }

    override fun showAllMethods() {
        _state.update {
            it.copy(
                stage = PrimerVaultedScope.State.Stage.AllMethods,
                editMode = PrimerVaultedScope.State.EditMode.View,
            )
        }
    }
}
