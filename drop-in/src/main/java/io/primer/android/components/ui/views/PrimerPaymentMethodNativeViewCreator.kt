package io.primer.android.components.ui.views

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import io.primer.android.components.ui.assets.PrimerPaymentMethodNativeView
import io.primer.android.databinding.PrimerPaymentMethodNativeButtonBinding

internal class PrimerPaymentMethodNativeViewCreator(
    private val paymentMethodNativeView: PrimerPaymentMethodNativeView,
) : PaymentMethodViewCreator {
    override fun create(
        context: Context,
        container: ViewGroup?,
    ): View {
        return PrimerPaymentMethodNativeButtonBinding.inflate(
            LayoutInflater.from(context),
            container,
            false,
        ).let { binding ->
            binding.paymentMethodParent.addView(
                paymentMethodNativeView.createView(context).apply {
                    layoutParams =
                        ViewGroup.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                        )

                    setOnClickListener {
                        binding.paymentMethodParent.performClick()
                    }
                },
            )
            binding.root
        }
    }
}
