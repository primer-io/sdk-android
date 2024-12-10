import io.primer.android.webredirect.R
import io.primer.android.webredirect.implementation.composer.ui.assets.HoolahBrand
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class HoolahBrandTest {

    private val hoolahBrand = HoolahBrand()

    @Test
    fun `should return correct resource ids`() {
        assertEquals(R.drawable.ic_logo_hoolah, hoolahBrand.iconResId, "iconResId does not match")
        assertEquals(R.drawable.ic_logo_hoolah_square, hoolahBrand.logoResId, "logoResId does not match")
        assertEquals(R.drawable.ic_logo_hoolah_light, hoolahBrand.iconLightResId, "iconLightResId does not match")
    }
}
