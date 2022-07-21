package io.primer.android.ui.fragments.forms

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import io.primer.android.R
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.databinding.FragmentPromptPayBinding
import io.primer.android.di.DIAppComponent
import io.primer.android.domain.payments.forms.models.Form
import io.primer.android.payment.async.AsyncPaymentMethodDescriptor
import io.primer.android.ui.extensions.autoCleaned
import io.primer.android.ui.fragments.forms.binding.BaseFormBinding
import io.primer.android.ui.fragments.forms.binding.toBaseFormBinding
import io.primer.android.utils.ImageLoader
import org.koin.core.component.inject

internal class PromptPayFragment : BaseFormFragment(), DIAppComponent {

    private var binding: FragmentPromptPayBinding by autoCleaned()

    override val baseFormBinding: BaseFormBinding by autoCleaned { binding.toBaseFormBinding() }

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
            requireArguments().getSerializable(PAYMENT_METHOD_TYPE_KEY) as PaymentMethodType
        )
    }

    override fun setupForm(form: Form) {
        super.setupForm(form)
        setupQrCode(form.qrCodeUrl)
    }

    private fun setupQrCode(qrCodeUrl: String?) {
        if (!qrCodeUrl.isNullOrBlank()) {
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.placeholder_qr
            )?.let { placeholder ->
                imageLoader.loadImage(
                    qrCodeUrl,
                    placeholder,
                    binding.ivQrImage
                )
            }
            binding.ivQrImage.setOnClickListener {
                startActivity(Intent.parseUri(qrCodeUrl, Intent.URI_ALLOW_UNSAFE))
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
            paymentMethodType: PaymentMethodType
        ) = PromptPayFragment().apply {
            arguments = bundleOf(
                STATUS_URL_KEY to statusUrl,
                PAYMENT_METHOD_TYPE_KEY to paymentMethodType
            )
        }
    }
}
