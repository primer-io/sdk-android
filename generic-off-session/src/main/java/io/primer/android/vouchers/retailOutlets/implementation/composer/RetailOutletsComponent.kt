package io.primer.android.vouchers.retailOutlets.implementation.composer

import io.primer.android.PrimerRetailerData
import io.primer.android.RetailOutletsList
import io.primer.android.PrimerSessionIntent
import io.primer.android.core.extensions.flatMap
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.errors.domain.ErrorMapperRegistry
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.paymentmethods.PaymentInputDataValidator
import io.primer.android.paymentmethods.PrimerInitializationData
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.paymentmethods.core.composer.RawDataPaymentMethodComponent
import io.primer.android.components.domain.core.models.metadata.PrimerPaymentMethodMetadata
import io.primer.android.components.domain.core.models.metadata.PrimerPaymentMethodMetadataState
import io.primer.android.paymentmethods.manager.composable.PrimerHeadlessDataInitializable
import io.primer.android.vouchers.retailOutlets.implementation.payment.delegate.RetailOutletsPaymentDelegate
import io.primer.android.vouchers.retailOutlets.implementation.rpc.domain.RetailOutletInteractor
import io.primer.android.vouchers.retailOutlets.implementation.rpc.domain.models.RetailOutletParams
import io.primer.android.vouchers.retailOutlets.implementation.tokenization.presentation.RetailOutletsTokenizationDelegate
import io.primer.android.vouchers.retailOutlets.implementation.tokenization.presentation.composable.RetailOutletsTokenizationInputable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

internal class RetailOutletsComponent(
    private val tokenizationDelegate: RetailOutletsTokenizationDelegate,
    private val paymentDelegate: RetailOutletsPaymentDelegate,
    private val retailOutletsDataValidator: PaymentInputDataValidator<PrimerRetailerData>,
    private val retailOutletInteractor: RetailOutletInteractor,
    private val errorMapperRegistry: ErrorMapperRegistry
) : RawDataPaymentMethodComponent<PrimerRetailerData>(), PrimerHeadlessDataInitializable {

    private var primerSessionIntent by Delegates.notNull<PrimerSessionIntent>()
    private var paymentMethodType by Delegates.notNull<String>()

    private val _componentInputValidations =
        MutableSharedFlow<List<PrimerInputValidationError>>()
    override val componentInputValidations: Flow<List<PrimerInputValidationError>> = _componentInputValidations

    override val metadataStateFlow: Flow<PrimerPaymentMethodMetadataState> = emptyFlow()
    override val metadataFlow: Flow<PrimerPaymentMethodMetadata> = emptyFlow()

    private val _collectedData: MutableSharedFlow<PrimerRetailerData> =
        MutableSharedFlow(replay = 1)

    override fun start(paymentMethodType: String, sessionIntent: PrimerSessionIntent) {
        this.paymentMethodType = paymentMethodType
        this.primerSessionIntent = sessionIntent
    }

    override fun configure(completion: (PrimerInitializationData?, PrimerError?) -> Unit) {
        composerScope.launch {
            retailOutletInteractor(RetailOutletParams(paymentMethodType = paymentMethodType)).map { outlets ->
                RetailOutletsList(result = outlets)
            }.fold(
                onSuccess = { retailOutletsList ->
                    completion(retailOutletsList, null)
                },
                onFailure = { throwable ->
                    completion(null, errorMapperRegistry.getPrimerError(throwable))
                }
            )
        }
    }

    override fun updateCollectedData(collectedData: PrimerRetailerData) {
        composerScope.launch {
            _collectedData.emit(collectedData)
        }
        validateRawData(collectedData)
    }

    override fun submit() {
        startTokenization(_collectedData.replayCache.last())
    }

    private fun startTokenization(
        retailerData: PrimerRetailerData
    ) = composerScope.launch {
        tokenizationDelegate.tokenize(
            RetailOutletsTokenizationInputable(
                retailOutletData = retailerData,
                paymentMethodType = paymentMethodType,
                primerSessionIntent = primerSessionIntent
            )
        ).flatMap { paymentMethodTokenData ->
            paymentDelegate.handlePaymentMethodToken(
                paymentMethodTokenData = paymentMethodTokenData,
                primerSessionIntent = primerSessionIntent
            )
        }.onFailure {
            paymentDelegate.handleError(it)
        }
    }

    private fun validateRawData(
        cardData: PrimerRetailerData
    ) = composerScope.launch {
        runSuspendCatching {
            _componentInputValidations.emit(retailOutletsDataValidator.validate(cardData))
        }
    }
}
