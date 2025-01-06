import io.primer.android.webredirect.R
import io.primer.android.webredirect.implementation.composer.ui.assets.GCashBrand
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GCashBrandTest {
    private val gCashBrand = GCashBrand()

    @Test
    fun `should return correct resource ids`() {
        assertEquals(R.drawable.ic_logo_gcash, gCashBrand.iconResId, "iconResId does not match")
        assertEquals(R.drawable.ic_logo_gcash_square, gCashBrand.logoResId, "logoResId does not match")
        assertEquals(R.drawable.ic_logo_gcash_dark, gCashBrand.iconDarkResId, "iconDarkResId does not match")
    }
}
