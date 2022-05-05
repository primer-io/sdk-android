package io.primer.android.ui.fragments.forms

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import io.primer.android.databinding.FragmentQrCodeBinding
import io.primer.android.domain.payments.forms.models.Form
import io.primer.android.model.dto.PaymentMethodType
import io.primer.android.payment.async.AsyncPaymentMethodDescriptor
import io.primer.android.ui.PayAmountText
import io.primer.android.ui.extensions.autoCleaned
import io.primer.android.ui.fragments.forms.binding.BaseFormBinding
import io.primer.android.ui.fragments.forms.binding.toBaseFormBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.core.component.KoinApiExtension

@ExperimentalCoroutinesApi
@KoinApiExtension
internal class QrCodeFragment : BaseFormFragment() {

    private var binding: FragmentQrCodeBinding by autoCleaned()

    override val baseFormBinding: BaseFormBinding by autoCleaned { binding.toBaseFormBinding() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentQrCodeBinding.inflate(inflater, container, false)
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
        setupAmount()
        setupQrCodeImage(form.qrCode.orEmpty())
    }

    override fun setupBackIcon() {
        super.setupBackIcon()
        val backIcon = baseFormBinding.formBackIcon
        backIcon.setOnClickListener {
            parentFragmentManager.popBackStack(
                parentFragmentManager.getBackStackEntryAt(0).id,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }
    }

    private fun setupAmount() {
        binding.formAmount.text = PayAmountText.generate(
            requireContext(),
            primerViewModel.amountLabelMonetaryAmount(
                primerViewModel.selectedPaymentMethod.value?.config?.type?.name.orEmpty()
            )
        )
    }

    private fun setupQrCodeImage(qrCode: String) {
        val decodedString = Base64.decode(qrCode, Base64.DEFAULT)
        binding.formImage.setImageBitmap(
            BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        )
    }

    companion object {

        private const val STATUS_URL_KEY = "STATUS_URL"
        private const val PAYMENT_METHOD_TYPE_KEY = "PAYMENT_METHOD_TYPE"

        fun newInstance(
            statusUrl: String,
            paymentMethodType: PaymentMethodType
        ) = QrCodeFragment().apply {
            arguments = bundleOf(
                STATUS_URL_KEY to statusUrl,
                PAYMENT_METHOD_TYPE_KEY to paymentMethodType
            )
        }
    }
}
