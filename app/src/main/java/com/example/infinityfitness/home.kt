package com.example.infinityfitness

import android.os.Bundle
import android.content.Intent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.infinityfitness.databinding.HomeBinding
import com.example.infinityfitness.fragments.HomeFragement
import com.example.infinityfitness.fragments.RegisterFragment
import com.example.infinityfitness.fragments.UserDataFragment
import com.example.infinityfitness.fragments.profileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView


class home : AppCompatActivity() {
    private lateinit var binding: HomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.home)
        binding = HomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setting edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize fragments
        val firstFragment = HomeFragement()
        val secondFragment = profileFragment()
        val thirdFragment = RegisterFragment()
        val fourthFragment = UserDataFragment()

        setCurrentFragement(firstFragment)

        binding.btm?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navHome -> {
                    setCurrentFragement(firstFragment)
                    true
                }

                R.id.navProfile -> {
                    setCurrentFragement(secondFragment)
                    true
                }

                R.id.navRegister -> {
                    setCurrentFragement(thirdFragment)
                    true
                }

                R.id.navData ->{
                    setCurrentFragement(fourthFragment)
                    true
                }

                else -> false
            }
        }


    }

        fun setCurrentFragement(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, fragment)
            commit()
        }
}
