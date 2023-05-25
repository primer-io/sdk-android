package io.primer.android.threeds.data.models.auth

import io.primer.android.core.serialization.json.JSONObjectSerializable
import io.primer.android.core.serialization.json.JSONObjectSerializer
import org.json.JSONObject

internal data class ThreeDsCustomerDataRequest(
    val name: String? = null,
    val email: String? = null,
    val homePhone: String? = null,
    val mobilePhone: String? = null,
    val workPhone: String? = null,
) : JSONObjectSerializable {

    companion object {
        private const val NAME_FIELD = "name"
        private const val EMAIL_FIELD = "email"
        private const val HOME_PHONE_FIELD = "homePhone"
        private const val MOBILE_PHONE_FIELD = "mobilePhone"
        private const val WORK_PHONE_FIELD = "workPhone"

        @JvmField
        val serializer = object : JSONObjectSerializer<ThreeDsCustomerDataRequest> {
            override fun serialize(t: ThreeDsCustomerDataRequest): JSONObject {
                return JSONObject().apply {
                    putOpt(NAME_FIELD, t.name)
                    putOpt(EMAIL_FIELD, t.email)
                    putOpt(HOME_PHONE_FIELD, t.homePhone)
                    putOpt(MOBILE_PHONE_FIELD, t.mobilePhone)
                    putOpt(WORK_PHONE_FIELD, t.workPhone)
                }
            }
        }
    }
}
