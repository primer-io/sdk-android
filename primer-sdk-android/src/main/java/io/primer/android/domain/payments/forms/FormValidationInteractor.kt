package io.primer.android.domain.payments.forms

import io.primer.android.domain.base.BaseFlowInteractor
import io.primer.android.domain.payments.forms.models.FormValidationParam
import io.primer.android.domain.payments.forms.validation.ValidatorFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn

internal class FormValidationInteractor(
    private val validatorFactory: ValidatorFactory,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseFlowInteractor<Boolean, FormValidationParam>() {

    override fun execute(params: FormValidationParam) =
        flowOf(
            validatorFactory.getValidators(params).all { it.validate(params.input.toString()) }
        ).flowOn(dispatcher)
}
