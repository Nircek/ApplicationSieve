package io.github.nircek.applicationsieve.fragment

import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.nircek.applicationsieve.App
import io.github.nircek.applicationsieve.R
import io.github.nircek.applicationsieve.databinding.FragmentPackageListBinding
import io.github.nircek.applicationsieve.db.Category
import io.github.nircek.applicationsieve.ui.PackageViewModel
import io.github.nircek.applicationsieve.ui.PackageViewModelFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
class PackageList : Fragment(), MenuProvider, AdapterView.OnItemSelectedListener {

    private lateinit var binding: FragmentPackageListBinding

    private val packageViewModel: PackageViewModel by activityViewModels {
        val app = requireActivity().application as App
        PackageViewModelFactory(app.dbRepository, app)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.package_list_options, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.drop_db -> AlertDialog.Builder(requireActivity()).apply {
                setTitle(R.string.drop_apps_confirm)
                setPositiveButton(R.string.drop_apps_btn) { _, _ -> packageViewModel.dropApps() }
            }.show()
            else -> return false
        }
        return true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        (requireActivity() as MenuHost).addMenuProvider(
            this,
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )
        FragmentPackageListBinding.inflate(inflater, container, false).let {
            binding = it
            it.viewmodel = packageViewModel
            it.lifecycleOwner = viewLifecycleOwner
            return it.root
        }
    }

    lateinit var categoriesAdapter: ArrayAdapter<Category>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoriesAdapter =
            ArrayAdapter<Category>(requireContext(), android.R.layout.simple_spinner_dropdown_item)
        categoriesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinner.let {
            it.adapter = categoriesAdapter
            it.onItemSelectedListener = this
        }

        val pkgAdapter = PackageListAdapter()
        binding.recyclerView.let {
            it.adapter = pkgAdapter
            it.layoutManager = LinearLayoutManager(this.context)
        }

        packageViewModel.listCategories.observe(viewLifecycleOwner) { categories ->
            categoriesAdapter.add(Category.all(resources))
            categoriesAdapter.addAll(categories)
        }
        packageViewModel.listInSelectedCategory.observe(viewLifecycleOwner) { pks ->
            pks?.let { pkgAdapter.submitList(it) }
        }

    }

    override fun onItemSelected(a: AdapterView<*>?, v: View?, pos: Int, id: Long) {
        packageViewModel.selectedCategory.value = categoriesAdapter.getItem(pos)!!.category_id
    }

    override fun onNothingSelected(a: AdapterView<*>?) {
        a?.setSelection(0)
    }
}
