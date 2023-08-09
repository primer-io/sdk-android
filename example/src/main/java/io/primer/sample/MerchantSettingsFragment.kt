package io.primer.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import io.primer.android.data.settings.PrimerPaymentHandling
import io.primer.sample.databinding.FragmentSettingsBinding
import io.primer.sample.datamodels.PrimerEnv
import io.primer.sample.utils.HideKeyboardFocusChangeListener
import io.primer.sample.utils.MoneyTextWatcher
import io.primer.sample.viewmodels.MainViewModel
import io.primer.sample.viewmodels.SettingsViewModel

class MerchantSettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()
    private val settingsViewModel: SettingsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureFlowToggleViews()
        configureClientTokenTextField()
        configureCustomerIdTextField()
        configureCountryTextField()
        configureAmountTextField()
        configurePaymentHandlingViews()
        configureSdkUiSettingsViews()
        configureEnvSetup()
        configureNextButton()
        configureEnvDropDown()

        viewModel.canLaunchPrimer.observe(viewLifecycleOwner) { canLaunch ->
            binding.universalCheckoutButton.isEnabled = canLaunch
        }

        settingsViewModel.country.observe(viewLifecycleOwner) { country ->
            viewModel.countryCode.postValue(country)
            binding.countryItem.setText(country.flag)
        }
        viewModel.environment.observe(viewLifecycleOwner) { env ->
            when (env) {
                PrimerEnv.Sandbox -> binding.dropDownEnvironment.setSelection(ENV_SANDBOX_ID)
                PrimerEnv.Dev -> binding.dropDownEnvironment.setSelection(ENV_DEV_ID)
                PrimerEnv.Staging -> binding.dropDownEnvironment.setSelection(ENV_STAGING_ID)
                PrimerEnv.Production -> binding.dropDownEnvironment.setSelection(ENV_PROD_ID)
            }
        }
        viewModel.apiKeyLiveData.observe(viewLifecycleOwner) { apiKey ->
            binding.apiKeyTextField.setText(apiKey)
        }

        viewModel.selectedFlow.observe(viewLifecycleOwner) { flow ->
            binding.clientTokenTextFieldLayout.isVisible =
                flow == MainViewModel.SelectedFlow.CLIENT_TOKEN
        }
    }

    private fun configureEnvDropDown() {
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.app_environments,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.dropDownEnvironment.adapter = adapter
        }
        binding.dropDownEnvironment.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    when (position) {
                        ENV_SANDBOX_ID -> viewModel.setCurrentEnv(PrimerEnv.Sandbox)
                        ENV_DEV_ID -> viewModel.setCurrentEnv(PrimerEnv.Dev)
                        ENV_STAGING_ID -> viewModel.setCurrentEnv(PrimerEnv.Staging)
                        ENV_PROD_ID -> viewModel.setCurrentEnv(PrimerEnv.Production)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }
    }

    private fun configureEnvSetup() {
        binding.apiKeyTextField.addTextChangedListener {
            viewModel.setApiKeyForSelectedEnv(it?.toString())
        }
    }

    private fun configureFlowToggleViews() {
        binding.flowToggleGroup.apply {
            addOnButtonCheckedListener { _, checkedId, _ ->
                viewModel.setSelectedFlow(
                    when (checkedId) {
                        binding.clientSession.id -> MainViewModel.SelectedFlow.CREATE_SESSION
                        binding.clientToken.id -> MainViewModel.SelectedFlow.CLIENT_TOKEN
                        else -> throw IllegalStateException()
                    }
                )
            }
            check(binding.clientSession.id)
        }
    }

    private fun configureClientTokenTextField() {
        binding.clientTokenTextField.apply {
            setText(viewModel.clientToken.value)
            addTextChangedListener { viewModel.setClientToken(it.toString()) }
            onFocusChangeListener =
                HideKeyboardFocusChangeListener(R.id.clientTokenTextField, activity)
        }
    }

    private fun configureCustomerIdTextField() {
        binding.customerIdTextField.apply {
            setText(viewModel.customerId.value)
            addTextChangedListener { viewModel.setCustomerId(it.toString()) }
            onFocusChangeListener =
                HideKeyboardFocusChangeListener(R.id.customerIdTextField, activity)
        }
    }

    private fun configureCountryTextField() {
        binding.countryItem.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_countryPickerFragment)
        }
    }

    private fun configureAmountTextField() {
        binding.amountTextField.apply {
            setText(viewModel.amountStringified)
            onFocusChangeListener = HideKeyboardFocusChangeListener(R.id.amountTextField, activity)
            addTextChangedListener(MoneyTextWatcher(binding.amountTextField))
            doAfterTextChanged {
                val cleanString = it.toString().replace("[.\\s]".toRegex(), "")
                try {
                    viewModel.setAmount(cleanString.toInt())
                } catch (e: NumberFormatException) {
                    viewModel.setAmount(0)
                    error = INVALID_AMOUNT_MESSAGE
                }
            }
        }
        binding.descriptorTextField.apply {
            setText(viewModel.descriptor.value)
            onFocusChangeListener =
                HideKeyboardFocusChangeListener(R.id.descriptorTextField, activity)
            addTextChangedListener { viewModel.setDescriptor(it.toString()) }
        }
        binding.metadataTextField.apply {
            setText(viewModel.metadata.value)
            onFocusChangeListener =
                HideKeyboardFocusChangeListener(R.id.metadataTextField, activity)
            addTextChangedListener { viewModel.setMetadata(it.toString()) }
        }
    }

    private fun configureSdkUiSettingsViews() {
        binding.disableInitScreen.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setInitScreenUiOptions(isChecked.not())
        }
    }

    private fun configurePaymentHandlingViews() {
        binding.paymentHandling.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.paymentHandlingAuto -> viewModel.setPaymentHandling(PrimerPaymentHandling.AUTO)
                R.id.paymentHandlingManual -> viewModel.setPaymentHandling(PrimerPaymentHandling.MANUAL)
            }
        }
    }

    private fun configureNextButton() {
        binding.vaultManagerButton.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_VaultManagerFragment)
        }
        binding.headlessCheckoutButton.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_ThirdFragment)
        }
        binding.hucRawButton.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_HeadlessRawFragment)
        }
        binding.universalCheckoutButton.setOnClickListener {
            findNavController().navigate(R.id.action_MerchantSettingsFragment_to_MerchantCheckoutFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        const val INVALID_AMOUNT_MESSAGE = "Invalid amount."

        private const val ENV_SANDBOX_ID = 0
        private const val ENV_DEV_ID = 1
        private const val ENV_STAGING_ID = 2
        private const val ENV_PROD_ID = 3
    }
}