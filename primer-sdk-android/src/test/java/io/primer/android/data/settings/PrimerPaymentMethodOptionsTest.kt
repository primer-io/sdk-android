package io.primer.android.data.settings

import android.os.Parcel
import android.os.Parcelable
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class PrimerPaymentMethodOptionsTest {

    @Test
    fun `should write options to parcel when writeToParcel() is called`() {
        val parcel = mockk<Parcel>(relaxed = true)
        val redirectScheme = "redirectScheme"
        val googlePayOptions = PrimerGooglePayOptions()
        val klarnaOptions = PrimerKlarnaOptions()
        val threeDsOptions = PrimerThreeDsOptions()
        val stripeOptions = PrimerStripeOptions()

        val options = PrimerPaymentMethodOptions(
            redirectScheme = redirectScheme,
            googlePayOptions = googlePayOptions,
            klarnaOptions = klarnaOptions,
            threeDsOptions = threeDsOptions,
            stripeOptions = stripeOptions
        )

        options.writeToParcel(parcel, 0)

        verify {
            parcel.writeString(redirectScheme)
            parcel.writeParcelable(googlePayOptions, 0)
            parcel.writeParcelable(klarnaOptions, 0)
            parcel.writeParcelable(threeDsOptions, 0)
            parcel.writeParcelable(stripeOptions, 0)
        }
    }

    @Test
    fun `describeContents should return 0`() {
        val options = PrimerPaymentMethodOptions()
        assertEquals(0, options.describeContents())
    }

    @Test
    fun `should read options from parcel when createFromParcel() is called`() {
        val parcel = mockk<Parcel>()
        val redirectScheme = "redirectScheme"
        val googlePayOptions = PrimerGooglePayOptions()
        val klarnaOptions = PrimerKlarnaOptions()
        val threeDsOptions = PrimerThreeDsOptions()
        val stripeOptions = PrimerStripeOptions()

        every { parcel.readString() } returns redirectScheme
        every { parcel.readParcelable<Parcelable>(any()) } returnsMany listOf(
            googlePayOptions,
            klarnaOptions,
            threeDsOptions,
            stripeOptions
        )

        val options = PrimerPaymentMethodOptions.CREATOR.createFromParcel(parcel)

        assertEquals(redirectScheme, options.redirectScheme)
        assertEquals(googlePayOptions, options.googlePayOptions)
        assertEquals(klarnaOptions, options.klarnaOptions)
        assertEquals(threeDsOptions, options.threeDsOptions)
        assertEquals(stripeOptions, options.stripeOptions)
    }

    @Test
    fun `newArray() should return an array of nulls`() {
        val array = PrimerPaymentMethodOptions.CREATOR.newArray(10)

        assertContentEquals(arrayOfNulls<PrimerPaymentMethodOptions>(10), array)
    }
}
