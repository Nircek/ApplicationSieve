package io.github.nircek.applicationsieve.fragment

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.nircek.applicationsieve.App
import io.github.nircek.applicationsieve.R
import io.github.nircek.applicationsieve.databinding.FragmentCategoryListBinding
import io.github.nircek.applicationsieve.db.Category
import io.github.nircek.applicationsieve.ui.PackageViewModel
import io.github.nircek.applicationsieve.ui.PackageViewModelFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
class CategoryList : Fragment(), MenuProvider {

    private lateinit var binding: FragmentCategoryListBinding

    private val packageViewModel: PackageViewModel by activityViewModels {
        val app = requireActivity().application as App
        PackageViewModelFactory(app.dbRepository, app)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.category_list_options, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.add_category -> Category.dialogNew(requireContext(), packageViewModel)
            R.id.delete_category -> AlertDialog.Builder(requireActivity()).apply {
                setTitle(R.string.delete_category)
                val categories = packageViewModel.allCategories.value
                val categoriesArray = categories.map { it.name }.toTypedArray()
                setItems(categoriesArray) { d, i ->
                    packageViewModel.deleteCategory(categories[i])
                    d.dismiss()
                }
            }.show()
            R.id.drop_db -> AlertDialog.Builder(requireActivity()).apply {
                setTitle(R.string.drop_categories_confirm)
                setPositiveButton(R.string.drop_apps_btn) { _, _ -> packageViewModel.dropCategories() }
            }.show()
            else -> return false
        }
        return true
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (requireActivity() as MenuHost).addMenuProvider(
            this,
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )
        FragmentCategoryListBinding.inflate(inflater, container, false).let {
            binding = it
            it.viewmodel = packageViewModel
            it.lifecycleOwner = viewLifecycleOwner
            return it.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as MenuHost).addMenuProvider(
            this,
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )
        val adapter = CategoryListAdapter()
        binding.recyclerView.let {
            it.adapter = adapter
            it.layoutManager = LinearLayoutManager(this.context)
        }

        packageViewModel.allCategories.live.observe(viewLifecycleOwner) { list ->
            // Update the cached copy of the list in the adapter.
            list?.let { adapter.submitList(listOf(Category.all(resources)) + it) }
        }

    }
}
