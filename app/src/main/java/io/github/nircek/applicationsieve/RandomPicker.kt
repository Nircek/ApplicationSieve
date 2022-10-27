package io.github.nircek.applicationsieve

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.github.nircek.applicationsieve.databinding.FragmentRandomPickerBinding


class RandomPicker : Fragment() {

    private var _binding: FragmentRandomPickerBinding? = null
    private val binding get() = _binding!!
    private var selectedApp: String? = null


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
                .filter{ it.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
            val packageInfo = packages.random()
            binding.text.text = "${packages.size} packages. Random: ${packageInfo.packageName} ${packageInfo.sourceDir} ${activity?.packageManager?.getLaunchIntentForPackage(packageInfo.packageName)}"
            binding.image.setImageDrawable(packageInfo.loadIcon(requireActivity().packageManager))
            selectedApp = packageInfo.packageName
        }
        binding.image.setOnClickListener {
            val launchIntent =
                if (selectedApp != null) requireActivity().packageManager.getLaunchIntentForPackage(
                    selectedApp!!
                ) else null
            if (launchIntent != null) startActivity(launchIntent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    }
