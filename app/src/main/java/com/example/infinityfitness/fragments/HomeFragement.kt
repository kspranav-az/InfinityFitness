package com.example.infinityfitness.fragments

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import androidx.fragment.app.Fragment
import com.example.infinityfitness.R

class HomeFragement() : Fragment(R.layout.home), Parcelable {

    constructor(parcel: Parcel) : this() {
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        val firstFragment = HomeFragement()
        val secondFragment = profileFragment()
        val thirdFragment = RegisterFragment()
        val fourthFragment = UserDataFragment()

        val imgbtn: View = view.findViewById(R.id.imageButton)
        val imgbtn2: View = view.findViewById(R.id.imageButton2)

        imgbtn.setOnClickListener{
            setCurrentFragement(thirdFragment)
        }
        imgbtn2.setOnClickListener{
            setCurrentFragement(fourthFragment)
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