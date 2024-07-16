package io.primer.android.data.settings

import android.os.Parcel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TemplateMandateDataTest {

    @Test
    fun `should write merchant name to parcel when writeToParcel() is called`() {
        val parcel = mockk<Parcel>(relaxed = true)
        val mandateData = PrimerStripeOptions.MandateData.TemplateMandateData(merchantName = "Primer Inc.")

        mandateData.writeToParcel(parcel, 0)

        verify {
            parcel.writeString("Primer Inc.")
        }
    }

    @Test
    fun `describeContents() should return 0`() {
        val mandateData = PrimerStripeOptions.MandateData.TemplateMandateData(merchantName = "Primer Inc.")
        assertEquals(0, mandateData.describeContents())
    }

    @Test
    fun `should read merchant name from parcel when createFromParcel() is called`() {
        val parcel = mockk<Parcel>()
        every { parcel.readString() } returns "Primer Inc."

        val mandateData = PrimerStripeOptions.MandateData.TemplateMandateData.CREATOR.createFromParcel(parcel)

        assertEquals("Primer Inc.", mandateData.merchantName)
    }

    @Test
    fun `should use empty string when createFromParcel() is called and mandate text is null`() {
        val parcel = mockk<Parcel>()
        every { parcel.readString() } returns null

        val mandateData = PrimerStripeOptions.MandateData.TemplateMandateData.CREATOR.createFromParcel(parcel)

        assertEquals("", mandateData.merchantName)
    }

    @Test
    fun `newArray() should return an array of nulls`() {
        val array = PrimerStripeOptions.MandateData.TemplateMandateData.CREATOR.newArray(5)
        assertEquals(5, array.size)
    }
}
