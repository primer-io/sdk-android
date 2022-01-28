package io.primer.android.domain.payments.forms

import io.primer.android.domain.base.BaseInteractor
import io.primer.android.domain.payments.forms.models.Form
import io.primer.android.domain.payments.forms.models.FormInputParams
import io.primer.android.domain.payments.forms.repository.FormsRepository

internal class FormsInteractor(private val formsRepository: FormsRepository) :
    BaseInteractor<Form, FormInputParams>() {

    override fun execute(params: FormInputParams) =
        formsRepository.getForms(params.paymentMethodType)
}
