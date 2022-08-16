package com.example.myapplication

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.icu.text.NumberFormat
import android.icu.util.Currency
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.myapplication.databinding.FragmentThirdBinding
import com.example.myapplication.datamodels.TransactionState
import com.example.myapplication.viewmodels.MainViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.primer.android.ExperimentalPrimerApi
import io.primer.android.completion.PrimerPaymentCreationDecisionHandler
import io.primer.android.completion.PrimerResumeDecisionHandler
import io.primer.android.components.PrimerHeadlessUniversalCheckout
import io.primer.android.components.PrimerHeadlessUniversalCheckoutListener
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.android.components.domain.core.models.card.PrimerRawCardData
import io.primer.android.components.domain.core.models.card.PrimerCardMetadata
import io.primer.android.components.domain.core.models.metadata.PrimerPaymentMethodMetadata
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.manager.raw.PrimerHeadlessUniversalCheckoutRawDataManager
import io.primer.android.components.manager.raw.PrimerHeadlessUniversalCheckoutRawDataManagerInterface
import io.primer.android.components.manager.raw.PrimerHeadlessUniversalCheckoutRawDataManagerListener
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.domain.action.models.PrimerClientSession
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodData
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData
import kotlin.math.pow

@OptIn(ExperimentalPrimerApi::class)
class HeadlessRawFragment : Fragment(), PrimerHeadlessUniversalCheckoutRawDataManagerListener {

    private val viewModel: MainViewModel by activityViewModels()
    private val headlessUniversalCheckout by lazy { PrimerHeadlessUniversalCheckout.current }
    private lateinit var rawDataManager: PrimerHeadlessUniversalCheckoutRawDataManagerInterface

    private var _binding: FragmentThirdBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentThirdBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.clientToken.observe(viewLifecycleOwner) { token ->
            token?.let {
                headlessUniversalCheckout.start(
                    requireContext(),
                    it,
                    viewModel.settings
                )
                showLoading("Starting HUC.")
            }
        }

        viewModel.transactionState.observe(viewLifecycleOwner) { state ->
            val message = when (state) {
                TransactionState.SUCCESS -> viewModel.transactionResponse.value.toString()
                TransactionState.ERROR -> requireContext().getString(R.string.something_went_wrong)
                else -> return@observe
            }
            hideLoading()
            AlertDialog.Builder(context).setMessage(message).show()
            viewModel.resetTransactionState()
        }

        viewModel.fetchClientSession()
        showLoading("Loading client token.")

        headlessUniversalCheckout.setListener(object : PrimerHeadlessUniversalCheckoutListener {
            override fun onAvailablePaymentMethodsLoaded(paymentMethods: List<PrimerHeadlessUniversalCheckoutPaymentMethod>) {
                Log.d(TAG, paymentMethods.toString())
                setupPaymentMethod(paymentMethods)
                hideLoading()
            }

            override fun onPreparationStarted(paymentMethodType: String) {
                showLoading("Preparation started $paymentMethodType")
                Log.d(TAG, "onPreparationStarted")
            }

            override fun onTokenizationStarted(paymentMethodType: String) {
                showLoading("Tokenization started $paymentMethodType")
            }

            override fun onPaymentMethodShowed(paymentMethodType: String) {
                Log.d(TAG, "onPaymentMethodShowed $paymentMethodType")
            }

            override fun onTokenizeSuccess(
                paymentMethodTokenData: PrimerPaymentMethodTokenData,
                decisionHandler: PrimerResumeDecisionHandler
            ) {
                showLoading("Tokenization success $paymentMethodTokenData. Creating payment.")
                viewModel.createPayment(paymentMethodTokenData, decisionHandler)
            }

            override fun onResumeSuccess(
                resumeToken: String,
                decisionHandler: PrimerResumeDecisionHandler
            ) {
                showLoading("Resume success $resumeToken. Resuming payment.")
                viewModel.resumePayment(resumeToken, decisionHandler)
            }

            override fun onBeforePaymentCreated(
                paymentMethodData: PrimerPaymentMethodData,
                createPaymentHandler: PrimerPaymentCreationDecisionHandler
            ) {
                super.onBeforePaymentCreated(paymentMethodData, createPaymentHandler)
                showLoading("On Before Payment Created with ${paymentMethodData.paymentMethodType}")
            }

            override fun onFailed(error: PrimerError, checkoutData: PrimerCheckoutData?) {
                hideLoading()
                AlertDialog.Builder(context)
                    .setMessage("On Failed $error with data $checkoutData")
                    .show()
            }

            override fun onCheckoutCompleted(checkoutData: PrimerCheckoutData) {
                hideLoading()
                AlertDialog.Builder(context).setMessage("On Checkout Completed $checkoutData")
                    .show()
            }

            @SuppressLint("NewApi")
            override fun onClientSessionUpdated(clientSession: PrimerClientSession) {
                super.onClientSessionUpdated(clientSession)
                val format = NumberFormat.getCurrencyInstance()
                val currency = Currency.getInstance(clientSession.currencyCode.orEmpty())
                format.maximumFractionDigits = currency.defaultFractionDigits
                format.minimumFractionDigits = currency.defaultFractionDigits
                format.currency = Currency.getInstance(clientSession.currencyCode.orEmpty())

                binding.nextButton.text =
                    format.format(clientSession.totalAmount?.toDouble()!! / 10.0.pow(currency.defaultFractionDigits))
            }
        })

        binding.nextButton.setOnClickListener {
            rawDataManager.submit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clientToken.postValue(null)
        headlessUniversalCheckout.cleanup()
        if (::rawDataManager.isInitialized) rawDataManager.cleanup()
        _binding = null
    }

    override fun onValidationChanged(
        isValid: Boolean,
        errors: List<PrimerInputValidationError>
    ) {
        binding.pmView.children.filterIsInstance<TextInputLayout>().iterator().forEach {
            it.error = null
        }
        errors.forEach {
            binding.pmView.findViewWithTag<TextInputLayout>(it.inputElementType).error =
                it.description
        }
        binding.nextButton.isEnabled = isValid
    }

    override fun onMetadataChanged(metadata: PrimerPaymentMethodMetadata) {
        when (metadata) {
            is PrimerCardMetadata -> binding.pmView.findViewWithTag<TextInputLayout>(
                PrimerInputElementType.CARD_NUMBER
            ).prefixText = metadata.cardNetwork.name
        }
    }

    private fun setupPaymentMethod(paymentMethodTypes: List<PrimerHeadlessUniversalCheckoutPaymentMethod>) {
        paymentMethodTypes.forEach { paymentMethod ->
            if (paymentMethod.requiredInputDataClass != null) {
                val viewGroup = (binding.parentView as ViewGroup)
                viewGroup.addView(Button(context).apply {
                    text = paymentMethod.paymentMethodType
                    setOnClickListener {
                        setupManager(paymentMethod.paymentMethodType)
                    }
                })
            }
        }
    }

    private fun createForm(requiredInputElementTypes: List<PrimerInputElementType>) {
        val inputElements = requiredInputElementTypes.map { type ->
            TextInputLayout(requireContext()).apply {
                tag = type
                hint = getHint(type)
                addView(TextInputEditText(context).apply {
                    id = View.generateViewId()
                    doAfterTextChanged {
                        rawDataManager.setRawData(getCardData())
                    }
                    layoutParams = LinearLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                    )
                })
            }
        }

        val viewGroup = (binding.pmView as ViewGroup)
        viewGroup.removeAllViews()
        inputElements.forEach {
            viewGroup.addView(it)
        }
    }

    private fun showLoading(message: String? = null) {
        binding.progressLayout.progressText.text = message
        binding.progressLayout.root.isVisible = true
    }

    private fun hideLoading() {
        binding.progressLayout.root.isVisible = false
    }

    private fun getHint(inputElementType: PrimerInputElementType): String {
        return when (inputElementType) {
            PrimerInputElementType.CARDHOLDER_NAME -> requireContext().getString(R.string.card_holder_name)
            PrimerInputElementType.CVV -> requireContext().getString(R.string.card_cvv)
            PrimerInputElementType.EXPIRY_DATE -> requireContext().getString(R.string.card_expiry)
            PrimerInputElementType.CARD_NUMBER -> requireContext().getString(R.string.card_number)
            PrimerInputElementType.POSTAL_CODE -> requireContext().getString(R.string.card_zip)
            else -> inputElementType.name
        }
    }

    private fun getCardData(): PrimerRawCardData {
        return PrimerRawCardData(
            binding.pmView.findViewWithTag<TextInputLayout>(
                PrimerInputElementType.CARD_NUMBER
            ).editText?.text.toString(),
            binding.pmView.findViewWithTag<TextInputLayout>(
                PrimerInputElementType.EXPIRY_DATE
            ).editText?.text.toString().substringBefore("/"),
            binding.pmView.findViewWithTag<TextInputLayout>(
                PrimerInputElementType.EXPIRY_DATE
            ).editText?.text.toString().substringAfter("/"),
            binding.pmView.findViewWithTag<TextInputLayout>(
                PrimerInputElementType.CVV
            ).editText?.text.toString(),
            binding.pmView.findViewWithTag<TextInputLayout>(
                PrimerInputElementType.CARDHOLDER_NAME
            ).editText?.text.toString(),
        )
    }

    private fun setupManager(paymentMethodType: String) {
        rawDataManager = PrimerHeadlessUniversalCheckoutRawDataManager.newInstance(paymentMethodType)
        rawDataManager.setManagerListener(this)
        createForm(rawDataManager.getRequiredInputElementTypes())
    }

    companion object {
        private val TAG = this::class.simpleName
    }
}