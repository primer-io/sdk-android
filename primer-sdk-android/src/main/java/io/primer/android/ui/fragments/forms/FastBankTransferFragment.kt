package io.primer.android.ui.fragments.forms

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.postDelayed
import io.primer.android.R
import io.primer.android.databinding.FragmentFastBankTransferBinding
import io.primer.android.di.DISdkComponent
import io.primer.android.domain.payments.forms.models.Form
import io.primer.android.payment.async.AsyncPaymentMethodDescriptor
import io.primer.android.ui.extensions.autoCleaned
import io.primer.android.ui.extensions.copyTextToClipboard
import io.primer.android.ui.fragments.forms.binding.BaseFormBinding
import io.primer.android.ui.fragments.forms.binding.toBaseFormBinding
import java.text.DateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

internal class FastBankTransferFragment : BaseFormFragment(), DISdkComponent {

    private var binding: FragmentFastBankTransferBinding by autoCleaned()

    override val baseFormBinding: BaseFormBinding by autoCleaned { binding.toBaseFormBinding() }

    private val dueDateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFastBankTransferBinding.inflate(inflater, container, false)
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
        setupListeners()
    }

    private fun setupListeners() {
        binding.tvAccountCode.setOnClickListener {
            val success = copyTextToClipboard(binding.tvAccountCode.text.toString())
            animateCopyIcon(success)
        }
    }

    private fun animateCopyIcon(success: Boolean) {
        binding.tvAccountCode.apply {
            isEnabled = false
            setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                ContextCompat.getDrawable(
                    requireContext(),
                    if (success) {
                        R.drawable.ic_copy_clipboard_success
                    } else { R.drawable.ic_copy_clipboard_failed }
                ),
                null
            )
            postDelayed(DELAY_COPY_ICON) {
                setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_copy_clipboard
                    ),
                    null
                )
                isEnabled = true
            }
        }
    }

    override fun setupForm(form: Form) {
        super.setupForm(form)
        setupAccountCode(form.accountNumber)
        setupDueAt(form.expiration)
    }

    private fun setupDueAt(expiration: String?) {
        if (!expiration.isNullOrBlank()) {
            val date = Date(TimeUnit.SECONDS.toMillis(expiration.toLong()))
            binding.tvValueDueDate.text = dueDateFormat.format(date)
        }
    }

    private fun setupAccountCode(accountNumber: String?) {
        binding.tvAccountCode.text = accountNumber.orEmpty()
    }

    override fun setupBackIcon() {
        super.setupBackIcon()
        val backIcon = baseFormBinding.formBackIcon
        backIcon.setOnClickListener { parentFragmentManager.popBackStack() }
    }

    override fun onResume() {
        super.onResume()
        binding.tvAccountCode.isEnabled = true
    }

    companion object {

        private const val DELAY_COPY_ICON = 3000L

        private const val STATUS_URL_KEY = "STATUS_URL"
        private const val PAYMENT_METHOD_TYPE_KEY = "PAYMENT_METHOD_TYPE"

        fun newInstance(
            statusUrl: String,
            paymentMethodType: String
        ) = FastBankTransferFragment().apply {
            arguments = bundleOf(
                STATUS_URL_KEY to statusUrl,
                PAYMENT_METHOD_TYPE_KEY to paymentMethodType
            )
        }
    }
}
