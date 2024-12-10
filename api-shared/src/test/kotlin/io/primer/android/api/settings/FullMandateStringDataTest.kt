package io.primer.android.api.settings

import android.os.Parcel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.data.settings.PrimerStripeOptions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FullMandateStringDataTest {

    @Test
    fun `should write value to parcel when writeToParcel() is called`() {
        val parcel = mockk<Parcel>(relaxed = true)
        val fullMandateStringData = PrimerStripeOptions.MandateData.FullMandateStringData(value = "123")

        fullMandateStringData.writeToParcel(parcel, 0)

        verify {
            parcel.writeString("123")
        }
    }

    @Test
    fun `describeContents() should return 0`() {
        val fullMandateStringData = PrimerStripeOptions.MandateData.FullMandateStringData(value = "123")
        assertEquals(0, fullMandateStringData.describeContents())
    }

    @Test
    fun `should read value from parcel when createFromParcel() is called`() {
        val parcel = mockk<Parcel>()
        every { parcel.readString() } returns "123"

        val fullMandateStringData =
            PrimerStripeOptions.MandateData.FullMandateStringData.createFromParcel(parcel)

        assertEquals("123", fullMandateStringData.value)
    }

    @Test
    fun `newArray() should return an array of nulls`() {
        val array = PrimerStripeOptions.MandateData.FullMandateStringData.newArray(5)
        assertEquals(5, array.size)
    }
}
