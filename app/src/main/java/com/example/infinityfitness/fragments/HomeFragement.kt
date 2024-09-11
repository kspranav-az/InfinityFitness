package com.example.infinityfitness.fragments

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import androidx.fragment.app.Fragment
import com.example.infinityfitness.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeFragement() : Fragment(R.layout.home), Parcelable {

    constructor(parcel: Parcel) : this() {
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        val thirdFragment = RegisterFragment()
        val fourthFragment = UserDataFragment()

        val bottom :BottomNavigationView = view.findViewById(R.id.btm)

        val imgbtn: View = view.findViewById(R.id.imageButton)
        val imgbtn2: View = view.findViewById(R.id.imageButton2)

        imgbtn.setOnClickListener {
            setCurrentFragement(thirdFragment)
            bottom.selectedItemId = R.id.navRegister
        }

        imgbtn2.setOnClickListener {
            setCurrentFragement(fourthFragment)
            bottom.selectedItemId = R.id.navData
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