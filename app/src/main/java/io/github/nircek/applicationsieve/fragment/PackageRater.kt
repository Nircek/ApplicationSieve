package io.github.nircek.applicationsieve.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import io.github.nircek.applicationsieve.App
import io.github.nircek.applicationsieve.databinding.FragmentPackageRaterBinding
import io.github.nircek.applicationsieve.ui.PackageViewModel
import io.github.nircek.applicationsieve.ui.PackageViewModelFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class PackageRater : Fragment() {
    private lateinit var binding: FragmentPackageRaterBinding
    private val args: PackageRaterArgs by navArgs()

    private val packageViewModel: PackageViewModel by activityViewModels {
        val app = requireActivity().application as App
        PackageViewModelFactory(app.dbRepository, app)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (args.packageName != null) packageViewModel.loadApp(args.packageName!!)
        if (args.categoryId != -1) packageViewModel.selCategory.value = args.categoryId
        FragmentPackageRaterBinding.inflate(inflater, container, false).let {
            binding = it
            it.viewmodel = packageViewModel
            it.lifecycleOwner = viewLifecycleOwner
            return it.root
        }
    }
}
