package io.primer.android.components.assets

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.BaseAnalyticsParams
import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.assets.ui.model.ViewProvider
import io.primer.android.assets.ui.model.getImageAsset
import io.primer.android.assets.ui.registry.BrandRegistry
import io.primer.android.components.assets.displayMetadata.PaymentMethodsImplementationInteractor
import io.primer.android.components.assets.extensions.dPtoPx
import io.primer.android.components.assets.extensions.scaleImage
import io.primer.android.components.assets.extensions.toResourcesScale
import io.primer.android.components.assets.ui.getCardBrand
import io.primer.android.components.assets.validation.resolvers.AssetManagerInitValidationRulesResolver
import io.primer.android.components.ui.assets.PrimerCardNetworkAsset
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.core.data.infrastructure.FileProvider
import io.primer.android.core.domain.None
import io.primer.android.core.domain.validation.ValidationResult
import io.primer.android.displayMetadata.domain.model.ImageColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

internal interface AssetsHeadlessDelegate {
    @ColorInt
    fun getPaymentMethodBackgroundColor(
        paymentMethodType: String,
        imageColor: ImageColor,
    ): Int?

    fun getPaymentMethodLogo(
        context: Context,
        paymentMethodType: String,
        imageColor: ImageColor,
    ): Drawable?

    fun getPaymentMethodName(paymentMethodType: String): String

    fun getPaymentMethodViewProvider(
        context: Context,
        paymentMethodType: String,
    ): ViewProvider

    @DrawableRes
    fun getCardNetworkImage(cardNetwork: CardNetwork.Type): Int

    fun getCardNetworkAsset(
        context: Context,
        cardNetwork: CardNetwork.Type,
    ): PrimerCardNetworkAsset

    fun getCardNetworkAssets(
        context: Context,
        cardNetworks: List<CardNetwork.Type>,
    ): List<PrimerCardNetworkAsset>

    fun getCurrentPaymentMethods(): List<String>
}

internal class DefaultAssetsHeadlessDelegate(
    private val initValidationRulesResolver: AssetManagerInitValidationRulesResolver,
    private val paymentMethodsImplementationInteractor: PaymentMethodsImplementationInteractor,
    private val imagesFileProvider: FileProvider,
    private val brandRegistry: BrandRegistry,
    private val analyticsInteractor: AnalyticsInteractor,
) : AssetsHeadlessDelegate {
    private val scope = CoroutineScope(SupervisorJob())

    override fun getPaymentMethodBackgroundColor(
        paymentMethodType: String,
        imageColor: ImageColor,
    ): Int? {
        checkIfInitialized()

        val backgroundColorMetadata =
            paymentMethodsImplementationInteractor.invoke(
                None,
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
        imageColor: ImageColor,
    ): Drawable? {
        checkIfInitialized()

        val cachedDrawable =
            Drawable.createFromPath(
                imagesFileProvider.getFile("${paymentMethodType}_$imageColor".lowercase())
                    .absolutePath,
            )
        val localResDrawableId = brandRegistry.getBrand(paymentMethodType).getImageAsset(imageColor)
        return when {
            cachedDrawable != null ->
                cachedDrawable.scaleImage(
                    context,
                    context.resources.displayMetrics.toResourcesScale() /
                        DEFAULT_EXPORTED_ICON_SCALE,
                    DEFAULT_EXPORTED_ICON_MAX_HEIGHT.dPtoPx(context),
                )

            localResDrawableId != 0 ->
                ContextCompat.getDrawable(
                    context,
                    localResDrawableId,
                )

            else -> null
        }
    }

    override fun getPaymentMethodName(paymentMethodType: String): String {
        checkIfInitialized()
        return paymentMethodsImplementationInteractor.execute(None)
            .find { it.paymentMethodType == paymentMethodType }?.name.orEmpty()
    }

    override fun getPaymentMethodViewProvider(
        context: Context,
        paymentMethodType: String,
    ): ViewProvider {
        checkIfInitialized()
        return brandRegistry.getBrand(paymentMethodType = paymentMethodType).viewProvider()
    }

    override fun getCardNetworkImage(cardNetwork: CardNetwork.Type): Int {
        logAnalyticsEvent(
            SdkFunctionParams(
                "getCardNetworkImage",
                mapOf("cardNetwork" to cardNetwork.name),
            ),
        )

        return cardNetwork.getCardBrand().iconResId
    }

    override fun getCardNetworkAsset(
        context: Context,
        cardNetwork: CardNetwork.Type,
    ): PrimerCardNetworkAsset {
        logAnalyticsEvent(
            SdkFunctionParams(
                "getCardNetworkAssets",
                mapOf("cardNetwork" to cardNetwork.name),
            ),
        )

        val cardBrand = cardNetwork.getCardBrand()
        return PrimerCardNetworkAsset(
            cardNetwork = cardNetwork,
            displayName = cardNetwork.displayName,
            cardBrand.getImageAsset(ImageColor.COLORED)?.let {
                ContextCompat.getDrawable(context, it)
            },
        )
    }

    override fun getCardNetworkAssets(
        context: Context,
        cardNetworks: List<CardNetwork.Type>,
    ): List<PrimerCardNetworkAsset> {
        logAnalyticsEvent(
            SdkFunctionParams(
                "getCardNetworkAssets",
                mapOf("cardNetworks" to cardNetworks.map { it.name }.toString()),
            ),
        )
        return cardNetworks.map { cardNetwork ->
            val cardBrand = cardNetwork.getCardBrand()
            PrimerCardNetworkAsset(
                cardNetwork,
                displayName = cardNetwork.displayName,
                cardBrand.getImageAsset(ImageColor.COLORED)?.let {
                    ContextCompat.getDrawable(context, it)
                },
            )
        }
    }

    override fun getCurrentPaymentMethods(): List<String> {
        checkIfInitialized()
        return paymentMethodsImplementationInteractor.execute(None)
            .map { it.paymentMethodType }
    }

    private fun logAnalyticsEvent(params: BaseAnalyticsParams) =
        scope.launch {
            analyticsInteractor(params)
        }

    private fun checkIfInitialized() {
        initValidationRulesResolver.resolve().rules.map {
            it.validate(Unit)
        }.filterIsInstance<ValidationResult.Failure>().forEach {
            throw it.exception
        }
    }

    private companion object {
        const val DEFAULT_EXPORTED_ICON_SCALE = 3.0f
        const val DEFAULT_EXPORTED_ICON_MAX_HEIGHT = 48.0f
    }
}
