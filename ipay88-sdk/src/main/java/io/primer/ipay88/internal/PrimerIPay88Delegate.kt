package io.primer.ipay88.internal

import com.ipay.IPayIHResultDelegate
import com.ipay.obj.MCTokenizationRet
import java.io.Serializable

internal class PrimerIPay88Delegate :
    IPayIHResultDelegate, Serializable {

    override fun onPaymentSucceeded(
        transId: String?,
        refNo: String?,
        amount: String?,
        remark: String?,
        authCode: String?,
        tokenizationRet: MCTokenizationRet?
    ) {
        IPayStateHolder.currentState = IPayPaymentState.Success
    }

    override fun onPaymentFailed(
        transId: String?,
        refNo: String?,
        amount: String?,
        remark: String?,
        errorDescription: String?,
        tokenizationRet: MCTokenizationRet?
    ) {
        IPayStateHolder.currentState =
            IPayPaymentState.Failed(transId, tokenizationRet?.tokenId, refNo, errorDescription)
    }

    override fun onPaymentCanceled(
        transId: String?,
        refNo: String?,
        amount: String?,
        remark: String?,
        errorDescription: String?,
        tokenizationRet: MCTokenizationRet?
    ) {
        IPayStateHolder.currentState = IPayPaymentState.Cancelled(transId, refNo, errorDescription)
    }

    override fun onRequeryResult(
        merchantCode: String?,
        refNo: String?,
        amount: String?,
        result: String?
    ) {
        IPayStateHolder.currentState = IPayPaymentState.Success
    }

    override fun onConnectionError(
        merchantCode: String?,
        refNo: String?,
        amount: String?,
        remark: String?,
        lang: String?,
        country: String?
    ) {
        IPayStateHolder.currentState = IPayPaymentState.ConnectionError(refNo)
    }
}

internal sealed class IPayPaymentState {
    object Success : IPayPaymentState()
    data class Failed(
        val transId: String?,
        val tokenId: String?,
        val refNo: String?,
        val errorDescription: String?
    ) : IPayPaymentState()

    data class Cancelled(
        val transId: String?,
        val refNo: String?,
        val errorDescription: String?
    ) : IPayPaymentState()

    data class ConnectionError(val refNo: String?) : IPayPaymentState()
}
