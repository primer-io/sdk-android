package io.primer.android.components.presentation.assets

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.BaseAnalyticsParams
import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.components.domain.assets.validation.resolvers.AssetManagerInitValidationRulesResolver
import io.primer.android.components.domain.core.validation.ValidationResult
import io.primer.android.components.ui.assets.ImageColor
import io.primer.android.components.ui.assets.PrimerCardNetworkAsset
import io.primer.android.components.ui.views.PaymentMethodViewCreator
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.configuration.models.getImageAsset
import io.primer.android.domain.base.None
import io.primer.android.domain.payments.displayMetadata.PaymentMethodsImplementationInteractor
import io.primer.android.domain.payments.methods.PaymentMethodModulesInteractor
import io.primer.android.infrastructure.files.ImagesFileProvider
import io.primer.android.ui.CardNetwork
import io.primer.android.ui.extensions.scaleImage
import io.primer.android.utils.dPtoPx
import io.primer.android.utils.toResourcesScale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

internal interface AssetsHeadlessDelegate {

    @ColorInt
    fun getPaymentMethodBackgroundColor(
        paymentMethodType: String,
        imageColor: ImageColor
    ): Int?

    fun getPaymentMethodLogo(
        context: Context,
        paymentMethodType: String,
        imageColor: ImageColor
    ): Drawable?

    fun getPaymentMethodName(paymentMethodType: String): String

    @DrawableRes
    fun getCardNetworkImage(cardNetwork: CardNetwork.Type): Int

    fun getCardNetworkAsset(
        context: Context,
        cardNetwork: CardNetwork.Type
    ): PrimerCardNetworkAsset

    fun getCardNetworkAssets(
        context: Context,
        cardNetworks: List<CardNetwork.Type>
    ): List<PrimerCardNetworkAsset>

    fun getCurrentPaymentMethods(): List<String>
}

internal class DefaultAssetsHeadlessDelegate(
    private val initValidationRulesResolver: AssetManagerInitValidationRulesResolver,
    private val paymentMethodsImplementationInteractor: PaymentMethodsImplementationInteractor,
    private val paymentMethodModulesInteractor: PaymentMethodModulesInteractor,
    private val analyticsInteractor: AnalyticsInteractor
) : AssetsHeadlessDelegate {

    private val scope = CoroutineScope(SupervisorJob())

    override fun getPaymentMethodBackgroundColor(
        paymentMethodType: String,
        imageColor: ImageColor
    ): Int? {
        checkIfInitialized()

        val backgroundColorMetadata = paymentMethodsImplementationInteractor.invoke(
            None()
        ).find {
            it.paymentMethodType == paymentMethodType
        }?.buttonMetadata?.backgroundColor
        return when (imageColor) {
            ImageColor.COLORED -> backgroundColorMetadata?.colored
            ImageColor.LIGHT -> backgroundColorMetadata?.light
            ImageColor.DARK -> backgroundColorMetadata?.dark
        }?.let { Color.parseColor(it) }
    }

    override fun getPaymentMethodLogo(
        context: Context,
        paymentMethodType: String,
        imageColor: ImageColor
    ): Drawable? {
        checkIfInitialized()

        val imagesFileProvider = ImagesFileProvider(context)
        val cachedDrawable =
            Drawable.createFromPath(
                imagesFileProvider.getFile("${paymentMethodType}_$imageColor".lowercase())
                    .absolutePath
            )
        val localResDrawableId =
            PaymentMethodType.safeValueOf(paymentMethodType).brand.getImageAsset(imageColor)
        return when {
            cachedDrawable != null -> cachedDrawable.scaleImage(
                context,
                context.resources.displayMetrics.toResourcesScale() /
                    PaymentMethodViewCreator.DEFAULT_EXPORTED_ICON_SCALE,
                PaymentMethodViewCreator.DEFAULT_EXPORTED_ICON_MAX_HEIGHT
                    .dPtoPx(context)
            )

            localResDrawableId != 0 -> ContextCompat.getDrawable(
                context,
                localResDrawableId
            )

            else -> null
        }
    }

    override fun getPaymentMethodName(paymentMethodType: String): String {
        checkIfInitialized()
        return paymentMethodModulesInteractor.getPaymentMethodDescriptors()
            .find { it.config.type == paymentMethodType }?.let { it.config.name }.orEmpty()
    }

    override fun getCardNetworkImage(
        cardNetwork: CardNetwork.Type
    ): Int {
        logAnalyticsEvent(
            SdkFunctionParams(
                "getCardNetworkImage",
                mapOf("cardNetwork" to cardNetwork.name)
            )
        )

        return cardNetwork.getCardBrand().iconResId
    }

    override fun getCardNetworkAsset(
        context: Context,
        cardNetwork: CardNetwork.Type
    ): PrimerCardNetworkAsset {
        logAnalyticsEvent(
            SdkFunctionParams(
                "getCardNetworkAssets",
                mapOf("cardNetwork" to cardNetwork.name)
            )
        )

        val cardBrand = cardNetwork.getCardBrand()
        return PrimerCardNetworkAsset(
            cardNetwork,
            cardBrand.displayName,
            cardBrand.getImageAsset(ImageColor.COLORED)?.let {
                ContextCompat.getDrawable(context, it)
            }
        )
    }

    override fun getCardNetworkAssets(
        context: Context,
        cardNetworks: List<CardNetwork.Type>
    ): List<PrimerCardNetworkAsset> {
        logAnalyticsEvent(
            SdkFunctionParams(
                "getCardNetworkAssets",
                mapOf("cardNetworks" to cardNetworks.map { it.name }.toString())
            )
        )
        return cardNetworks.map { cardNetwork ->
            val cardBrand = cardNetwork.getCardBrand()
            PrimerCardNetworkAsset(
                cardNetwork,
                cardBrand.displayName,
                cardBrand.getImageAsset(ImageColor.COLORED)?.let {
                    ContextCompat.getDrawable(context, it)
                }
            )
        }
    }

    override fun getCurrentPaymentMethods(): List<String> {
        checkIfInitialized()
        return paymentMethodModulesInteractor.getPaymentMethodDescriptors()
            .map { it.config.type }
    }

    private fun logAnalyticsEvent(params: BaseAnalyticsParams) = scope.launch {
        analyticsInteractor(params).collect()
    }

    private fun checkIfInitialized() {
        initValidationRulesResolver.resolve().rules.map {
            it.validate(Unit)
        }.filterIsInstance<ValidationResult.Failure>().forEach {
            throw it.exception
        }
    }
}
