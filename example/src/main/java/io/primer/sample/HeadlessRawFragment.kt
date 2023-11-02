package io.primer.sample

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.primer.android.completion.PrimerHeadlessUniversalCheckoutResumeDecisionHandler
import io.primer.android.completion.PrimerPaymentCreationDecisionHandler
import io.primer.android.components.PrimerHeadlessUniversalCheckout
import io.primer.android.components.PrimerHeadlessUniversalCheckoutListener
import io.primer.android.components.PrimerHeadlessUniversalCheckoutUiListener
import io.primer.android.components.SdkUninitializedException
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.domain.core.models.PrimerRawData
import io.primer.android.components.domain.core.models.bancontact.PrimerBancontactCardData
import io.primer.android.components.domain.core.models.bancontact.PrimerBancontactCardMetadata
import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.android.components.domain.core.models.card.PrimerCardMetadata
import io.primer.android.components.domain.core.models.metadata.PrimerPaymentMethodMetadata
import io.primer.android.components.domain.core.models.otp.PrimerOtpCodeData
import io.primer.android.components.domain.core.models.phoneNumber.PrimerPhoneNumberData
import io.primer.android.components.domain.core.models.retailOutlet.PrimerRetailerData
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.manager.raw.PrimerHeadlessUniversalCheckoutRawDataManager
import io.primer.android.components.manager.raw.PrimerHeadlessUniversalCheckoutRawDataManagerInterface
import io.primer.android.components.manager.raw.PrimerHeadlessUniversalCheckoutRawDataManagerListener
import io.primer.android.data.payments.configure.PrimerInitializationData
import io.primer.android.data.payments.configure.retailOutlets.RetailOutletsList
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.domain.action.models.PrimerClientSession
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.exception.UnsupportedPaymentIntentException
import io.primer.android.domain.payments.additionalInfo.PrimerCheckoutAdditionalInfo
import io.primer.android.domain.payments.additionalInfo.XenditCheckoutVoucherAdditionalInfo
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodData
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData
import io.primer.sample.databinding.FragmentHeadlessBinding
import io.primer.sample.datamodels.TransactionState
import io.primer.sample.repositories.AppApiKeyRepository
import io.primer.sample.viewmodels.HeadlessManagerViewModel
import io.primer.sample.viewmodels.HeadlessManagerViewModelFactory
import io.primer.sample.viewmodels.MainViewModel
import kotlin.math.pow

class HeadlessRawFragment : Fragment(), PrimerHeadlessUniversalCheckoutRawDataManagerListener {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var headlessManagerViewModel: HeadlessManagerViewModel

    private val headlessUniversalCheckout by lazy {
        PrimerHeadlessUniversalCheckout.current
    }
    private lateinit var rawDataManager: PrimerHeadlessUniversalCheckoutRawDataManagerInterface

    private var _binding: FragmentHeadlessBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentHeadlessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        headlessManagerViewModel = ViewModelProvider(
            requireActivity(),
            HeadlessManagerViewModelFactory(AppApiKeyRepository()),
        )[HeadlessManagerViewModel::class.java]

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

        headlessManagerViewModel.transactionState.observe(viewLifecycleOwner) { state ->
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

        headlessUniversalCheckout.setCheckoutListener(object :
            PrimerHeadlessUniversalCheckoutListener {
            override fun onAvailablePaymentMethodsLoaded(paymentMethods: List<PrimerHeadlessUniversalCheckoutPaymentMethod>) {
                Log.d(TAG, paymentMethods.toString())
                setupPaymentMethod(paymentMethods)
                hideLoading()
            }

            override fun onTokenizationStarted(paymentMethodType: String) {
                showLoading("Tokenization started $paymentMethodType")
            }

            override fun onTokenizeSuccess(
                paymentMethodTokenData: PrimerPaymentMethodTokenData,
                decisionHandler: PrimerHeadlessUniversalCheckoutResumeDecisionHandler
            ) {
                showLoading("Tokenization success $paymentMethodTokenData. Creating payment.")
                headlessManagerViewModel.createPayment(
                    paymentMethodTokenData, requireNotNull(viewModel.environment.value),
                    viewModel.descriptor.value.orEmpty(), decisionHandler
                )
            }

            override fun onCheckoutResume(
                resumeToken: String,
                decisionHandler: PrimerHeadlessUniversalCheckoutResumeDecisionHandler
            ) {
                showLoading("Resume success $resumeToken. Resuming payment.")
                headlessManagerViewModel.resumePayment(
                    resumeToken,
                    requireNotNull(viewModel.environment.value),
                    decisionHandler
                )
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

            override fun onCheckoutAdditionalInfoReceived(additionalInfo: PrimerCheckoutAdditionalInfo) {
                super.onCheckoutAdditionalInfoReceived(additionalInfo)
                when (additionalInfo) {
                    is XenditCheckoutVoucherAdditionalInfo -> {
                        AlertDialog.Builder(context)
                            .setMessage("Obtained additional info: $additionalInfo")
                            .show()
                    }
                }
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

        headlessUniversalCheckout.setCheckoutUiListener(object :
            PrimerHeadlessUniversalCheckoutUiListener {
            override fun onPreparationStarted(paymentMethodType: String) {
                showLoading("Preparation started $paymentMethodType")
                Log.d(TAG, "onPreparationStarted")
            }

            override fun onPaymentMethodShowed(paymentMethodType: String) {
                Log.d(TAG, "onPaymentMethodShowed $paymentMethodType")
            }

        })

        binding.nextButton.setOnClickListener {
            rawDataManager.submit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.setClientToken(null)
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
            is PrimerBancontactCardMetadata -> binding.pmView.findViewWithTag<TextInputLayout>(
                PrimerInputElementType.CARD_NUMBER
            ).prefixText = metadata.cardNetwork.name
        }
    }

    private fun setupPaymentMethod(paymentMethodTypes: List<PrimerHeadlessUniversalCheckoutPaymentMethod>) {
        paymentMethodTypes.forEach { paymentMethod ->
            if (paymentMethod.paymentMethodManagerCategories.contains(
                    PrimerPaymentMethodManagerCategory.RAW_DATA
                )
            ) {
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

    private fun createForm(
        paymentMethodType: String,
        requiredInputElementTypes: List<PrimerInputElementType>
    ) {
        val inputElements = requiredInputElementTypes.map { type ->
            TextInputLayout(requireContext()).apply {
                tag = type
                hint = getHint(type)
                addView(TextInputEditText(context).apply {
                    id = View.generateViewId()
                    doAfterTextChanged {
                        rawDataManager.setRawData(getRawData(paymentMethodType))
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

    private fun getRawData(paymentMethodType: String): PrimerRawData {
        return when (paymentMethodType) {
            "PAYMENT_CARD" -> PrimerCardData(
                binding.pmView.findViewWithTag<TextInputLayout>(
                    PrimerInputElementType.CARD_NUMBER
                ).editText?.text.toString(),
                binding.pmView.findViewWithTag<TextInputLayout>(
                    PrimerInputElementType.EXPIRY_DATE
                ).editText?.text.toString().trim(),
                binding.pmView.findViewWithTag<TextInputLayout>(
                    PrimerInputElementType.CVV
                ).editText?.text.toString(),
                binding.pmView.findViewWithTag<TextInputLayout>(
                    PrimerInputElementType.CARDHOLDER_NAME
                ).editText?.text.toString(),
            )
            "ADYEN_BANCONTACT_CARD" -> PrimerBancontactCardData(
                binding.pmView.findViewWithTag<TextInputLayout>(
                    PrimerInputElementType.CARD_NUMBER
                ).editText?.text.toString().trim(),
                binding.pmView.findViewWithTag<TextInputLayout>(
                    PrimerInputElementType.EXPIRY_DATE
                ).editText?.text.toString().trim(),
                binding.pmView.findViewWithTag<TextInputLayout>(
                    PrimerInputElementType.CARDHOLDER_NAME
                ).editText?.text.toString().trim(),
            )
            "XENDIT_OVO" -> PrimerPhoneNumberData(
                binding.pmView.findViewWithTag<TextInputLayout>(
                    PrimerInputElementType.PHONE_NUMBER
                ).editText?.text.toString()
            )
            "ADYEN_MBWAY" -> PrimerPhoneNumberData(
                binding.pmView.findViewWithTag<TextInputLayout>(
                    PrimerInputElementType.PHONE_NUMBER
                ).editText?.text.toString()
            )
            "ADYEN_BLIK" -> PrimerOtpCodeData(
                binding.pmView.findViewWithTag<TextInputLayout>(
                    PrimerInputElementType.OTP_CODE
                ).editText?.text.toString()
            )
            else -> throw IllegalArgumentException("Unsupported payment method type $paymentMethodType")
        }
    }

    private fun setupManager(paymentMethodType: String) {
        try {
            rawDataManager =
                PrimerHeadlessUniversalCheckoutRawDataManager.newInstance(paymentMethodType)
            rawDataManager.setListener(this)
            if (paymentMethodType == "XENDIT_RETAIL_OUTLETS") {
                rawDataManager.configure { primerInitializationData, error ->
                    if (error == null) {
                        showChooser(primerInitializationData)
                    } else {
                        AlertDialog.Builder(context)
                            .setMessage(error.description)
                            .show()
                    }
                }
            } else {
                createForm(paymentMethodType, rawDataManager.getRequiredInputElementTypes())
            }
        } catch (e: SdkUninitializedException) {
            AlertDialog.Builder(context).setMessage(e.message).setNegativeButton(
                android.R.string.cancel
            ) { _, _ -> findNavController().navigateUp() }.show()
        } catch (e: UnsupportedPaymentIntentException) {
            AlertDialog.Builder(context).setMessage(e.message).setNegativeButton(
                android.R.string.cancel
            ) { _, _ -> findNavController().navigateUp() }.show()
        }
    }

    private fun showChooser(primerInitializationData: PrimerInitializationData?) {
        when (primerInitializationData) {
            is RetailOutletsList -> {
                val retailerNames = primerInitializationData.result.map { it.name }.toTypedArray()
                val itemClick = DialogInterface.OnClickListener { dialog, which ->
                    if (which > -1) {
                        rawDataManager.setRawData(
                            PrimerRetailerData(
                                primerInitializationData.result[which].id
                            )
                        )

                        rawDataManager.submit()
                    }
                    dialog.dismiss()
                }
                AlertDialog.Builder(context)
                    .setItems(
                        retailerNames,
                        itemClick
                    )
                    .show()
            }
        }
    }

    companion object {
        private val TAG = this::class.simpleName
    }
}