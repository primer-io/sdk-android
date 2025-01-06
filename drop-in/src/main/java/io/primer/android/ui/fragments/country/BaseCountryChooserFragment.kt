package io.primer.android.ui.fragments.country

import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.primer.android.R
import io.primer.android.configuration.data.model.CountryCode
import io.primer.android.core.di.extensions.viewModel
import io.primer.android.databinding.FragmentSelectCountryBinding
import io.primer.android.ui.FieldFocuser
import io.primer.android.ui.extensions.autoCleaned
import io.primer.android.ui.fragments.base.BaseFragment

internal abstract class BaseCountryChooserFragment : BaseFragment() {
    protected var binding: FragmentSelectCountryBinding by autoCleaned()

    protected val viewModel: SelectCountryViewModel
        by viewModel<SelectCountryViewModel, SelectCountryViewModelFactory>()

    protected var adapter: CountriesSelectionAdapter by autoCleaned()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSelectCountryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupTheme()
        setupAdapter()
        setupListeners()
        setupObservers()

        adjustBottomSheetState(BottomSheetBehavior.STATE_COLLAPSED)

        viewModel.fetchCountriesData(dataSourceType)

        view.post {
            FieldFocuser.focus(binding.searchCountry)
            adjustBottomSheetState(BottomSheetBehavior.STATE_EXPANDED)
        }
    }

    private fun setupTheme() {
        val imageColorStates =
            ColorStateList.valueOf(
                theme.titleText.defaultColor.getColor(
                    requireContext(),
                    theme.isDarkMode,
                ),
            )
        binding.ivBack.imageTintList = imageColorStates
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            TextViewCompat.setCompoundDrawableTintList(
                binding.searchCountry,
                imageColorStates,
            )
        } else {
            binding.searchCountry.compoundDrawables.forEach { drawable ->
                drawable?.let {
                    DrawableCompat.setTintList(
                        it.mutate(),
                        imageColorStates,
                    )
                }
            }
        }
    }

    private fun setupAdapter() {
        binding.rvCountries.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL,
            ).apply {
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.divider_country_selection,
                )?.let(::setDrawable)
            },
        )

        adapter = CountriesSelectionAdapter(::onSelectCountryCode, theme)
        binding.rvCountries.adapter = adapter
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener { parentFragmentManager.popBackStack() }
        binding.searchCountry.setTextColor(
            theme.input.text.defaultColor.getColor(
                requireContext(),
                theme.isDarkMode,
            ),
        )

        binding.searchCountry.doOnTextChanged { newText, _, _, _ ->
            viewModel.onFilterChanged(
                dataSourceType,
                newText.toString(),
            )

            binding.tvEmptyResultForQuery.text =
                resources.getString(
                    R.string.no_results_for_query,
                    newText,
                )
        }
    }

    private fun setupObservers() {
        viewModel.countriesData.observe(viewLifecycleOwner) { countries ->
            adapter.items = countries
            binding.llEmptyResultContainer.isVisible = countries.isEmpty()
            binding.tvSearchResultLabel.isVisible = countries.isNotEmpty()
        }
        primerViewModel.keyboardVisible.observe(viewLifecycleOwner, ::onKeyboardVisibilityChanged)
    }

    private fun onKeyboardVisibilityChanged(visible: Boolean) {
        if (visible) {
            adjustBottomSheetState(BottomSheetBehavior.STATE_EXPANDED)
        }
        binding.tvSearchResultLabel.isVisible = visible && adapter.itemCount > 0
    }

    protected abstract fun onSelectCountryCode(code: CountryCode)

    protected abstract val searchPlaceholderId: Int

    protected abstract val dataSourceType: CountryDataType
}
