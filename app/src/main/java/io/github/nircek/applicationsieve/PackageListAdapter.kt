package io.github.nircek.applicationsieve

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

class PackageListAdapter :
    ListAdapter<Package, PackageListAdapter.PackageViewHolder>(PackagesComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackageViewHolder {
        return PackageViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: PackageViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current.id, current.rating)
    }

    class PackageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val wordItemView: TextView = itemView.findViewById(R.id.textView)

        fun bind(text: String, rating: Float) {
            wordItemView.text =
                itemView.resources.getString(R.string.item_string, rating.roundToInt(), text)
        }

        companion object {
            fun create(parent: ViewGroup): PackageViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.package_item, parent, false)
                return PackageViewHolder(view)
            }
        }
    }

    class PackagesComparator : DiffUtil.ItemCallback<Package>() {
        override fun areItemsTheSame(oldItem: Package, newItem: Package): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Package, newItem: Package): Boolean {
            return oldItem.id == newItem.id
        }
    }
}

