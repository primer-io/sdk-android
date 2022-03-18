package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentCountryPickerBinding
import com.example.myapplication.viewmodels.SettingsViewModel

class CountryPickerFragment : Fragment() {

    private var _binding: FragmentCountryPickerBinding? = null
    private val binding get() = _binding!!

    private val adapter by lazy { CountriesAdapter() }

    private val settingsViewModel: SettingsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCountryPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvCountries.adapter = adapter

        adapter.onItemClick = { countryCode ->
            settingsViewModel.setCountry(countryCode)
            findNavController().popBackStack()
        }

        // observe country
        settingsViewModel.country.observe(viewLifecycleOwner) { country ->
            adapter.setSelected(country)
        }
        settingsViewModel.countries.observe(viewLifecycleOwner) { countries ->
            adapter.setItems(countries)
        }
        binding.polandItem.setOnClickListener {
            settingsViewModel.setCountry("PL")
            findNavController().navigate(R.id.action_CountryPickerFragment_to_firstFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}