package io.github.nircek.applicationsieve.fragment

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import io.github.nircek.applicationsieve.App
import io.github.nircek.applicationsieve.R
import io.github.nircek.applicationsieve.databinding.FragmentBluetoothConnecterBinding
import io.github.nircek.applicationsieve.ui.PackageViewModel
import io.github.nircek.applicationsieve.ui.PackageViewModelFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class BluetoothConnecter : Fragment() {
    private lateinit var binding: FragmentBluetoothConnecterBinding
    private val checkPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    private val packageViewModel: PackageViewModel by activityViewModels {
        val app = requireActivity().application as App
        PackageViewModelFactory(app.dbRepository, app)
    }

    private val SERVICE_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
    fun boundedDevices(): Result<Set<BluetoothDevice>> {
        try {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter() ?: return Result.failure(Exception("BLUETOOTH UNAVAILABLE"))
            if (!bluetoothAdapter.isEnabled) return Result.failure(Exception("BLUETOOTH OFFLINE"))
            val returned = bluetoothAdapter.bondedDevices
            return if(returned.isEmpty()) { Result.failure(Exception("NO DEVICES")) } else {Result.success(returned)}
        } catch (_: SecurityException) {
            return Result.failure(Exception("PERMISSION DENIED"))
        }

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        FragmentBluetoothConnecterBinding.inflate(inflater, container, false).let { it ->
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

            it.connectDevice.setOnClickListener{
                val paired = boundedDevices()
                if(paired.isFailure) {
                    Toast.makeText(
                        this.context,
                        paired.exceptionOrNull().toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                val devices = paired.getOrDefault(setOf()).toTypedArray()

                val builder: AlertDialog.Builder = AlertDialog.Builder(this.requireContext())
                builder
                    .setTitle("Choose a device")
                    .setItems(devices.map{ dev ->
                        this.resources.getString(
                        R.string.bluetooth_device,
                            if (ActivityCompat.checkSelfPermission(
                                    this.requireContext(),
                                    Manifest.permission.BLUETOOTH_CONNECT
                                ) != PackageManager.PERMISSION_GRANTED
                            ) "" else dev.name,
                        dev.address
                    )}.toTypedArray()) { dialog, which ->
                        val device = devices[which]
                        try {
                            device.createRfcommSocketToServiceRecord(SERVICE_UUID).apply {
                                connect()
                                if (!isConnected) {
                                    throw Exception("Device wasn't enabled")
                                }
                                outputStream.write("siema\n".encodeToByteArray())
                                Thread.sleep(2000)
                                close()
                            }
                        } catch(_: Exception) {
                            Toast.makeText(
                                this.context,
                                "Device doesn't support this protocol",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                val dialog: AlertDialog = builder.create()
                dialog.show()

            }
            return it.root
        }
    }
}
