package com.example.myapplication.datamodels

import androidx.annotation.Keep

@Keep
data class TransactionResponse(
    val id: String,
    val date: String,
    val status: TransactionStatus,
    val requiredAction: RequiredActionData? = null,
) : ExampleAppRequestBody

@Keep
enum class TransactionStatus {

    PENDING,
    FAILED,
    AUTHORIZED,
    SETTLING,
    PARTIALLY_SETTLED,
    SETTLED,
    DECLINED,
    CANCELLED
}

fun TransactionStatus.toTransactionState(): TransactionState {
    return when (this) {
        TransactionStatus.PENDING -> TransactionState.PENDING
        TransactionStatus.FAILED, TransactionStatus.DECLINED, TransactionStatus.CANCELLED -> TransactionState.ERROR
        else -> TransactionState.SUCCESS
    }
}

@Keep
data class RequiredActionData(
    val name: RequiredActionName,
    val description: String,
    val clientToken: String? = null,
) : ExampleAppRequestBody

@Keep
enum class RequiredActionName {

    `3DS_AUTHENTICATION`,
    USE_PRIMER_SDK
}
