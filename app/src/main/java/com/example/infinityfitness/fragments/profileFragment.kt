package com.example.infinityfitness.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.example.infinityfitness.DueCust
import com.example.infinityfitness.MainActivity
import com.example.infinityfitness.R

class profileFragment:Fragment(R.layout.profile) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val logoutBtn: ImageButton = view.findViewById(R.id.lgout)
        val dueBtn   : ImageButton = view.findViewById(R.id.due)

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

}