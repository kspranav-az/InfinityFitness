package com.example.infinityfitness.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.example.infinityfitness.MainActivity
import com.example.infinityfitness.R

class profileFragment:Fragment(R.layout.profile) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find the logout button using the provided view
        val logoutBtn: ImageButton = view.findViewById(R.id.lgout)

        // Set an OnClickListener for the logout button
        logoutBtn.setOnClickListener {
            // Create an Intent to navigate to MainActivity
            val intent = Intent(activity, MainActivity::class.java)

            // Set flags to clear the activity stack
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            // Start MainActivity
            startActivity(intent)

            // Optionally, finish the current activity
            activity?.finish()
        }
    }

}