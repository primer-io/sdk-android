package io.primer.android.payment

import android.view.View
import android.view.ViewGroup
import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.model.dto.SyncValidationError
import io.primer.android.ui.fragments.PaymentMethodLoadingFragment
import io.primer.android.ui.payment.LoadingState
import org.json.JSONObject
import java.util.Collections

abstract class PaymentMethodDescriptor(
    val config: PaymentMethodRemoteConfig,
    private val values: JSONObject = JSONObject(), // FIXME avoid holding JSONObject here
) {

    abstract val selectedBehaviour: SelectedPaymentMethodBehaviour

    open val behaviours: List<SelectedPaymentMethodBehaviour> =
        listOf(NewFragmentBehaviour({ PaymentMethodLoadingFragment.newInstance() }))

    abstract val type: PaymentMethodUiType

    abstract val vaultCapability: VaultCapability

    // FIXME this should not be here. a model should not be responsible creating views
    abstract fun createButton(container: ViewGroup): View

    open fun getLoadingState(): LoadingState? = null

    // FIXME all this should not be here. a model should not be responsible for parsing itself into json
    protected fun getStringValue(key: String): String {
        return values.optString(key)
    }

    fun setTokenizableValue(key: String, value: String) {
        values.put(key, value)
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
}
