package io.primer.android.payment

import io.primer.android.completion.PrimerResumeDecisionHandler
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.payments.additionalInfo.PrimerCheckoutAdditionalInfoResolver
import io.primer.android.model.SyncValidationError
import io.primer.android.ui.fragments.PaymentMethodLoadingFragment
import io.primer.android.ui.payment.LoadingState
import org.json.JSONObject
import java.util.Collections

internal abstract class PaymentMethodDescriptor(
    val config: PaymentMethodConfigDataResponse,
    val localConfig: PrimerConfig
) {

    // remove
    protected val values: JSONObject by lazy { JSONObject() }

    abstract val selectedBehaviour: SelectedPaymentMethodBehaviour

    abstract val type: PaymentMethodUiType

    abstract val vaultCapability: VaultCapability

    abstract val headlessDefinition: HeadlessDefinition?

    open val resumeHandler: PrimerResumeDecisionHandler? = null

    /**
     The logic is the following:
     1. if we are launched using `showPaymentMethod` (isStandalonePaymentMethod = true) and
     we have disabled initial screen (isInitScreenEnabled.not()), we won't show loading screen.
     2. Otherwise, we show loading screen.
     */
    open val behaviours: List<SelectedPaymentMethodBehaviour>
        get() = if (localConfig.settings.uiOptions.isInitScreenEnabled.not() &&
            localConfig.isStandalonePaymentMethod
        ) {
            listOf()
        } else { listOf(NewFragmentBehaviour({ PaymentMethodLoadingFragment.newInstance() })) }

    open val sdkCapabilities = listOf(SDKCapability.HEADLESS, SDKCapability.DROP_IN)

    open val additionalInfoResolver: PrimerCheckoutAdditionalInfoResolver? = null

    internal val brand = PaymentMethodType.safeValueOf(config.type).brand

    // remove
    open fun getLoadingState(): LoadingState? = null

    // remove
    fun setTokenizableValue(key: String, value: String) {
        values.put(key, value)
    }

    // remove
    fun appendTokenizableValue(parent: String, key: String, value: String) {
        values.put(parent, (values.optJSONObject(parent) ?: JSONObject()).put(key, value))
    }

    // remove
    fun appendTokenizableValue(superParent: String, parent: String, key: String, value: String) {
        values.put(
            superParent,
            (values.optJSONObject(superParent) ?: JSONObject())
                .put(parent, (values.optJSONObject(parent) ?: JSONObject()).put(key, value))
        )
    }

    // remove
    open fun validate(): List<SyncValidationError> {
        return Collections.emptyList()
    }

    // remove
    open fun getValidAutoFocusableFields(): Set<String> = hashSetOf()

    // remove
    open fun toPaymentInstrument(): JSONObject {
        return values
    }

    // remove
    fun pushValues(json: JSONObject) {
        json.keys().forEach {
            values.put(it, json[it])
        }
    }

    // remove
    fun hasFieldValue(type: PrimerInputElementType): Boolean {
        val data = if (values.has(type.field)) values.get(type.field) else null
        return if (data is String) {
            data.isNotBlank()
        } else { data != null }
    }

    // remove
    fun clearInputField(type: PrimerInputElementType) {
        values.remove(type.field)
    }
}
