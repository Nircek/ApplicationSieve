package io.github.nircek.applicationsieve.fragment

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.github.nircek.applicationsieve.R
import io.github.nircek.applicationsieve.db.DbRepository.RatedApp
import kotlin.math.roundToInt

class PackageListAdapter :
    ListAdapter<RatedApp, PackageListAdapter.PackageViewHolder>(PackagesComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackageViewHolder {
        return PackageViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: PackageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PackageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val pkgItemView: TextView = itemView.findViewById(R.id.textView)

        fun bind(pkg: RatedApp) {
            pkgItemView.text =
                itemView.resources.getString(
                    R.string.item_string,
                    pkg.rating.roundToInt(),
                    pkg.package_name
                )
            val bitmap = BitmapFactory.decodeByteArray(pkg.icon, 0, pkg.icon.size)
            val draw = BitmapDrawable(pkgItemView.resources, bitmap)
            pkgItemView.setCompoundDrawablesWithIntrinsicBounds(draw, null, null, null)
            pkgItemView.setOnClickListener {
                val action = PackageListDirections.actionListToRater(pkg.package_name)
                pkgItemView.findNavController().navigate(action)
            }
        }

        companion object {
            fun create(parent: ViewGroup): PackageViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.package_item, parent, false)
                return PackageViewHolder(view)
            }
        }
    }

    class PackagesComparator : DiffUtil.ItemCallback<RatedApp>() {
        override fun areItemsTheSame(oldItem: RatedApp, newItem: RatedApp) = oldItem === newItem
        override fun areContentsTheSame(oldItem: RatedApp, newItem: RatedApp) =
            oldItem.package_name == newItem.package_name
    }
}

