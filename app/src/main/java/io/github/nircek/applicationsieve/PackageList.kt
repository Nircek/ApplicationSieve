package io.github.nircek.applicationsieve

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.nircek.applicationsieve.databinding.FragmentPackageListBinding


class PackageList : Fragment() {

    private lateinit var binding: FragmentPackageListBinding

    private val packageViewModel: PackageViewModel by activityViewModels {
        val app = requireActivity().application as App
        PackageViewModelFactory(app.repository, app)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        FragmentPackageListBinding.inflate(inflater, container, false).let {
            binding = it
            it.viewmodel = packageViewModel
            it.lifecycleOwner = viewLifecycleOwner
            return it.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = PackageListAdapter()
        binding.recyclerView.let {
            it.adapter = adapter
            it.layoutManager = LinearLayoutManager(this.context)
        }

        packageViewModel.allPkgs.observe(viewLifecycleOwner) { pks ->
            // Update the cached copy of the pks in the adapter.
            pks?.let { adapter.submitList(it) }
        }

    }
}
