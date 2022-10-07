package io.primer.android.payment

import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.domain.payments.additionalInfo.PrimerCheckoutAdditionalInfoResolver
import io.primer.android.model.SyncValidationError
import io.primer.android.ui.fragments.PaymentMethodLoadingFragment
import io.primer.android.ui.payment.LoadingState
import org.json.JSONObject
import java.util.Collections

internal abstract class PaymentMethodDescriptor(val config: PaymentMethodConfigDataResponse) {

    protected val values: JSONObject by lazy { JSONObject() }

    abstract val selectedBehaviour: SelectedPaymentMethodBehaviour

    open val behaviours: List<SelectedPaymentMethodBehaviour> =
        listOf(NewFragmentBehaviour({ PaymentMethodLoadingFragment.newInstance() }))

    abstract val type: PaymentMethodUiType

    abstract val vaultCapability: VaultCapability

    open val sdkCapabilities = listOf(SDKCapability.HEADLESS, SDKCapability.DROP_IN)

    open val additionalInfoResolver: PrimerCheckoutAdditionalInfoResolver? = null

    internal val brand = PaymentMethodType.safeValueOf(config.type).brand

    open fun getLoadingState(): LoadingState? = null

    fun setTokenizableValue(key: String, value: String) {
        values.put(key, value)
    }

    fun setTokenizableField(type: PrimerInputElementType, value: String) {
        values.put(type.field, value)
    }

    fun setTokenizableValue(key: String, value: JSONObject) {
        values.put(key, value)
    }

    fun appendTokenizableValue(parent: String, key: String, value: String) {
        values.put(parent, (values.optJSONObject(parent) ?: JSONObject()).put(key, value))
    }

    fun appendTokenizableValue(superParent: String, parent: String, key: String, value: String) {
        values.put(
            superParent,
            (values.optJSONObject(superParent) ?: JSONObject())
                .put(parent, (values.optJSONObject(parent) ?: JSONObject()).put(key, value))
        )
    }

    open fun validate(): List<SyncValidationError> {
        return Collections.emptyList()
    }

    open fun getValidAutoFocusableFields(): Set<String> = hashSetOf()

    open fun toPaymentInstrument(): JSONObject {
        return values
    }

    fun pushValues(json: JSONObject) {
        json.keys().forEach {
            values.put(it, json[it])
        }
    }

    fun hasFieldValue(type: PrimerInputElementType): Boolean {
        val data = if (values.has(type.field)) values.get(type.field) else null
        return if (data is String) data.isNotBlank()
        else data != null
    }

    fun clearInputField(type: PrimerInputElementType) {
        values.remove(type.field)
    }
}
