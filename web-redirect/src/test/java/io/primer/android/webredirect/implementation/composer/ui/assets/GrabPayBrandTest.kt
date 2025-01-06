import io.primer.android.webredirect.R
import io.primer.android.webredirect.implementation.composer.ui.assets.GrabPayBrand
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GrabPayBrandTest {
    private val grabPayBrand = GrabPayBrand()

    @Test
    fun `should return correct resource ids`() {
        assertEquals(R.drawable.ic_logo_grab_pay, grabPayBrand.iconResId, "iconResId does not match")
        assertEquals(R.drawable.ic_logo_grab_pay_light, grabPayBrand.iconLightResId, "iconLightResId does not match")
        assertEquals(R.drawable.ic_logo_grab_pay_dark, grabPayBrand.iconDarkResId, "iconDarkResId does not match")
    }
}
