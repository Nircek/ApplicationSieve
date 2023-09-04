package io.github.nircek.applicationsieve.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import io.github.nircek.applicationsieve.App
import io.github.nircek.applicationsieve.BuildConfig
import io.github.nircek.applicationsieve.R
import io.github.nircek.applicationsieve.databinding.FragmentExporterBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


class Exporter : Fragment() {
    private lateinit var binding: FragmentExporterBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        FragmentExporterBinding.inflate(inflater, container, false).let {
            binding = it
            return it.root
        }
    }

    private var currentDatabasePath: String? = null

    private fun timestamp(): String {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(Date())
    }

    private fun saveDatabaseToFile() {
        val app = requireActivity().application as App
        currentDatabasePath = app.database.openHelper.readableDatabase.path
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/x-sqlite3"
            putExtra(Intent.EXTRA_TITLE, "${timestamp()}-${BuildConfig.APPLICATION_ID}.db")
        }
        resultLauncher.launch(intent)
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { saveFile(it) }
            }
        }

    private fun saveFile(uri: Uri) {
        try {
            val src = File(currentDatabasePath!!).inputStream()
            val dst = requireContext().contentResolver.openOutputStream(uri)!!
            src.copyTo(dst)
            src.close()
            dst.close()
        } catch (e: IOException) {
            throw e // TODO
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.export_button)?.setOnClickListener {
            saveDatabaseToFile()
        }
    }

}
