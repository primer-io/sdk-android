package io.primer.android.data.settings

import android.os.Parcel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FullMandateDataTest {

    @Test
    fun `should write value to parcel when writeToParcel() is called`() {
        val parcel = mockk<Parcel>(relaxed = true)
        val fullMandateData = PrimerStripeOptions.MandateData.FullMandateData(value = 123)

        fullMandateData.writeToParcel(parcel, 0)

        verify {
            parcel.writeInt(123)
        }
    }

    @Test
    fun `describeContents() should return 0`() {
        val fullMandateData = PrimerStripeOptions.MandateData.FullMandateData(value = 123)
        assertEquals(0, fullMandateData.describeContents())
    }

    @Test
    fun `should read value from parcel when createFromParcel() is called`() {
        val parcel = mockk<Parcel>()
        every { parcel.readInt() } returns 123

        val fullMandateData = PrimerStripeOptions.MandateData.FullMandateData.CREATOR.createFromParcel(parcel)

        assertEquals(123, fullMandateData.value)
    }

    @Test
    fun `newArray() should return an array of nulls`() {
        val array = PrimerStripeOptions.MandateData.FullMandateData.CREATOR.newArray(5)
        assertEquals(5, array.size)
    }
}
