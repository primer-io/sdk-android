@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.android.ui.fragments.forms

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import io.primer.android.R
import io.primer.android.components.utils.ImageLoader
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.inject
import io.primer.android.databinding.PrimerFragmentQrCodeBinding
import io.primer.android.domain.payments.forms.models.Form
import io.primer.android.ui.extensions.autoCleaned
import io.primer.android.ui.extensions.getParentDialogOrNull
import io.primer.android.ui.extensions.popBackStackToRoot
import io.primer.android.ui.fragments.CheckoutSheetFragment
import io.primer.android.ui.fragments.forms.binding.BaseFormBinding
import io.primer.android.ui.fragments.forms.binding.toBaseFormBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class QrCodeFragment : BaseFormFragment(), DISdkComponent {
    private var binding: PrimerFragmentQrCodeBinding by autoCleaned()

    override val baseFormBinding: BaseFormBinding by autoCleaned { binding.toBaseFormBinding() }

    private val base64Pattern by lazy {
        Regex("^([A-Za-z0-9+\\/]{4})*([A-Za-z0-9\\/]{3}=|[A-Za-z0-9\\/]{2}==)?\$")
    }

    private val imageLoader: ImageLoader by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addOnBackPressedCallback()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = PrimerFragmentQrCodeBinding.inflate(inflater, container, false)
        with(binding.tvAmount) {
            isGone = false
            text = primerViewModel.getTotalAmountFormatted()
        }
        return binding.root
    }

    override fun setupForm(form: Form) {
        super.setupForm(form)

        val qrCodeUrl = requireArguments().getString(QR_CODE_URL_KEY)
        val qrCodeBase64 = requireArguments().getString(QR_CODE_BASE_64_KEY)

        setupQrCode(qrCodeUrl, qrCodeBase64)
    }

    override fun setupBackIcon() {
        super.setupBackIcon()
        getToolbar()?.getBackButton()?.setOnClickListener {
            logAnalyticsBackPressed()
            popBackStackAndCleanupManager()
        }
    }

    private fun setupQrCode(
        qrCodeUrl: String?,
        qrCodeBase64: String?,
    ) {
        if (!qrCodeBase64.isNullOrBlank() && qrCodeBase64.contains(base64Pattern)) {
            lifecycleScope.launch(Dispatchers.Default) {
                val imageBytes = Base64.decode(qrCodeBase64, Base64.DEFAULT)
                val decodedImage =
                    BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                withContext(Dispatchers.Main) {
                    binding.ivQrImage.setImageBitmap(decodedImage)
                }
            }
        } else {
            ContextCompat.getDrawable(requireContext(), R.drawable.placeholder_qr)?.let { placeholder ->
                if (!qrCodeUrl.isNullOrBlank()) {
                    imageLoader.loadImage(
                        qrCodeUrl,
                        placeholder,
                        binding.ivQrImage,
                    )
                }
            }
        }
    }

    //region Utils
    private fun addOnBackPressedCallback() {
        getParentDialogOrNull()?.onBackPressedDispatcher?.addCallback(this) {
            popBackStackAndCleanupManager()
        }
    }

    private fun popBackStackAndCleanupManager() {
        (parentFragment as? CheckoutSheetFragment)?.popBackStackToRoot()
        primerViewModel.clearSelectedPaymentMethodNativeUiManager()
    }
    //endregion

    companion object {
        private const val QR_CODE_BASE_64_KEY = "QR_CODE_BASE_64"
        private const val QR_CODE_URL_KEY = "QR_CODE_URL"
        private const val STATUS_URL_KEY = "STATUS_URL"
        private const val PAYMENT_METHOD_TYPE_KEY = "PAYMENT_METHOD_TYPE"

        fun newInstance(
            qrCodeBase64: String?,
            qrCodeUrl: String?,
            statusUrl: String,
            paymentMethodType: String,
        ) = QrCodeFragment().apply {
            arguments =
                bundleOf(
                    QR_CODE_BASE_64_KEY to qrCodeBase64,
                    QR_CODE_URL_KEY to qrCodeUrl,
                    STATUS_URL_KEY to statusUrl,
                    PAYMENT_METHOD_TYPE_KEY to paymentMethodType,
                )
        }
    }
}
