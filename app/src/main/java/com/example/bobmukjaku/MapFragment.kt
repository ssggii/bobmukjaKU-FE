package com.example.bobmukjaku

import MapListFragment
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.bobmukjaku.databinding.FragmentMapBinding
import com.google.android.material.tabs.TabLayout

class MapFragment : Fragment() {

    lateinit var binding: FragmentMapBinding
    lateinit var mContext: Context
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Toolbar()
    }

    private fun Toolbar() {
        val tabLayout = binding.mapTabs

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val tabPosition = tab?.position

                tab?.icon?.setColorFilter(Color.parseColor("#28A872"), PorterDuff.Mode.SRC_IN)

                when (tabPosition) {
                    0 -> {
                        val MapListFragment = MapListFragment()
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.map_container, MapListFragment).commit()
                    }
                    1 -> {
                        val MapScrapFragment = MapScrapFragment()
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.map_container, MapScrapFragment).commit()
                    }
                    2 -> {
                        val MapReviewFragment = MapReviewFragment()
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.map_container, MapReviewFragment).commit()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // icon color black으로
                tab?.icon?.clearColorFilter()
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                //구현하지 않음
            }

        })

        // Set default fragment
        val mapListFragment = MapListFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.map_container, mapListFragment).commit()

        tabLayout.getTabAt(0)?.icon?.setColorFilter(Color.parseColor("#28A872"), PorterDuff.Mode.SRC_IN)
    }
}