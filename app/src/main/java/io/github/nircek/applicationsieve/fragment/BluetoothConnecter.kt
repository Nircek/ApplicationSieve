package io.github.nircek.applicationsieve.fragment

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import io.github.nircek.applicationsieve.App
import io.github.nircek.applicationsieve.R
import io.github.nircek.applicationsieve.databinding.FragmentBluetoothConnecterBinding
import io.github.nircek.applicationsieve.ui.PackageViewModel
import io.github.nircek.applicationsieve.ui.PackageViewModelFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class BluetoothConnecter : Fragment() {
    private lateinit var binding: FragmentBluetoothConnecterBinding
    private val checkPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    private val packageViewModel: PackageViewModel by activityViewModels {
        val app = requireActivity().application as App
        PackageViewModelFactory(app.dbRepository, app)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        FragmentBluetoothConnecterBinding.inflate(inflater, container, false).let {
            binding = it
            it.vm = packageViewModel
            it.lifecycleOwner = viewLifecycleOwner
            it.enableBluetooth.setOnClickListener {
                try {
                    val REQUEST_ENABLE_BT = 66
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                } catch (_: SecurityException) {
                    Toast.makeText(
                        this.context,
                        this.resources.getString(R.string.permission_needed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            it.grantBluetooth.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    checkPermission.launch(Manifest.permission.BLUETOOTH_CONNECT)
                }
            }
            return it.root
        }
    }
}
