package io.primer.android.domain.payments.apaya

import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.base.BaseFlowInteractor
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.payments.apaya.models.ApayaSessionParams
import io.primer.android.domain.payments.apaya.models.ApayaWebResultParams
import io.primer.android.domain.payments.apaya.repository.ApayaRepository
import io.primer.android.domain.payments.apaya.validation.ApayaSessionParamsValidator
import io.primer.android.domain.payments.apaya.validation.ApayaWebResultValidator
import io.primer.android.domain.payments.apaya.models.ApayaPaymentData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

internal class ApayaSessionInteractor(
    private val apayaSessionParamsValidator: ApayaSessionParamsValidator,
    private val apayaWebResultValidator: ApayaWebResultValidator,
    private val apayaRepository: ApayaRepository,
    private val baseErrorEventResolver: BaseErrorEventResolver,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseFlowInteractor<ApayaPaymentData, ApayaSessionParams>() {

    override fun execute(params: ApayaSessionParams) =
        apayaSessionParamsValidator.validate(params)
            .catch {
                baseErrorEventResolver.resolve(it, ErrorMapperType.SESSION_CREATE)
            }
            .flatMapLatest {
                apayaRepository.createClientSession(params).map {
                    ApayaPaymentData(
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
