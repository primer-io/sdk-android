package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.myapplication.databinding.FragmentFirstBinding
import com.example.myapplication.utils.HideKeyboardFocusChangeListener
import com.example.myapplication.utils.MoneyTextWatcher
import com.example.myapplication.viewmodels.MainViewModel
import com.example.myapplication.viewmodels.SettingsViewModel
import io.primer.android.model.dto.PaymentHandling

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()
    private val settingsViewModel: SettingsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configureCustomerIdTextField()
        configureCountryTextField()
        configureAmountTextField()
        configurePaymentHandlingViews()
        configureNextButton()

        viewModel.canLaunchPrimer.observe(viewLifecycleOwner) { canLaunch ->
            binding.nextButton.isEnabled = canLaunch
        }

        settingsViewModel.country.observe(viewLifecycleOwner) { country ->
            viewModel.countryCode.postValue(country)
            binding.countryItem.setText(country.flag)
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
                viewModel.setAmount(cleanString.toInt())
            }
        }
        binding.descriptorTextField.apply {
            setText(viewModel.descriptor.value)
            onFocusChangeListener =
                HideKeyboardFocusChangeListener(R.id.descriptorTextField, activity)
            addTextChangedListener { viewModel.setDescriptor(it.toString()) }
        }
    }

    private fun configurePaymentHandlingViews() {
        binding.paymentHandling.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.paymentHandlingAuto -> viewModel.setPaymentHandling(PaymentHandling.AUTO)
                R.id.paymentHandlingManual -> viewModel.setPaymentHandling(PaymentHandling.MANUAL)
            }
        }
    }

    private fun configureNextButton() {
        binding.componentsButton.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_ThirdFragment)
        }
        binding.nextButton.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}