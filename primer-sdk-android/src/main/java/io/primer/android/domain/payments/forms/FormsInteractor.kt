package io.primer.android.domain.payments.forms

import io.primer.android.domain.base.BaseFlowInteractor
import io.primer.android.domain.payments.forms.models.Form
import io.primer.android.domain.payments.forms.models.FormInputParams
import io.primer.android.domain.payments.forms.repository.FormsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn

internal class FormsInteractor(
    private val formsRepository: FormsRepository,
    override val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : BaseFlowInteractor<Form, FormInputParams>() {

    override fun execute(params: FormInputParams) =
        formsRepository.getForms(params.paymentMethodType).flowOn(dispatcher)
}
