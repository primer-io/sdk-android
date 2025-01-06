package io.primer.android.viewmodel

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.android.components.domain.core.models.card.PrimerCardMetadataState
import io.primer.android.components.domain.core.models.card.PrimerCardNetwork
import io.primer.android.components.domain.core.models.card.PrimerCardNumberEntryMetadata
import io.primer.android.components.domain.core.models.metadata.PrimerPaymentMethodMetadataState
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.manager.raw.PrimerHeadlessUniversalCheckoutRawDataManager
import io.primer.android.components.manager.raw.PrimerHeadlessUniversalCheckoutRawDataManagerInterface
import io.primer.android.components.manager.raw.PrimerHeadlessUniversalCheckoutRawDataManagerListener
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.domain.helper.toSyncValidationError
import io.primer.android.model.SyncValidationError
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.util.Collections

internal class CardViewModel(
    private val cardManager: PrimerHeadlessUniversalCheckoutRawDataManagerInterface =
        PrimerHeadlessUniversalCheckoutRawDataManager.newInstance(PaymentMethodType.PAYMENT_CARD.name)
) : ViewModel() {

    private val _cardValidationErrors: MutableStateFlow<List<SyncValidationError>> = MutableStateFlow(emptyList())
    val cardValidationErrors: StateFlow<List<SyncValidationError>> = _cardValidationErrors

    private val _billingAddressValidationErrors: MutableStateFlow<List<SyncValidationError>> =
        MutableStateFlow(emptyList())
    val billingAddressValidationErrors: StateFlow<List<SyncValidationError>> = _billingAddressValidationErrors

    val tokenizationStatus = MutableStateFlow(TokenizationStatus.NONE)

    var submitted = false
        private set

    val autoFocusFields: MutableLiveData<Set<PrimerInputElementType>> = MutableLiveData(
        Collections.emptySet()
    )

    private val _cardNetworksState: MutableStateFlow<CardNetworksState> =
        MutableStateFlow(CardNetworksState(emptyList(), null, null))
    val cardNetworksState: StateFlow<CardNetworksState> = _cardNetworksState

    private var cachedCardData: PrimerCardData? = null

    @VisibleForTesting
    var isMetadataUpdating = false

    override fun onCleared() {
        super.onCleared()
        cardManager.cleanup()
    }

    fun initialize() {
        cardManager.setListener(object : PrimerHeadlessUniversalCheckoutRawDataManagerListener {

            override fun onValidationChanged(isValid: Boolean, errors: List<PrimerInputValidationError>) {
                _cardValidationErrors.value = errors.map { inputValidationError ->
                    inputValidationError.toSyncValidationError(cachedCardData)
                }
                autoFocusFields.postValue(getValidAutoFocusableFields(errors))
            }

            override fun onMetadataStateChanged(metadataState: PrimerPaymentMethodMetadataState) {
                when (metadataState) {
                    is PrimerCardMetadataState.Fetched -> {
                        handleFetchedMetadata(metadataState.cardNumberEntryMetadata)
                        isMetadataUpdating = false
                    }
                    is PrimerCardMetadataState.Fetching -> {
                        isMetadataUpdating = true
                    }
                }
            }
        })
    }

    @VisibleForTesting
    internal fun handleFetchedMetadata(metadata: PrimerCardNumberEntryMetadata) {
        val selectableNetworks = metadata.selectableCardNetworks?.items
        val detectedNonSelectableNetwork = metadata.detectedCardNetworks.let {
            it.preferred ?: it.items.firstOrNull()
        }

        val resolvedNetworks = selectableNetworks ?: listOf(detectedNonSelectableNetwork)

        val state = CardNetworksState(
            networks = resolvedNetworks.mapNotNull { it },
            preferredNetwork = metadata.selectableCardNetworks?.preferred?.network,
            selectedNetwork = cachedCardData?.cardNetwork
                ?: metadata.selectableCardNetworks?.preferred?.network
                ?: resolvedNetworks.first()?.network
        )
        _cardNetworksState.update { state }
    }

    fun setSelectedNetwork(network: CardNetwork.Type) {
        cachedCardData?.let { onCardDataChanged(it.copy(cardNetwork = network)) }
        _cardNetworksState.update { it.copy(selectedNetwork = network) }
    }

    fun onCardDataChanged(cardData: PrimerCardData) {
        val newCardData = when {
            cachedCardData == null -> cardData
            cardData.cardNetwork == null -> cardData.copy(cardNetwork = cachedCardData?.cardNetwork)
            else -> cardData
        }
        cardManager.setRawData(newCardData)
        this.cachedCardData = newCardData
    }

    fun submit() {
        cardManager.submit()
        submitted = true
    }

    fun isValid() = _cardValidationErrors.value.isEmpty() &&
        _billingAddressValidationErrors.value.isEmpty() &&
        cachedCardData != null &&
        isSubmitButtonEnabled(tokenizationStatus.value)

    fun updateValidationErrors(errors: List<SyncValidationError>) {
        _billingAddressValidationErrors.value = errors
    }

    @VisibleForTesting
    internal fun isSubmitButtonEnabled(tokenizationStatus: TokenizationStatus?): Boolean {
        return (
            tokenizationStatus == TokenizationStatus.NONE ||
                tokenizationStatus == TokenizationStatus.ERROR
            ) && !isMetadataUpdating
    }

    @VisibleForTesting
    internal fun getValidAutoFocusableFields(errors: List<PrimerInputValidationError>): Set<PrimerInputElementType> {
        val fields = hashSetOf<PrimerInputElementType>()
        if (errors.none { it.inputElementType == PrimerInputElementType.CARD_NUMBER }) {
            fields.add(PrimerInputElementType.CARD_NUMBER)
        }

        if (errors.none { it.inputElementType == PrimerInputElementType.CVV }) {
            fields.add(PrimerInputElementType.CVV)
        }

        if (errors.none { it.inputElementType == PrimerInputElementType.EXPIRY_DATE }) {
            fields.add(PrimerInputElementType.EXPIRY_DATE)
        }

        val containsCardholderName =
            cardManager.getRequiredInputElementTypes().contains(PrimerInputElementType.CARDHOLDER_NAME)
        if (containsCardholderName && cachedCardData?.cardHolderName.isNullOrBlank().not()
        ) {
            fields.add(PrimerInputElementType.CARDHOLDER_NAME)
        }

        return fields
    }
}

internal data class CardNetworksState(
    val networks: List<PrimerCardNetwork>,
    val preferredNetwork: CardNetwork.Type?,
    val selectedNetwork: CardNetwork.Type?
)
