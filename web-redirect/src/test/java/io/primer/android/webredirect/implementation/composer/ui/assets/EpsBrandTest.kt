import io.primer.android.webredirect.R
import io.primer.android.webredirect.implementation.composer.ui.assets.EpsBrand
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class EpsBrandTest {

    private val epsBrand = EpsBrand()

    @Test
    fun `should return correct resource ids`() {
        assertEquals(R.drawable.ic_logo_eps, epsBrand.iconResId, "iconResId does not match")
        assertEquals(R.drawable.ic_logo_eps_square, epsBrand.logoResId, "logoResId does not match")
        assertEquals(R.drawable.ic_logo_eps_dark, epsBrand.iconDarkResId, "iconDarkResId does not match")
    }
}
