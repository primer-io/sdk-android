package io.primer.android.ui.fragments.forms

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import io.primer.android.R
import io.primer.android.databinding.FragmentPromptPayBinding
import io.primer.android.di.DISdkComponent
import io.primer.android.di.extension.inject
import io.primer.android.domain.payments.forms.models.Form
import io.primer.android.payment.async.AsyncPaymentMethodDescriptor
import io.primer.android.ui.extensions.autoCleaned
import io.primer.android.ui.fragments.forms.binding.BaseFormBinding
import io.primer.android.ui.fragments.forms.binding.toBaseFormBinding
import io.primer.android.utils.ImageLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class PromptPayFragment : BaseFormFragment(), DISdkComponent {

    private var binding: FragmentPromptPayBinding by autoCleaned()

    override val baseFormBinding: BaseFormBinding by autoCleaned { binding.toBaseFormBinding() }

    private val base64Pattern by lazy {
        Regex("^([A-Za-z0-9+\\/]{4})*([A-Za-z0-9\\/]{3}=|[A-Za-z0-9\\/]{2}==)?\$")
    }

    private val imageLoader: ImageLoader by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPromptPayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.statusUrlLiveData.observe(viewLifecycleOwner) {
            val descriptor =
                primerViewModel.selectedPaymentMethod.value as AsyncPaymentMethodDescriptor
            descriptor.behaviours.forEach {
                primerViewModel.executeBehaviour(it)
            }
        }
        viewModel.getStatus(
            requireArguments().getString(STATUS_URL_KEY).orEmpty(),
            requireArguments().getString(PAYMENT_METHOD_TYPE_KEY).orEmpty()
        )
    }

    override fun setupForm(form: Form) {
        super.setupForm(form)
        setupQrCode(form.qrCodeUrl, form.qrCode)
    }

    private fun setupQrCode(qrCodeUrl: String?, qrCode: String?) {
        if (!qrCode.isNullOrBlank() && qrCode.contains(base64Pattern)) {
            lifecycleScope.launch(Dispatchers.Default) {
                val imageBytes = Base64.decode(qrCode, Base64.DEFAULT)
                val decodedImage =
                    BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                withContext(Dispatchers.Main) {
                    binding.ivQrImage.setImageBitmap(decodedImage)
                }
            }
        } else {
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.placeholder_qr
            )?.let { placeholder ->
                if (!qrCodeUrl.isNullOrBlank()) {
                    imageLoader.loadImage(
                        qrCodeUrl,
                        placeholder,
                        binding.ivQrImage
                    )
                }
            }
        }
    }

    override fun setupBackIcon() {
        super.setupBackIcon()
        val backIcon = baseFormBinding.formBackIcon
        backIcon.setOnClickListener { parentFragmentManager.popBackStack() }
    }

    companion object {

        private const val STATUS_URL_KEY = "STATUS_URL"
        private const val PAYMENT_METHOD_TYPE_KEY = "PAYMENT_METHOD_TYPE"

        fun newInstance(
            statusUrl: String,
            paymentMethodType: String
        ) = PromptPayFragment().apply {
            arguments = bundleOf(
                STATUS_URL_KEY to statusUrl,
                PAYMENT_METHOD_TYPE_KEY to paymentMethodType
            )
        }
    }
}
