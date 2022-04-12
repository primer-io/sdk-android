package io.primer.android.payment

import android.view.View
import android.view.ViewGroup
import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.data.configuration.models.PrimerInputFieldType
import io.primer.android.model.SyncValidationError
import io.primer.android.ui.fragments.PaymentMethodLoadingFragment
import io.primer.android.ui.payment.LoadingState
import org.json.JSONObject
import java.util.Collections

internal abstract class PaymentMethodDescriptor(val config: PaymentMethodRemoteConfig) {

    protected val values: JSONObject by lazy { JSONObject() }

    abstract val selectedBehaviour: SelectedPaymentMethodBehaviour

    open val behaviours: List<SelectedPaymentMethodBehaviour> =
        listOf(NewFragmentBehaviour({ PaymentMethodLoadingFragment.newInstance() }))

    abstract val type: PaymentMethodUiType

    abstract val vaultCapability: VaultCapability

    internal val brand = config.type.brand

    // FIXME this should not be here. a model should not be responsible creating views
    abstract fun createButton(container: ViewGroup): View

    open fun getLoadingState(): LoadingState? = null

    fun setTokenizableValue(key: String, value: String) {
        values.put(key, value)
    }

    fun setTokenizableField(type: PrimerInputFieldType, value: String) {
        values.put(type.field, value)
    }

    fun setTokenizableValue(key: String, value: JSONObject) {
        values.put(key, value)
    }

    fun appendTokenizableValue(parent: String, key: String, value: String) {
        values.put(parent, (values.optJSONObject(parent) ?: JSONObject()).put(key, value))
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

    fun hasFieldValue(type: PrimerInputFieldType): Boolean {
        val data = if (values.has(type.field)) values.get(type.field) else null
        return if (data is String) data.isNotBlank()
        else data != null
    }

    fun clearInputField(type: PrimerInputFieldType) {
        values.remove(type.field)
    }
}
