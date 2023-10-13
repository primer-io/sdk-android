package io.primer.android.ui.fragments.multibanko

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import io.primer.android.R
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.databinding.FragmentMultibancoPaymentBinding
import io.primer.android.di.DISdkComponent
import io.primer.android.di.extension.inject
import io.primer.android.domain.payments.forms.models.Form
import io.primer.android.ui.PayAmountText
import io.primer.android.ui.extensions.autoCleaned
import io.primer.android.ui.extensions.copyTextToClipboard
import io.primer.android.ui.fragments.forms.BaseFormFragment
import io.primer.android.ui.fragments.forms.binding.BaseFormBinding
import io.primer.android.ui.fragments.forms.binding.toBaseFormBinding
import io.primer.android.viewmodel.ViewStatus
import java.text.DateFormat
import java.text.SimpleDateFormat

internal class MultibancoPaymentFragment : BaseFormFragment(), DISdkComponent {

    private var binding: FragmentMultibancoPaymentBinding by autoCleaned()

    override val baseFormBinding: BaseFormBinding by autoCleaned { binding.toBaseFormBinding() }

    private val localConfig: PrimerConfig by inject()

    private val expiresAtDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    private val dueDateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMultibancoPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAmount()
        setupTheme()
    }

    private fun setupTheme() {
        binding.tvTitleComplete.setTextColor(
            theme.titleText.defaultColor.getColor(
                requireContext(),
                theme.isDarkMode
            )
        )
        binding.tvDescription.setTextColor(
            theme.subtitleText.defaultColor.getColor(
                requireContext(),
                theme.isDarkMode
            )
        )
        binding.tvValueDueDate.setTextColor(
            theme.titleText.defaultColor.getColor(
                requireContext(),
                theme.isDarkMode
            )
        )
        binding.tvValueAmount.setTextColor(
            theme.titleText.defaultColor.getColor(
                requireContext(),
                theme.isDarkMode
            )
        )
        binding.tvValueReference.setTextColor(
            theme.titleText.defaultColor.getColor(
                requireContext(),
                theme.isDarkMode
            )
        )
        binding.tvValueEntity.setTextColor(
            theme.titleText.defaultColor.getColor(
                requireContext(),
                theme.isDarkMode
            )
        )
        val imageColorStates = ColorStateList.valueOf(
            theme.titleText.defaultColor.getColor(
                requireContext(),
                theme.isDarkMode
            )
        )
        binding.ivShare.imageTintList = imageColorStates
    }

    private fun setupAmount() {
        binding.tvValueAmount.text = PayAmountText.generate(
            requireContext(),
            localConfig.monetaryAmount
        )
    }

    override fun setupForm(form: Form) {
        super.setupForm(form)
        setupReferences(form.entity, form.reference)
        val expiresAt = setupDueAt(form.expiresAt)
        setupShareButton(form.entity, form.reference, expiresAt)
    }

    private fun setupShareButton(entity: String?, reference: String?, expiresAt: String?) {
        val paymentInfoData = buildString {
            append(getString(R.string.multibancoEntity))
            append(": ")
            appendLine(entity)
            append(getString(R.string.multibancoReference))
            append(": ")
            appendLine(reference)
            append(getString(R.string.dueAt))
            append(": ")
            append(expiresAt)
        }
        binding.ivShare.setOnClickListener {
            shareContent(paymentInfoData)
        }
    }

    private fun shareContent(paymentInfoData: String) {
        try {
            activity?.startActivity(
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, paymentInfoData)
                }
            )
        } catch (e: ActivityNotFoundException) { // not have an app for share plain text
            copyTextToClipboard(paymentInfoData)
            Log.e("Primer", e.localizedMessage ?: "Can't share content, device not support")
        }
    }

    private fun setupDueAt(expiresAt: String?): String? {
        return if (!expiresAt.isNullOrBlank()) {
            expiresAtDateFormat.parse(expiresAt)?.let { date ->
                dueDateFormat.format(date).apply {
                    binding.tvValueDueDate.text = this
                }
            }
        } else { null }
    }

    private fun setupReferences(entity: String?, reference: String?) {
        binding.tvValueEntity.text = entity.orEmpty()
        binding.tvValueReference.text = reference.orEmpty()
    }

    override fun setupBackIcon() {
        super.setupBackIcon()
        val backIcon = baseFormBinding.formBackIcon
        backIcon.setOnClickListener {
            primerViewModel.viewStatus.value = ViewStatus.SELECT_PAYMENT_METHOD
        }
    }

    companion object {

        private const val STATUS_URL_KEY = "STATUS_URL"
        private const val PAYMENT_METHOD_TYPE_KEY = "PAYMENT_METHOD_TYPE"

        fun newInstance(
            statusUrl: String,
            paymentMethodType: String
        ) = MultibancoPaymentFragment().apply {
            arguments = bundleOf(
                STATUS_URL_KEY to statusUrl,
                PAYMENT_METHOD_TYPE_KEY to paymentMethodType
            )
        }
    }
}
