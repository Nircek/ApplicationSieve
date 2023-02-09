package io.github.nircek.applicationsieve.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.nircek.applicationsieve.App
import io.github.nircek.applicationsieve.databinding.FragmentCategoryListBinding
import io.github.nircek.applicationsieve.ui.PackageViewModel
import io.github.nircek.applicationsieve.ui.PackageViewModelFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
class CategoryList : Fragment() {

    private lateinit var binding: FragmentCategoryListBinding

    private val packageViewModel: PackageViewModel by activityViewModels {
        val app = requireActivity().application as App
        PackageViewModelFactory(app.dbRepository, app)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        FragmentCategoryListBinding.inflate(inflater, container, false).let {
            binding = it
            it.viewmodel = packageViewModel
            it.lifecycleOwner = viewLifecycleOwner
            return it.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = CategoryListAdapter()
        binding.recyclerView.let {
            it.adapter = adapter
            it.layoutManager = LinearLayoutManager(this.context)
        }

        packageViewModel.listCategories.observe(viewLifecycleOwner) { list ->
            // Update the cached copy of the list in the adapter.
            list?.let { adapter.submitList(it) }
        }

    }
}
