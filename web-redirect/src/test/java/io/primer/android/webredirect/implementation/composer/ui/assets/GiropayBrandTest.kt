import io.primer.android.webredirect.R
import io.primer.android.webredirect.implementation.composer.ui.assets.GiropayBrand
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GiropayBrandTest {

    private val giropayBrand = GiropayBrand()

    @Test
    fun `should return correct resource ids`() {
        assertEquals(R.drawable.ic_logo_giropay, giropayBrand.iconResId, "iconResId does not match")
    }
}
