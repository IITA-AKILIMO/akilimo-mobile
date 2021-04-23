package com.iita.akilimo.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class ViewPagerAdapter(fm: FragmentManager, behavior: Int, fragmentsSet: MutableSet<Fragment>) :
    FragmentStatePagerAdapter(fm, behavior) {

    private var mNumOfItems: MutableSet<Fragment> = fragmentsSet

    override fun getItem(position: Int): Fragment {
        return mNumOfItems.elementAt(position)
    }

    override fun getCount(): Int {
        return mNumOfItems.size
    }
}
