package com.example.infinityfitness.fragments

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.infinityfitness.DueCust
import com.example.infinityfitness.MainActivity
import com.example.infinityfitness.R
import com.example.infinityfitness.database.GymDatabase
import com.google.firebase.iid.Metadata
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class profileFragment:Fragment(R.layout.profile) {

    private lateinit var progressLayout: View
    private lateinit var progressBar: ProgressBar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val logoutBtn: ImageButton = view.findViewById(R.id.lgout)
        val dueBtn   : ImageButton = view.findViewById(R.id.due)
        val exprt : ImageButton = view.findViewById(R.id.xprt)
        val imprt : ImageButton = view.findViewById(R.id.imprt)
        progressLayout = view.findViewById(R.id.progress_layout)
        progressBar = view.findViewById(R.id.progress_bar)

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

        exprt.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val inflater: LayoutInflater = layoutInflater
                val popupView: View = inflater.inflate(R.layout.popup, null)

                // Create the PopupWindow
                val popupWindow = PopupWindow(
                    popupView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    true
                )
                popupWindow.setBackgroundDrawable(
                    ContextCompat.getDrawable(requireContext(), android.R.color.transparent)
                )
                val ed: TextView = popupView.findViewById<TextView>(R.id.Text)
                ed.setText("do you want to export the data?")

                // Show the popup window
                withContext(Dispatchers.Main) {
                    // Show the popup window
                    popupWindow.showAtLocation(view, android.view.Gravity.CENTER, 0, 0)
                }

                val yesb: Button = popupView.findViewById<Button>(R.id.yesbtn)
                val nob: Button = popupView.findViewById<Button>(R.id.nobtn)
                yesb.setOnClickListener {
                    lifecycleScope.launch(Dispatchers.IO) {
                        uploadDatabaseWithBackup()
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                requireContext(),
                                "upload is in progress",
                                Toast.LENGTH_SHORT
                            )
                                .show()

                            popupWindow.dismiss()
                        }
                    }
                }
                nob.setOnClickListener{
                    popupWindow.dismiss()
                }
            }
        }

        imprt.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val inflater: LayoutInflater = layoutInflater
                val popupView: View = inflater.inflate(R.layout.popup, null)

                // Create the PopupWindow
                val popupWindow = PopupWindow(
                    popupView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    true
                )
                popupWindow.setBackgroundDrawable(
                    ContextCompat.getDrawable(requireContext(), android.R.color.transparent)
                )
                val ed: TextView = popupView.findViewById<TextView>(R.id.Text)
                ed.text = "Do you want to import the data?"

                // Switch to the main thread to show the popup
                withContext(Dispatchers.Main) {
                    // Show the popup window
                    popupWindow.showAtLocation(view, android.view.Gravity.CENTER, 0, 0)
                }

                val yesb: Button = popupView.findViewById<Button>(R.id.yesbtn)
                val nob: Button = popupView.findViewById<Button>(R.id.nobtn)
                yesb.setOnClickListener {
                    lifecycleScope.launch(Dispatchers.IO) {
                        importDatabaseWithBackup()
                        // Switch back to the main thread to show the toast
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Download in progress", Toast.LENGTH_SHORT)
                                .show()
                            popupWindow.dismiss()
                        }
                    }
                }
                nob.setOnClickListener {
                    popupWindow.dismiss()
                }
            }
        }



    }

//    private fun getDatabasePath(): File {
//        return requireContext().getDatabasePath("gym_database")
//    }


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

    private val storageRef: StorageReference = FirebaseStorage.getInstance().reference
    private val databaseName = "gym_database"
    private val tempDatabaseName = "temp_gym_database"

    private fun getDatabasePath(): File {
        val db = GymDatabase.getDatabase(requireContext())
        db.destroyInstance()
        val dbPath = requireContext().getDatabasePath(databaseName)
        Log.d("Database", "Database path: ${dbPath.absolutePath}")
        return dbPath.absoluteFile
    }

    fun uploadDatabaseWithBackup() {
        val dbRef = storageRef.child("databases/$databaseName")

        // Check if the database already exists in Firebase
        dbRef.metadata.addOnSuccessListener {
            // If exists, rename it to temp_ prefix
            renameFileInCloud(dbRef)
        }.addOnFailureListener {
            // No existing file, upload new database directly
            uploadNewDatabase()
        }
    }

    private fun renameFileInCloud(dbRef: StorageReference) {
        val tempDbRef = storageRef.child("databases/$tempDatabaseName")

        val metadata = StorageMetadata.Builder().setCustomMetadata("name", tempDatabaseName).build()
        // Rename the original database in Firebase by updating its name
        dbRef.updateMetadata(metadata)
            .addOnSuccessListener {
                // Renaming successful, proceed to upload new database
                uploadNewDatabase()
            }.addOnFailureListener { e ->
                Log.e("Firebase", "Failed to rename file in cloud: ${e.message}")
            }
    }


    private fun updateProgressBar(progress: Int) {
        lifecycleScope.launch(Dispatchers.Main) {
            progressBar.progress = progress
        }
    }


    private fun showProgressBar() {
        lifecycleScope.launch(Dispatchers.Main) {
            progressLayout.visibility = View.VISIBLE
        }
    }

    private fun hideProgressBar() {
        lifecycleScope.launch(Dispatchers.Main) {
            progressLayout.visibility = View.GONE
        }
    }


    private fun uploadNewDatabase() {
        val databaseFile = getDatabasePath()
        val fileUri = Uri.fromFile(databaseFile)
        val dbRef = storageRef.child("databases/$databaseName")

        val uploadTask = dbRef.putFile(fileUri)

        showProgressBar()


        uploadTask.addOnProgressListener {
            val progress = (100.0 * it.bytesTransferred) / it.totalByteCount
            updateProgressBar(progress.toInt())

            Log.d("Firebase", "Upload progress: $progress%")
        }

        uploadTask.addOnSuccessListener {
            Log.d("Firebase", "Database uploaded successfully!")
            hideProgressBar()
            Toast.makeText(requireContext(), "The database is uploaded successfully", Toast.LENGTH_SHORT).show()
            //deleteTempFileInCloud() // Optional: Delete the temp file after success
        }.addOnFailureListener { e ->
            Log.e("Firebase", "Failed to upload database: ${e.message}")
            hideProgressBar()
            Toast.makeText(requireContext(), "Failed to upload", Toast.LENGTH_SHORT).show()
            // Restore the temp file if the upload fails
            //restoreTempFileInCloud()
        }
    }

//    private fun restoreTempFileInCloud() {
//        val tempDbRef = storageRef.child("databases/$tempDatabaseName")
//        val originalDbRef = storageRef.child("databases/$databaseName")
//
//        val metadata = StorageMetadata.Builder().setCustomMetadata("name", databaseName).build()
//
//        // Delete the failed upload first
//        originalDbRef.delete().addOnSuccessListener {
//            // Rename temp file back to original
//            tempDbRef.updateMetadata(metadata)
//                .addOnSuccessListener {
//                    Log.d("Firebase", "Restored original database from temp copy.")
//                }.addOnFailureListener { e ->
//                    Log.e("Firebase", "Failed to rename temp file back: ${e.message}")
//                }
//        }.addOnFailureListener { e ->
//            Log.e("Firebase", "Failed to delete corrupted file: ${e.message}")
//        }
//    }

//    private fun deleteTempFileInCloud() {
//        val tempDbRef = storageRef.child("databases/$tempDatabaseName")
//
//        // Delete the temp file after successful upload
//        tempDbRef.delete().addOnSuccessListener {
//            Log.d("Firebase", "Temporary file deleted successfully.")
//        }.addOnFailureListener { e ->
//            Log.e("Firebase", "Failed to delete temp file: ${e.message}")
//        }
//    }

    private fun importDatabaseWithBackup() {
        exportLocalDatabase()

        val dbRef = storageRef.child("databases/$databaseName")
        val localDbFile = getDatabasePath()

        val tempLocalBackup = File(requireContext().getExternalFilesDir(null), "local_gym_database_backup")

        val mon = dbRef.getFile(localDbFile)

        showProgressBar()

        mon.addOnSuccessListener {
            Log.d("Local" , "Saved at ${localDbFile.absolutePath}")
            Log.d("Firebase", "Database downloaded and replaced successfully!")
            Toast.makeText(requireContext(), "Database is successfully downloaded", Toast.LENGTH_SHORT).show()
            tempLocalBackup.delete() // Optionally delete local backup after success
            hideProgressBar()

        }

        mon.addOnFailureListener { e ->
            Log.e("Firebase", "Failed to download database: ${e.message}")
            // Restore the local backup if downloading fails
            restoreLocalBackup(tempLocalBackup)
            hideProgressBar()
            Toast.makeText(requireContext(), "Failed to download Database", Toast.LENGTH_SHORT).show()
        }
        mon.addOnProgressListener {
            val progress = (100.0 * it.bytesTransferred) / it.totalByteCount
            updateProgressBar(progress.toInt())

            Log.d("Firebase", "Download progress: $progress%")
        }
    }

    private fun exportLocalDatabase() {
        val localDbFile = getDatabasePath()
        val externalDir = requireContext().getExternalFilesDir(null)
        val backupFile = File(externalDir, "local_gym_database_backup")

        try {
            localDbFile.inputStream().use { input ->
                backupFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            Log.d("Database", "Local database backup created at ${backupFile.path}")
        } catch (e: IOException) {
            Log.e("Database", "Failed to create local backup: ${e.message}")
        }
    }

    private fun restoreLocalBackup(backupFile: File) {
        val localDbFile = getDatabasePath()

        try {
            backupFile.inputStream().use { input ->
                localDbFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            Log.d("Database", "Local database restored from backup.")
        } catch (e: IOException) {
            Log.e("Database", "Failed to restore local backup: ${e.message}")
        }
    }

}






