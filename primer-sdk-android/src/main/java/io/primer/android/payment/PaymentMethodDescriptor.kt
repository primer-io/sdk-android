package io.primer.android.payment

import android.content.Context
import android.view.View
import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.model.dto.SyncValidationError
import org.json.JSONObject
import java.util.Collections

abstract class PaymentMethodDescriptor(
    val config: PaymentMethodRemoteConfig,
    private val values: JSONObject = JSONObject(), // FIXME avoid holding JSONObject here
) {

    abstract val identifier: String

    abstract val selectedBehaviour: SelectedPaymentMethodBehaviour

    abstract val type: PaymentMethodType

    abstract val vaultCapability: VaultCapability

    // FIXME this should not be here. a model should not be responsible creating views
    abstract fun createButton(context: Context): View

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

    open fun validate(): List<SyncValidationError> {
        return Collections.emptyList()
    }

    open fun toPaymentInstrument(): JSONObject {
        return values
    }
}
