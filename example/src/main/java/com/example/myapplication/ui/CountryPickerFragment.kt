package com.example.myapplication.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentCountryPickerBinding
import com.example.myapplication.viewmodels.SettingsViewModel

class CountryPickerFragment : Fragment() {

    private var _binding: FragmentCountryPickerBinding? = null
    private val binding get() = _binding!!

    private val settingsViewModel: SettingsViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCountryPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.germanyItem.setText("🇩🇪")
        binding.swedenItem.setText("🇸🇪")
        binding.singaporeItem.setText("🇸🇬")
        binding.norwayItem.setText("🇳🇴")

        // observe country
        settingsViewModel.country.observe(viewLifecycleOwner) { country ->
            binding.germanyItem.isSelected = country == "DE"
            binding.swedenItem.isSelected = country == "SE"
            binding.singaporeItem.isSelected = country == "SG"
            binding.norwayItem.isSelected = country == "NO"
        }

        // set on click listeners
        binding.germanyItem.setOnClickListener {
            settingsViewModel.setCountry("DE")
            findNavController().navigate(R.id.action_CountryPickerFragment_to_firstFragment)
        }

        binding.swedenItem.setOnClickListener {
            settingsViewModel.setCountry("SE")
            findNavController().navigate(R.id.action_CountryPickerFragment_to_firstFragment)
        }

        binding.singaporeItem.setOnClickListener {
            settingsViewModel.setCountry("SG")
            findNavController().navigate(R.id.action_CountryPickerFragment_to_firstFragment)
        }

        binding.norwayItem.setOnClickListener {
            settingsViewModel.setCountry("NO")
            findNavController().navigate(R.id.action_CountryPickerFragment_to_firstFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}