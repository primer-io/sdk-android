package io.primer.android.threeds.data.models

import kotlinx.serialization.Serializable

@Serializable
data class ThreeDsAmount internal constructor(val amount: Int?, val currency: String?)
