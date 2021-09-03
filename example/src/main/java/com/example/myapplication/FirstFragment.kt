package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Filter
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.myapplication.databinding.FragmentFirstBinding
import com.example.myapplication.models.CountryCode
import com.example.myapplication.models.PrimerEnv
import com.example.myapplication.utils.HideKeyboardFocusChangeListener
import com.example.myapplication.utils.MoneyTextWatcher
import com.example.myapplication.utils.UnfilteredArrayAdapter


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AppMainViewModel by activityViewModels()

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
        configureEnvironmentTextField()
        configureCountryTextField()
        configureCurrencyTextField()
        configureAmountTextField()
        configureNextButton()
        configureCheckBoxes()

        viewModel.canLaunchPrimer.observe(viewLifecycleOwner) { canLaunch ->
            binding.nextButton.isEnabled = canLaunch
        }

        viewModel.countryCode.observe(viewLifecycleOwner) { country ->
            binding.currencyTextField.setText(country.currencyCode.symbol)
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

    private fun configureEnvironmentTextField() {
        binding.environmentTextField.apply {
            setText(viewModel.environment.value.toString())
            setAdapter(UnfilteredArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                PrimerEnv.values(),
            ))
            addTextChangedListener { editable ->
                viewModel.environment.postValue(PrimerEnv.valueOf(editable.toString()))
            }
        }
    }

    private fun configureCountryTextField() {
        binding.countryTextField.apply {
            setText(viewModel.countryCode.value.toString())
            setAdapter(UnfilteredArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                CountryCode.values(),
            ))
            addTextChangedListener { editable ->
                viewModel.countryCode.postValue(CountryCode.valueOf(editable.toString()))
            }
        }
    }

    private fun configureCurrencyTextField() {
        binding.currencyTextField.isFocusable = false
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
    }

    private fun configureCheckBoxes() {
        binding.klarnaCheckBox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setUseKlarna(isChecked)
        }

        binding.paypalCheckBox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setUsePayPal(isChecked)
        }

        binding.cardCheckBox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setUseCard(isChecked)
        }

        binding.googlePayCheckBox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setUseGooglePay(isChecked)
        }
        binding.payByMobileCheckBox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setUsePayMobile(isChecked)
        }
    }

    private fun configureNextButton() {
        binding.nextButton.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}