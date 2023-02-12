package io.github.nircek.applicationsieve.ui

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import androidx.lifecycle.findViewTreeLifecycleOwner
import io.github.nircek.applicationsieve.R
import io.github.nircek.applicationsieve.db.Category
import kotlinx.coroutines.ExperimentalCoroutinesApi

private fun SpinnerAdapter.asCategoryAdapter(): ArrayAdapter<Category> {
    if (this !is ArrayAdapter<*> || !isEmpty && getItem(0) !is Category)
        throw IllegalArgumentException("Adapter must be ArrayAdapter<Category> in CategorySpinner")
    @Suppress("UNCHECKED_CAST")
    return this as ArrayAdapter<Category>
}

@OptIn(ExperimentalCoroutinesApi::class)
class CategorySpinner(ctx: Context, attrs: AttributeSet?) :
    androidx.appcompat.widget.AppCompatSpinner(ctx, attrs),
    AdapterView.OnItemSelectedListener {
    lateinit var vm: PackageViewModel
    private val supplyNew: Boolean
    private val list = ArrayList<Category>()
    private val _adapter get() = adapter.asCategoryAdapter()
    override fun setAdapter(adapter: SpinnerAdapter) = super.setAdapter(adapter.asCategoryAdapter())


    init {
        adapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_dropdown_item, list)
        onItemSelectedListener = this
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.CategorySpinner, 0, 0)
        try {
            supplyNew = a.getBoolean(R.styleable.CategorySpinner_supply_new, false)
        } finally {
            a.recycle()
        }
    }

    override fun onItemSelected(a: AdapterView<*>?, v: View?, pos: Int, id: Long) {
        val chosen = _adapter.getItem(pos)!!.category_id
        if (chosen >= 0) {
            vm.selectedCategory.value = chosen
            return
        }
        Category.dialogNew(context, vm)
    }

    override fun onNothingSelected(a: AdapterView<*>?) {
        a?.setSelection(0)
    }

    fun setViewModel(obj: PackageViewModel?) {
        if (obj == null) return
        vm = obj
        findViewTreeLifecycleOwner()?.let {
            vm.selectedCategory.observe(it) { category ->
                Handler(Looper.getMainLooper()).postDelayed({
                    // delay to make sure that listCategories got updated after Category.dialogNew
                    val index = vm.listCategories.value
                        ?.withIndex()
                        ?.filter { e -> e.value.category_id == category }
                        ?.getOrNull(0)
                        ?.index ?: -1
                    setSelection(index + 1)
                }, 0)
            }
            vm.listCategories.observe(it) { categories ->
                list.clear()
                list.add(Category.all(resources))
                list.addAll(categories)
                if (supplyNew) list.add(Category.new(resources))
                _adapter.notifyDataSetChanged()
            }
        }
    }
}


