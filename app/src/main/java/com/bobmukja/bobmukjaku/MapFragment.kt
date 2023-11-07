package com.bobmukja.bobmukjaku

import MapListFragment
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bobmukja.bobmukjaku.databinding.FragmentMapBinding
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

                // MapFragment 내부에서 데이터 설정 및 전달
                val data = arguments
                if (data != null) {
                    val roomId = data.getLong("roomId")
                    val roomName = data.getString("roomName")
                    val meetingDate = data.getString("meetingDate")
                    val startTime = data.getString("startTime")
                    val endTime = data.getString("endTime")
                    val kindOfFood = data.getString("kindOfFood")
                    val total = data.getInt("total")
                    val currentNum = data.getInt("currentNum")

                    val data2 = Bundle()

                    data2.putLong("roomId", roomId)
                    data2.putString("roomName", roomName)
                    data2.putString("meetingDate", meetingDate)
                    data2.putString("startTime", startTime)
                    data2.putString("endTime", endTime)
                    data2.putString("kindOfFood", kindOfFood)
                    data2.putInt("total", total)
                    data2.putInt("currentNum", currentNum)

                    when (tabPosition) {
                        0 -> {
                            val mapListFragment = MapListFragment()
                            mapListFragment.arguments = data2
                            requireActivity().supportFragmentManager.beginTransaction()
                                .replace(R.id.map_container, mapListFragment).commit()
                        }
                        1 -> {
                            val mapScrapFragment = MapScrapFragment()
                            mapScrapFragment.arguments = data2
                            requireActivity().supportFragmentManager.beginTransaction()
                                .replace(R.id.map_container, mapScrapFragment).commit()
                        }
                        2 -> {
                            val mapReviewFragment = MapReviewFragment()
                            mapReviewFragment.arguments = data2
                            requireActivity().supportFragmentManager.beginTransaction()
                                .replace(R.id.map_container, mapReviewFragment).commit()
                        }
                    }
                } else {
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
        // MapFragment 내부에서 데이터 설정 및 전달
        val data = arguments
        if (data != null) {
            val roomId = data.getLong("roomId")
            val roomName = data.getString("roomName")
            val meetingDate = data.getString("meetingDate")
            val startTime = data.getString("startTime")
            val endTime = data.getString("endTime")
            val kindOfFood = data.getString("kindOfFood")
            val total = data.getInt("total")
            val currentNum = data.getInt("currentNum")

            val data2 = Bundle()

            data2.putLong("roomId", roomId)
            data2.putString("roomName", roomName)
            data2.putString("meetingDate", meetingDate)
            data2.putString("startTime", startTime)
            data2.putString("endTime", endTime)
            data2.putString("kindOfFood", kindOfFood)
            data2.putInt("total", total)
            data2.putInt("currentNum", currentNum)

            val mapListFragment = MapListFragment()
            mapListFragment.arguments = data2

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.map_container, mapListFragment).commit()
        } else {
            val mapListFragment = MapListFragment()

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.map_container, mapListFragment).commit()
        }
        tabLayout.getTabAt(0)?.icon?.setColorFilter(Color.parseColor("#28A872"), PorterDuff.Mode.SRC_IN)
    }
}