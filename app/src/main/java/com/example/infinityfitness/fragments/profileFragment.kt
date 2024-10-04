package com.example.infinityfitness.fragments

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.infinityfitness.DueCust
import com.example.infinityfitness.MainActivity
import com.example.infinityfitness.Manifest
import com.example.infinityfitness.R
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class profileFragment:Fragment(R.layout.profile) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val logoutBtn: ImageButton = view.findViewById(R.id.lgout)
        val dueBtn   : ImageButton = view.findViewById(R.id.due)
        val exprt : ImageButton = view.findViewById(R.id.xprt)

        logoutBtn.setOnClickListener {
            val intent = Intent(activity, MainActivity::class.java)

            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)

            activity?.finish()
        }
        dueBtn.setOnClickListener {
            val intent = Intent(activity, DueCust::class.java)

            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)

            activity?.finish()
        }
    }

    private fun getDatabasePath(): File {
        return requireContext().getDatabasePath("gym_database")
    }


    private fun exportRoomDatabaseToExternalStorage(context: Context) {
        // Get your database file path
        val dbFile = getDatabasePath()

        // Copy the database file to external storage (Scoped Storage)
        val externalDir = context.getExternalFilesDir(null) // Scoped storage for app-specific data
        val destFile = File(externalDir, "gym_database_backup")

        try {
            // Copy the database file to the destination
            dbFile.inputStream().use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            Toast.makeText(context, "Database exported successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to export database.", Toast.LENGTH_SHORT).show()
        }
    }




}