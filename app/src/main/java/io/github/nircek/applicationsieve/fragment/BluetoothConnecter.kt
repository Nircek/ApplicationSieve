package io.github.nircek.applicationsieve.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.util.Log
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
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID


private const val TAG = "BT_SERVICE"

private val SERVICE_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

const val MESSAGE_READ: Int = 0
const val MESSAGE_WRITE: Int = 1
const val MESSAGE_TOAST: Int = 2

@OptIn(ExperimentalCoroutinesApi::class)
class BluetoothConnecter : Fragment() {
    private lateinit var binding: FragmentBluetoothConnecterBinding
    private val checkPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    private val packageViewModel: PackageViewModel by activityViewModels {
        val app = requireActivity().application as App
        PackageViewModelFactory(app.dbRepository, app)
    }

    fun boundedDevices(): Result<Set<BluetoothDevice>> {
        try {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter() ?: return Result.failure(
                Exception("BLUETOOTH UNAVAILABLE")
            )
            if (!bluetoothAdapter.isEnabled) return Result.failure(Exception("BLUETOOTH OFFLINE"))
            val returned = bluetoothAdapter.bondedDevices
            return if (returned.isEmpty()) {
                Result.failure(Exception("NO DEVICES"))
            } else {
                Result.success(returned)
            }
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

            it.connectDevice.setOnClickListener {
                val paired = boundedDevices()
                if (paired.isFailure) {
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
                    .setItems(devices.map { dev ->
                        this.resources.getString(
                            R.string.bluetooth_device,
                            if (ActivityCompat.checkSelfPermission(
                                    this.requireContext(),
                                    Manifest.permission.BLUETOOTH_CONNECT
                                ) != PackageManager.PERMISSION_GRANTED
                            ) "" else dev.name,
                            dev.address
                        )
                    }.toTypedArray()) { dialog, which ->
                        val device = devices[which]
                        val handlerThread = HandlerThread("MyHandlerThread")
                        handlerThread.start()
                        val looper = handlerThread.looper
                        val handler = btHandler(looper)
                        val service = BluetoothService(handler, device)
                        service.start()
                        packageViewModel.setService(service)
                    }

                val dialog: AlertDialog = builder.create()
                dialog.show()

            }
            return it.root
        }
    }


    private inner class btHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            val activity = activity
            when (msg.what) {
                MESSAGE_TOAST -> if (null != activity) {
                    Toast.makeText(
                        activity, msg.data.getString("toast"),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                MESSAGE_READ -> if (null != activity) {
                    val str = String((msg.obj as ByteArray), 0, msg.arg1)
                    Toast.makeText(activity, str, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    inner class BluetoothService(
        private val handler: Handler,
        private val device: BluetoothDevice
    ) : Thread() {

        private val socket: BluetoothSocket by lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(SERVICE_UUID)
        }
        private val inputStream: InputStream = socket.inputStream
        private val outputStream: OutputStream = socket.outputStream
        private val buffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream

        private fun sendToast(msg: String) {
            handler.obtainMessage(MESSAGE_TOAST).apply {
                data = Bundle().apply {
                    putString("toast", msg)
                }
            }.sendToTarget()
        }

        override fun run() {
            var numBytes: Int // bytes returned from read()
            try {
                socket.connect()
                while (true) {
                    numBytes = inputStream.read(buffer)
                    handler.obtainMessage(
                        MESSAGE_READ, numBytes, -1,
                        buffer
                    ).sendToTarget()
                }
            } catch (_: InterruptedException) {
                sendToast("Interrupted.")
            } catch (e: Exception) {
                Log.e(TAG, "Error occurred when reading data", e)
                sendToast("Disconnected.")
            } finally {
                try {
                    socket.close()
                } catch (e: IOException) {
                    Log.e(TAG, "Could not close the connect socket", e)
                }
            }
        }

        fun write(bytes: ByteArray) {
            try {
                outputStream.write(bytes)
            } catch (e: IOException) {
                Log.e(TAG, "Error occurred when sending data", e)
                sendToast("Couldn't send data to the other device")
                return
            }
            handler.obtainMessage(MESSAGE_WRITE, -1, -1, bytes).sendToTarget()
        }

    }
}
