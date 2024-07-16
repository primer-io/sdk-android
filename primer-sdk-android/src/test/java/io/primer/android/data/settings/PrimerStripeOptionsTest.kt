package io.primer.android.data.settings

import android.os.Parcel
import android.os.Parcelable
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.extensions.readParcelable
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class PrimerStripeOptionsTest {

    @Test
    fun `should create PrimerStripeOptions with publishableKey`() {
        val mandateData = PrimerStripeOptions.MandateData.TemplateMandateData("Primer Inc.")
        val publishableKey = "pk"
        val options = PrimerStripeOptions(mandateData, publishableKey)

        assertEquals(mandateData, options.mandateData)
        assertEquals(publishableKey, options.publishableKey)
    }

    @Test
    fun `should create PrimerStripeOptions with null publishableKey`() {
        val options = PrimerStripeOptions()

        assertEquals(null, options.publishableKey)
    }

    @Test
    fun `describeContents() should return 0`() {
        assertEquals(0, PrimerStripeOptions().describeContents())
    }

    @Test
    fun `should write publishable key to parcel when writeToParcel() is called`() {
        val mandateData = PrimerStripeOptions.MandateData.FullMandateData(0)
        val publishableKey = "pk"
        val options = PrimerStripeOptions(mandateData, publishableKey)
        val parcel = mockk<Parcel>(relaxed = true)

        options.writeToParcel(parcel, 0)

        verify {
            parcel.writeString(publishableKey)
        }
    }

    @Test
    fun `should read publishable key from parcel when createFromParcel() is called`() {
        val publishableKey = "pk"
        val mandateData = PrimerStripeOptions.MandateData.FullMandateData(0)
        val parcel = mockk<Parcel>(relaxed = true) {
            every { readString() } returns publishableKey
            every { readParcelable<Parcelable>() } returns mandateData
        }

        val options = PrimerStripeOptions.CREATOR.createFromParcel(parcel)

        assertEquals(publishableKey, options.publishableKey)
    }

    @Test
    fun `newArray() should return an array of nulls`() {
        val array = PrimerStripeOptions.CREATOR.newArray(10)

        assertContentEquals(arrayOfNulls<PrimerStripeOptions>(10), array)
    }
}
