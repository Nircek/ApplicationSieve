package io.github.nircek.applicationsieve.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.github.nircek.applicationsieve.R
import io.github.nircek.applicationsieve.db.Category

class CategoryListAdapter :
    ListAdapter<Category, CategoryListAdapter.CategoryViewHolder>(CategoriesComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryTextView: TextView = itemView.findViewById(R.id.textView)

        fun bind(category: Category) {
            categoryTextView.text = category.name
            itemView.setOnClickListener {
                // FIXME: make it change VM directly
                val action =
                    CategoryListDirections.actionCategoryListToRater(null, category.category_id)
                itemView.findNavController().navigate(action)
            }
        }

        companion object {
            fun create(parent: ViewGroup): CategoryViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.category_item, parent, false)
                return CategoryViewHolder(view)
            }
        }
    }

    class CategoriesComparator : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category) = oldItem === newItem
        override fun areContentsTheSame(oldItem: Category, newItem: Category) =
            oldItem.category_id == newItem.category_id
    }
}

