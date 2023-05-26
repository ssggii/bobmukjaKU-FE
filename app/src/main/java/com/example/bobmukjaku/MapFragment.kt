package com.example.bobmukjaku

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.bobmukjaku.databinding.FragmentMapBinding
import com.google.android.material.tabs.TabLayout


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MapFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MapFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    lateinit var binding: FragmentMapBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MapFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MapFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}