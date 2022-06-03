package io.primer.android.ui.fragments.country

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.primer.android.R
import io.primer.android.data.configuration.models.CountryCode
import io.primer.android.databinding.FragmentSelectCountryBinding
import io.primer.android.ui.FieldFocuser
import io.primer.android.ui.extensions.autoCleaned
import io.primer.android.ui.fragments.base.BaseFragment
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
internal class SelectCountryFragment : BaseFragment() {

    private var binding: FragmentSelectCountryBinding by autoCleaned()

    private val viewModel by viewModel<SelectCountryViewModel>()

    private var adapter: CountriesSelectionAdapter by autoCleaned()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSelectCountryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTheme()
        setupAdapter()
        setupListeners()
        setupObservers()

        adjustBottomSheetState(BottomSheetBehavior.STATE_COLLAPSED)

        viewModel.fetchCountries()

        view.post {
            FieldFocuser.focus(binding.searchCountry)
            adjustBottomSheetState(BottomSheetBehavior.STATE_EXPANDED)
        }
    }

    private fun setupTheme() {
        val imageColorStates = ColorStateList.valueOf(
            theme.titleText.defaultColor.getColor(
                requireContext(),
                theme.isDarkMode
            )
        )
        binding.ivBack.imageTintList = imageColorStates
        binding.searchCountry.compoundDrawableTintList = imageColorStates
    }

    private fun setupAdapter() {
        binding.rvCountries.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            ).apply {
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.divider_country_selection
                )?.let(::setDrawable)
            }
        )

        adapter = CountriesSelectionAdapter(::onSelectCountryCode, theme)
        binding.rvCountries.adapter = adapter
    }

    private fun onSelectCountryCode(code: CountryCode) {
        viewModel.getCountryByCode(code) { country ->
            primerViewModel.setSelectedCountry(country)
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener { parentFragmentManager.popBackStack() }
        binding.searchCountry.setTextColor(
            theme.input.text.defaultColor.getColor(
                requireContext(),
                theme.isDarkMode
            )
        )

        binding.searchCountry.doAfterTextChanged { newText ->
            viewModel.onFilterChanged(
                newText.toString()
            )
            binding.chooseCountryDivider.visibility = when (newText.isNullOrBlank()) {
                false -> View.INVISIBLE
                true -> View.VISIBLE
            }
        }
    }

    private fun setupObservers() {
        viewModel.countriesData.observe(viewLifecycleOwner) { countries ->
            adapter.items = countries
        }
        primerViewModel.keyboardVisible.observe(viewLifecycleOwner, ::onKeyboardVisibilityChanged)
    }

    private fun onKeyboardVisibilityChanged(visible: Boolean) {
        if (visible) {
            adjustBottomSheetState(BottomSheetBehavior.STATE_EXPANDED)
        }
    }

    companion object {

        @JvmStatic
        fun newInstance(): SelectCountryFragment {
            return SelectCountryFragment()
        }
    }
}
