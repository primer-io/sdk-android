@file:OptIn(ExperimentalCoroutinesApi::class)

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
import io.primer.android.core.di.DISdkComponent
import io.primer.android.databinding.FragmentMultibancoPaymentBinding
import io.primer.android.domain.payments.forms.models.Form
import io.primer.android.ui.extensions.autoCleaned
import io.primer.android.ui.extensions.copyTextToClipboard
import io.primer.android.ui.fragments.forms.BaseFormFragment
import io.primer.android.ui.fragments.forms.binding.BaseFormBinding
import io.primer.android.ui.fragments.forms.binding.toBaseFormBinding
import io.primer.android.viewmodel.ViewStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi

internal class MultibancoPaymentFragment : BaseFormFragment(), DISdkComponent {

    private var binding: FragmentMultibancoPaymentBinding by autoCleaned()

    override val baseFormBinding: BaseFormBinding by autoCleaned { binding.toBaseFormBinding() }

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
        val titleTextColor = theme.titleText.defaultColor.getColor(requireContext(), theme.isDarkMode)
        binding.tvTitleComplete.setTextColor(titleTextColor)
        binding.tvDescription.setTextColor(
            theme.subtitleText.defaultColor.getColor(requireContext(), theme.isDarkMode)
        )
        binding.tvValueDueDate.setTextColor(titleTextColor)
        binding.tvValueAmount.setTextColor(titleTextColor)
        binding.tvValueReference.setTextColor(titleTextColor)
        binding.tvValueEntity.setTextColor(titleTextColor)
        val imageColorStates = ColorStateList.valueOf(titleTextColor)
        binding.ivShare.imageTintList = imageColorStates
    }

    private fun setupAmount() {
        binding.tvValueAmount.text = primerViewModel.getTotalAmountFormatted()
    }

    override fun setupForm(form: Form) {
        super.setupForm(form)
        val entity = requireArguments().getString(ENTITY_KEY).orEmpty()
        val reference = requireArguments().getString(REFERENCE_KEY).orEmpty()
        val expiresAt = requireArguments().getString(EXPIRES_AT_KEY).orEmpty()
        setupDueAt(expiresAt)
        setupReferences(entity, reference)
        setupShareButton(entity, reference, expiresAt)
    }

    private fun setupShareButton(entity: String, reference: String, expiresAt: String) {
        binding.ivShare.setOnClickListener {
            shareContent(
                buildString {
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
            )
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

    private fun setupDueAt(expiresAt: String) {
        binding.tvValueDueDate.text = expiresAt
    }

    private fun setupReferences(entity: String, reference: String) {
        binding.tvValueEntity.text = entity.orEmpty()
        binding.tvValueReference.text = reference.orEmpty()
    }

    override fun setupBackIcon() {
        super.setupBackIcon()
        val backIcon = baseFormBinding.formBackIcon
        backIcon.setOnClickListener {
            primerViewModel.setViewStatus(ViewStatus.SelectPaymentMethod)
        }
    }

    companion object {
        private const val ENTITY_KEY = "entity"
        private const val REFERENCE_KEY = "reference"
        private const val EXPIRES_AT_KEY = "expiresAt"

        fun newInstance(entity: String, reference: String, expiresAt: String) = MultibancoPaymentFragment().apply {
            arguments = bundleOf(
                ENTITY_KEY to entity,
                REFERENCE_KEY to reference,
                EXPIRES_AT_KEY to expiresAt
            )
        }
    }
}
