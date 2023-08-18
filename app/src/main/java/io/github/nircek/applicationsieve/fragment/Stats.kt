package io.github.nircek.applicationsieve.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import io.github.nircek.applicationsieve.App
import io.github.nircek.applicationsieve.databinding.FragmentStatsBinding
import io.github.nircek.applicationsieve.ui.PackageViewModel
import io.github.nircek.applicationsieve.ui.PackageViewModelFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class Stats : Fragment() {
    private lateinit var binding: FragmentStatsBinding

    private val packageViewModel: PackageViewModel by activityViewModels {
        val app = requireActivity().application as App
        PackageViewModelFactory(app.dbRepository, app)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        FragmentStatsBinding.inflate(inflater, container, false).let {
            binding = it
            it.vm = packageViewModel
            it.lifecycleOwner = viewLifecycleOwner
            return it.root
        }
    }
}
