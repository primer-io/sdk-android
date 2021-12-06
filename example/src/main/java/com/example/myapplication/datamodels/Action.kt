package com.example.myapplication.datamodels

import androidx.annotation.Keep

@Keep
class Action(
    val type: String,
    val params: Params? = null,
) {

    data class Request(
        val clientToken: String,
        val actions: List<Action>,
    ) : ExampleAppRequestBody

    data class Params(
        val paymentMethodType: String? = null,
        val binData: Map<String, Any>? = null,
    )
}