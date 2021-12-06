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
import com.example.myapplication.datamodels.AppCountryCode
import com.example.myapplication.datamodels.PrimerEnv
import com.example.myapplication.utils.HideKeyboardFocusChangeListener
import com.example.myapplication.utils.MoneyTextWatcher
import com.example.myapplication.utils.UnfilteredArrayAdapter
import com.example.myapplication.viewmodels.MainViewModel
import com.example.myapplication.viewmodels.SettingsViewModel
import io.primer.android.model.dto.CountryCode

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
        configureNextButton()

        viewModel.canLaunchPrimer.observe(viewLifecycleOwner) { canLaunch ->
            binding.nextButton.isEnabled = canLaunch
        }

        settingsViewModel.country.observe(viewLifecycleOwner) { country ->
            when (country) {
                "DE" -> binding.countryItem.setText("🇩🇪")
                "SE" -> binding.countryItem.setText("🇸🇪")
                "SG" -> binding.countryItem.setText("🇸🇬")
                "NO" -> binding.countryItem.setText("🇳🇴")
            }
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