package io.primer.android.components.assets.ui

import androidx.annotation.DrawableRes
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.displayMetadata.domain.model.ImageColor
import io.primer.android.headlessCore.R

internal enum class Brand(
    @DrawableRes internal val iconResId: Int,
    @DrawableRes internal val iconLightResId: Int? = null,
    @DrawableRes internal val iconDarkResId: Int? = null,
) {
    VISA(iconResId = R.drawable.ic_visa_card_colored),
    MASTERCARD(iconResId = R.drawable.ic_mastercard_card_colored),
    AMEX(iconResId = R.drawable.ic_amex_card_colored),
    DISCOVER(iconResId = R.drawable.ic_discover_card_colored),
    JCB(iconResId = R.drawable.ic_jcb_card_colored),
    DINERS_CLUB(
        iconResId = R.drawable.ic_diners_club_card_colored,
    ),
    MIR(iconResId = R.drawable.ic_mir_card_colored),
    UNIONPAY(iconResId = R.drawable.ic_unionpay_card_colored),
    HIPER(iconResId = R.drawable.ic_hiper_card_colored),
    CARTES_BANCAIRES(iconResId = R.drawable.ic_card_bancaires_colored),
    DANKORT(iconResId = R.drawable.ic_dankort_card_colored),
    MEASTRO(iconResId = R.drawable.ic_maestro_card_colored),
    ELO(iconResId = R.drawable.ic_elo_card_colored),
    GENERIC(iconResId = R.drawable.ic_generic_card),
    ;

    internal fun getImageAsset(imageColor: ImageColor) =
        when (imageColor) {
            ImageColor.COLORED -> iconResId
            ImageColor.DARK -> iconDarkResId
            ImageColor.LIGHT -> iconLightResId
        }
}

@Suppress("ComplexMethod")
internal fun CardNetwork.Type.getCardBrand() =
    when (this) {
        CardNetwork.Type.VISA -> Brand.VISA
        CardNetwork.Type.MASTERCARD -> Brand.MASTERCARD
        CardNetwork.Type.AMEX -> Brand.AMEX
        CardNetwork.Type.DANKORT -> Brand.DANKORT
        CardNetwork.Type.DINERS_CLUB -> Brand.DINERS_CLUB
        CardNetwork.Type.DISCOVER -> Brand.DISCOVER
        CardNetwork.Type.JCB -> Brand.JCB
        CardNetwork.Type.UNIONPAY -> Brand.UNIONPAY
        CardNetwork.Type.MIR -> Brand.MIR
        CardNetwork.Type.HIPER -> Brand.HIPER
        CardNetwork.Type.CARTES_BANCAIRES -> Brand.CARTES_BANCAIRES
        CardNetwork.Type.MAESTRO -> Brand.MEASTRO
        CardNetwork.Type.ELO -> Brand.ELO
        CardNetwork.Type.HIPERCARD -> Brand.GENERIC
        CardNetwork.Type.OTHER -> Brand.GENERIC
    }

@DrawableRes
fun CardNetwork.Type.getCardImageAsset(imageColor: ImageColor): Int =
    getCardBrand().getImageAsset(imageColor) ?: R.drawable.ic_generic_card
