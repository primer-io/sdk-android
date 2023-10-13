package io.primer.android.domain.payments.apaya

import io.primer.android.components.presentation.paymentMethods.nativeUi.apaya.models.ApayaPaymentModel
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.base.BaseFlowInteractor
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.payments.apaya.models.ApayaSessionParams
import io.primer.android.domain.payments.apaya.models.ApayaWebResultParams
import io.primer.android.domain.payments.apaya.repository.ApayaSessionRepository
import io.primer.android.domain.payments.apaya.validation.ApayaSessionParamsValidator
import io.primer.android.domain.payments.apaya.validation.ApayaWebResultValidator
import io.primer.android.extensions.doOnError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

internal class ApayaSessionInteractor(
    private val apayaSessionParamsValidator: ApayaSessionParamsValidator,
    private val apayaWebResultValidator: ApayaWebResultValidator,
    private val apayaSessionRepository: ApayaSessionRepository,
    private val baseErrorEventResolver: BaseErrorEventResolver,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseFlowInteractor<ApayaPaymentModel, ApayaSessionParams>() {

    override fun execute(params: ApayaSessionParams) =
        apayaSessionParamsValidator.validate(params)
            .doOnError {
                baseErrorEventResolver.resolve(it, ErrorMapperType.SESSION_CREATE)
            }
            .flatMapLatest {
                apayaSessionRepository.createClientSession(params).map {
                    ApayaPaymentModel(
                        it.webViewTitle,
                        it.redirectUrl,
                        RETURN_URL,
                        it.token
                    )
                }.flowOn(dispatcher).catch {
                    baseErrorEventResolver.resolve(
                        it,
                        ErrorMapperType.SESSION_CREATE
                    )
                }
            }

    fun validateWebResultParams(webResultParams: ApayaWebResultParams) =
        apayaWebResultValidator.validate(webResultParams)
            .catch {
                baseErrorEventResolver.resolve(
                    it,
                    ErrorMapperType.SESSION_CREATE
                )
            }

    internal companion object {

        const val RETURN_URL = "primer.io"
    }
}
