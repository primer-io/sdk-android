package io.primer.android.domain.payments.forms

import io.primer.android.domain.base.BaseInteractor
import io.primer.android.domain.payments.forms.models.FormValidationParam
import io.primer.android.domain.payments.forms.validation.ValidatorFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn

internal class FormValidationInteractor(
    private val validatorFactory: ValidatorFactory,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseInteractor<Boolean, FormValidationParam>() {

    override fun execute(params: FormValidationParam) =
        flowOf(
            validatorFactory.getValidators(params).all { it.validate(params.input.toString()) }
        ).flowOn(dispatcher)
}
