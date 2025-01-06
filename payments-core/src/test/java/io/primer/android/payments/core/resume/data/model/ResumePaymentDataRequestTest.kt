package io.primer.android.payments.core.resume.data.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class ResumePaymentDataRequestTest {
    @Test
    fun `test ResumePaymentDataRequest serialization`() {
        // Given
        val resumeToken = "sampleResumeToken"
        val requestData = ResumePaymentDataRequest(resumeToken)

        // When
        val serializedJson = ResumePaymentDataRequest.serializer.serialize(requestData)

        // Then
        assertNotNull(serializedJson)
        assertEquals(resumeToken, serializedJson.getString("resumeToken"))
    }
}
