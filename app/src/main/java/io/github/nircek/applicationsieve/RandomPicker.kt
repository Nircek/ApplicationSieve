package io.github.nircek.applicationsieve

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.nircek.applicationsieve.databinding.FragmentRandomPickerBinding


class RandomPicker : Fragment() {

    private var _binding: FragmentRandomPickerBinding? = null
    private val binding get() = _binding!!
    private var selectedApp: String? = null
    private var selectedAppIcon: Drawable? = null
    private var selectedAppIconStream: ByteArray? = null

    private val packageViewModel: PackageViewModel by activityViewModels {
        PackageViewModelFactory((requireActivity().application as App).repository)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRandomPickerBinding.inflate(inflater, container, false)

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.randomizeButton.setOnClickListener {
            val packages = requireActivity().packageManager
                .getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
            val packageInfo = packages.random()
            binding.text.text =
                "${packages.size} packages. Random: ${packageInfo.packageName} ${packageInfo.sourceDir} ${
                    activity?.packageManager?.getLaunchIntentForPackage(packageInfo.packageName)
                }"
            selectedApp = packageInfo.packageName
            selectedAppIcon = packageInfo.loadIcon(requireActivity().packageManager)
            selectedAppIconStream = PackageRepository.drawableToStream(selectedAppIcon!!)
            binding.image.setImageDrawable(selectedAppIcon)
        }
        binding.image.setOnClickListener {
            val launchIntent =
                if (selectedApp != null) requireActivity().packageManager.getLaunchIntentForPackage(
                    selectedApp!!
                ) else null
            if (launchIntent != null) startActivity(launchIntent)
        }
        binding.add.setOnClickListener {
            if (selectedApp == null) return@setOnClickListener
            Toast.makeText(context, "${binding.rating.rating}/7", Toast.LENGTH_SHORT).show()
            packageViewModel.insert(
                Package(
                    selectedApp!!,
                    selectedAppIconStream!!,
                    binding.rating.rating
                )
            )

        }

        val adapter = PackageListAdapter()
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this.context)

        packageViewModel.allPkgs.observe(viewLifecycleOwner) { pks ->
            // Update the cached copy of the pks in the adapter.
            pks?.let { adapter.submitList(it) }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
