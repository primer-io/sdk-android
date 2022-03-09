package com.example.myapplication

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.myapplication.databinding.FragmentThirdBinding
import com.example.myapplication.datamodels.TransactionState
import com.example.myapplication.viewmodels.MainViewModel
import io.primer.android.completion.ResumeHandler
import io.primer.android.components.manager.PrimerCardManager
import io.primer.android.components.PrimerHeadlessUniversalCheckoutListener
import io.primer.android.components.PrimerHeadlessUniversalCheckout
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.android.components.manager.PrimerCardManagerListener
import io.primer.android.components.ui.widgets.PrimerEditTextFactory
import io.primer.android.model.dto.PaymentMethodType
import io.primer.android.ui.CardType
import io.primer.android.components.ui.widgets.elements.PrimerInputElement
import io.primer.android.components.ui.widgets.elements.PrimerInputElementListener
import io.primer.android.components.ui.widgets.elements.PrimerInputElementType
import io.primer.android.model.PrimerDebugOptions
import io.primer.android.model.dto.APIError
import io.primer.android.model.dto.Options
import io.primer.android.model.dto.PaymentMethodToken
import io.primer.android.model.dto.PrimerConfig
import io.primer.android.model.dto.PrimerPaymentMethodType
import io.primer.android.model.dto.PrimerSettings

class HeadlessComponentsFragment : Fragment(), PrimerInputElementListener {

    private val margin by lazy { requireContext().resources.getDimensionPixelOffset(R.dimen.medium_vertical_margin) }
    private val viewModel: MainViewModel by activityViewModels()
    private val headlessUniversalCheckout by lazy { PrimerHeadlessUniversalCheckout.current }
    private val cardManager by lazy { PrimerCardManager.newInstance() }

    private val inputElementListener = object : PrimerInputElementListener {
        override fun inputElementValueChanged(inputElement: PrimerInputElement) {
            /* TODO */
        }

        override fun inputElementValueIsValid(inputElement: PrimerInputElement, isValid: Boolean) {
            /* TODO */
        }

        override fun inputElementDidDetectCardType(type: CardType.Type) {
            /* TODO */
        }
    }

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
                    PrimerConfig(
                        settings = PrimerSettings(
                            options = Options(
                                preferWebView = true,
                                debugOptions = PrimerDebugOptions(is3DSSanityCheckEnabled = false),
                                redirectScheme = "primer"
                            )
                        ),
                    )
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

        cardManager.setCardManagerListener(object : PrimerCardManagerListener {
            override fun onCardValidationChanged(isCardFormValid: Boolean) {
                Log.d(TAG, "onCardValidChanged $isCardFormValid")
                binding.nextButton.isEnabled = isCardFormValid
            }
        })

        headlessUniversalCheckout.setListener(object : PrimerHeadlessUniversalCheckoutListener {
            override fun onClientSessionSetupSuccessfully(paymentMethods: List<PrimerHeadlessUniversalCheckoutPaymentMethod>) {
                Log.d(TAG, paymentMethods.toString())
                setupPaymentMethod(paymentMethods)
                hideLoading()
            }

            override fun onTokenizationPreparation() {
                showLoading("Tokenization preparation started")
                Log.d(TAG, "onTokenizationPreparation")
            }

            override fun onTokenizationStarted() {
                showLoading("Tokenization started")
            }

            override fun onPaymentMethodShowed() {
                Log.d(TAG, "onPaymentMethodShowed")
            }

            override fun onTokenizationSuccess(
                paymentMethodToken: PaymentMethodToken,
                resumeHandler: ResumeHandler
            ) {
                showLoading("Tokenization success. Creating payment.")
                viewModel.createPayment(paymentMethodToken, resumeHandler)
            }

            override fun onResumeSuccess(resumeToken: String, resumeHandler: ResumeHandler) {
                showLoading("Resume success. Resuming payment.")
                viewModel.resumePayment(resumeToken, resumeHandler)
            }

            override fun onError(error: APIError) {
                hideLoading()
                Toast.makeText(requireContext(), error.description, Toast.LENGTH_SHORT).show()
            }
        })

        binding.nextButton.setOnClickListener {
            cardManager.tokenize()
        }
    }

    override fun inputElementValueChanged(inputElement: PrimerInputElement) {
        Log.d(TAG, "inputElementValueChanged ${inputElement.getType()}")
        binding.nextButton.isEnabled = cardManager.isCardFormValid()
    }

    override fun inputElementValueIsValid(inputElement: PrimerInputElement, isValid: Boolean) {
        Log.d(TAG, "inputElementValueIsValid ${inputElement.getType()} $isValid")
    }

    override fun inputElementDidDetectCardType(type: CardType.Type) {
        Log.d(TAG, "inputElementDidDetectCardType $type")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clientToken.postValue(null)
        headlessUniversalCheckout.cleanup()
        _binding = null
    }

    private fun setupPaymentMethod(paymentMethodTypes: List<PrimerHeadlessUniversalCheckoutPaymentMethod>) {
        paymentMethodTypes.forEach {
            when (it.paymentMethodType) {
                PaymentMethodType.PAYMENT_CARD -> createForm(
                    cardManager.getRequiredInputElementTypes().orEmpty()
                )
                else -> addPaymentMethodView(it.paymentMethodType)
            }
        }
    }

    private fun createForm(requiredInputElementTypes: List<PrimerInputElementType>) {
        val inputElements = requiredInputElementTypes.map { type ->
            PrimerEditTextFactory.createFromType(requireContext(), type).apply {
                setHint(getHint(type))
                setPrimerInputElementListener(inputElementListener)
            }
        }

        val viewGroup = (binding.parentView as ViewGroup)
        inputElements.forEach {
            viewGroup.addView(it)
        }

        cardManager.setInputElements(inputElements)
    }

    private fun addPaymentMethodView(paymentMethodType: PrimerPaymentMethodType) {
        val pmViewGroup = (binding.pmView as ViewGroup)

        headlessUniversalCheckout.makeView(paymentMethodType)?.apply {
            layoutParams = layoutParams.apply {
                val layoutParams = this as LinearLayout.LayoutParams
                layoutParams.topMargin = margin
                layoutParams.bottomMargin = margin
            }
            pmViewGroup.addView(this)
            setOnClickListener {
                headlessUniversalCheckout.showPaymentMethod(
                    requireContext(),
                    paymentMethodType
                )
            }
        }
    }

    private fun showLoading(message: String? = null) {
        binding.progressLayout.progressText.text = message
        binding.progressLayout.root.isVisible = true
    }

    private fun hideLoading() {
        binding.progressLayout.root.isVisible = false
    }

    private fun getHint(inputElementType: PrimerInputElementType): Int {
        return when (inputElementType) {
            PrimerInputElementType.CARDHOLDER_NAME -> R.string.card_holder_name
            PrimerInputElementType.CVV -> R.string.card_cvv
            PrimerInputElementType.EXPIRY_DATE -> R.string.card_expiry
            PrimerInputElementType.CARD_NUMBER -> R.string.card_number
            PrimerInputElementType.POSTAL_CODE -> R.string.card_zip
        }
    }

    companion object {
        private val TAG = this::class.simpleName
    }
}