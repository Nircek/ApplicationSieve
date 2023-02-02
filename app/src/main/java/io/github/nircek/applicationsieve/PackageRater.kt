package io.github.nircek.applicationsieve

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import io.github.nircek.applicationsieve.databinding.FragmentPackageRaterBinding

class PackageRater : Fragment() {
    private lateinit var binding: FragmentPackageRaterBinding

    private val packageViewModel: PackageViewModel by activityViewModels {
        val app = requireActivity().application as App
        PackageViewModelFactory(app.repository, app)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        FragmentPackageRaterBinding.inflate(inflater, container, false).let {
            binding = it
            it.viewmodel = packageViewModel
            it.lifecycleOwner = this
            return it.root
        }
    }
}
