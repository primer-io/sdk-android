package io.primer.android.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.di.DIAppComponent
import io.primer.android.domain.base.None
import io.primer.android.domain.deeplink.async.AsyncPaymentMethodDeeplinkInteractor
import io.primer.android.domain.tokenization.TokenizationInteractor
import io.primer.android.domain.tokenization.models.TokenizationParams
import io.primer.android.model.SyncValidationError
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.card.CreditCard
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.util.Collections

internal class TokenizationViewModel(
    private val config: PrimerConfig,
    private val tokenizationInteractor: TokenizationInteractor,
    private val asyncPaymentMethodDeeplinkInteractor: AsyncPaymentMethodDeeplinkInteractor
) : ViewModel(), DIAppComponent {

    private var paymentMethod: PaymentMethodDescriptor? = null

    val submitted = MutableLiveData(false)
    val error = MutableLiveData<Throwable>()
    val tokenizationResult = MutableLiveData<String>()

    val tokenizationStatus = MutableLiveData(TokenizationStatus.NONE)
    val tokenizationError = MutableLiveData<Unit>()
    val validationErrors: MutableLiveData<List<SyncValidationError>> = MutableLiveData(
        Collections.emptyList()
    )
    val autoFocusFields: MutableLiveData<Set<String>> = MutableLiveData(
        Collections.emptySet()
    )
    private val _tokenizationCanceled = MutableLiveData<String>()
    val tokenizationCanceled: LiveData<String> = _tokenizationCanceled

    fun resetPaymentMethod(paymentMethodDescriptor: PaymentMethodDescriptor? = null) {
        paymentMethod = paymentMethodDescriptor
        submitted.postValue(false)
        tokenizationStatus.postValue(TokenizationStatus.NONE)

        if (paymentMethodDescriptor != null) {
            validationErrors.postValue(paymentMethodDescriptor.validate())
        } else {
            validationErrors.postValue(Collections.emptyList())
        }
    }

    fun isValid(): Boolean =
        paymentMethod != null && (validationErrors.value?.isEmpty() == true)

    fun tokenize() {
        viewModelScope.launch {
            tokenizationInteractor(
                TokenizationParams(
                    paymentMethod ?: return@launch,
                    config.paymentMethodIntent,
                )
            )
                .onStart { tokenizationStatus.postValue(TokenizationStatus.LOADING) }
                .catch {
                    tokenizationStatus.postValue(TokenizationStatus.ERROR)
                }
                .collect {
                    tokenizationResult.postValue(it)
                    tokenizationStatus.postValue(TokenizationStatus.SUCCESS)
                }
        }
    }

    fun setTokenizableValue(key: String, value: String, withValidation: Boolean = true) {
        paymentMethod?.let { pm ->
            pm.setTokenizableValue(key, value)
            if (withValidation) validationErrors.value = pm.validate()
            autoFocusFields.value = pm.getValidAutoFocusableFields()
        }
    }

    fun setCardHasFields(fields: Map<String, Boolean>?) {
        val availableFields = mutableMapOf<PrimerInputElementType, Boolean>()
        for ((key, value) in fields.orEmpty()) {
            PrimerInputElementType.fieldOf(key)?.let { fieldType ->
                availableFields[fieldType] = value
            }
        }
        (paymentMethod as? CreditCard)?.availableFields?.putAll(availableFields)
    }

    fun userCanceled(paymentMethodType: String) {
        _tokenizationCanceled.postValue(paymentMethodType)
    }

    fun hasField(inputType: PrimerInputElementType): Boolean = paymentMethod
        ?.hasFieldValue(inputType) ?: false

    fun getRedirectionUrl() = asyncPaymentMethodDeeplinkInteractor.execute(None())

    // endregion
}
