package io.primer.android.components.data.payments.paymentMethods.nolpay.error

import com.snowballtech.transit.rta.TransitException
import io.primer.android.domain.error.models.GeneralError
import io.primer.android.domain.error.models.NolPayError
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NolPayErrorMapperTest {

    private val errorMapper = NolPayErrorMapper()

    @Test
    fun `should emmit NolPayError when throwable is TransitException`() {
        val code = "code"
        val message = "message"
        val throwable = TransitException(code, message)

        val actualResult = errorMapper.getPrimerError(throwable)
        val expectedDescription = "Nol SDK encountered an error $code. $message"

        assertTrue(actualResult is NolPayError)
        assertEquals(expectedDescription, actualResult.description)
    }

    @Test
    fun `should emmit GeneralError when throwable is not TransitException`() {
        val message = "message"
        val throwable = Exception(message)

        val actualResult = errorMapper.getPrimerError(throwable)
        val expectedDescription = "Something went wrong. Message $message."

        assertTrue(actualResult is GeneralError.UnknownError)
        assertEquals(expectedDescription, actualResult.description)
    }
}
