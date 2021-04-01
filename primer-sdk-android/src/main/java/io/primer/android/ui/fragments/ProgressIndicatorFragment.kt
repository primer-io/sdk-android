package io.primer.android.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.primer.android.R

class ProgressIndicatorFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_progress, container, false)
    }

    companion object {

        fun newInstance(): ProgressIndicatorFragment = ProgressIndicatorFragment()
    }
}
