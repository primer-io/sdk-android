package io.primer.android.domain.payments.apaya

import io.primer.android.domain.payments.apaya.models.ApayaSessionParams
import io.primer.android.domain.payments.apaya.models.ApayaWebResultParams
import io.primer.android.domain.payments.apaya.repository.ApayaRepository
import io.primer.android.domain.payments.apaya.validation.ApayaSessionParamsValidator
import io.primer.android.domain.payments.apaya.validation.ApayaWebResultValidator
import io.primer.android.events.EventDispatcher
import io.primer.android.extensions.toTokenizationErrorEvent
import io.primer.android.model.ApayaPaymentData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

internal class ApayaInteractor(
    private val apayaSessionParamsValidator: ApayaSessionParamsValidator,
    private val apayaWebResultValidator: ApayaWebResultValidator,
    private val apayaRepository: ApayaRepository,
    private val eventDispatcher: EventDispatcher,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    fun createClientSession(params: ApayaSessionParams) =
        apayaSessionParamsValidator.validate(params)
            .catch {
                eventDispatcher.dispatchEvent(it.toTokenizationErrorEvent(it.message))
            }
            .flatMapLatest {
                apayaRepository.createClientSession(params).map {
                    ApayaPaymentData(
                        it.redirectUrl,
                        RETURN_URL,
                        it.token
                    )
                }.flowOn(dispatcher).catch {
                    eventDispatcher.dispatchEvent(
                        it.toTokenizationErrorEvent(
                            APAYA_FAILED_CREATE_SESSION
                        )
                    )
                }
            }

    fun validateWebResultParams(webResultParams: ApayaWebResultParams) =
        apayaWebResultValidator.validate(webResultParams)
            .catch {
                eventDispatcher.dispatchEvent(it.toTokenizationErrorEvent(it.message))
            }

    internal companion object {

        const val RETURN_URL = "primer.io/apaya/"
        private const val APAYA_FAILED_CREATE_SESSION =
            "The call to create an Apaya payment session (token & redirectUrl) failed."
    }
}
