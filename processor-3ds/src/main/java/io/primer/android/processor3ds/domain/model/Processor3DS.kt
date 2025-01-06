package io.primer.android.processor3ds.domain.model

import java.io.Serializable

data class Processor3DS(
    val redirectUrl: String,
    val statusUrl: String,
    val title: String = "3D Secure",
) : Serializable
