package io.github.nircek.applicationsieve

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import io.github.nircek.applicationsieve.databinding.FragmentPackageRaterBinding

class PackageRater : Fragment() {
    private lateinit var binding: FragmentPackageRaterBinding
    private val args: PackageRaterArgs by navArgs()

    private val packageViewModel: PackageViewModel by activityViewModels {
        val app = requireActivity().application as App
        PackageViewModelFactory(app.repository, app)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (args.packageName != null) packageViewModel.loadApp(args.packageName!!)
        FragmentPackageRaterBinding.inflate(inflater, container, false).let {
            binding = it
            it.viewmodel = packageViewModel
            it.lifecycleOwner = viewLifecycleOwner
            return it.root
        }
    }
}
