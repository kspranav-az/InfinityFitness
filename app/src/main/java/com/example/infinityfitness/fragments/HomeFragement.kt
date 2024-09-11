package com.example.infinityfitness.fragments

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import androidx.databinding.DataBindingUtil.setContentView
import androidx.fragment.app.Fragment
import com.example.infinityfitness.R
import com.example.infinityfitness.databinding.HomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeFragement() : Fragment(R.layout.home), Parcelable {

    constructor(parcel: Parcel) : this() {
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        val binding = HomeBinding.inflate(layoutInflater)

        val thirdFragment = RegisterFragment()
        val fourthFragment = UserDataFragment()

        val regbtn: View = view.findViewById(R.id.navRegister)
        val userbtn: View = view.findViewById(R.id.navData)
        val bottom :BottomNavigationView = view.findViewById(R.id.btm)

        val imgbtn: View = view.findViewById(R.id.imageButton)
        val imgbtn2: View = view.findViewById(R.id.imageButton2)

        imgbtn.setOnClickListener {
            setCurrentFragement(thirdFragment)
            bottom.selectedItemId = R.id.navRegister // Select the corresponding item in BottomNavigationView
        }

        // Set click listener for the second image button
        imgbtn2.setOnClickListener {
            setCurrentFragement(fourthFragment)
            bottom.selectedItemId = R.id.navData // Select the corresponding item in BottomNavigationView
        }


    }
    private fun setCurrentFragement(fragment: Fragment) {
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, fragment)
            commit()
        }

    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<HomeFragement> {
        override fun createFromParcel(parcel: Parcel): HomeFragement {
            return HomeFragement(parcel)
        }

        override fun newArray(size: Int): Array<HomeFragement?> {
            return arrayOfNulls(size)
        }
    }
}