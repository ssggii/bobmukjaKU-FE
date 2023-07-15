package com.example.bobmukjaku

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.bobmukjaku.databinding.FragmentFriendBinding
import com.google.android.material.tabs.TabLayout

class FriendFragment : Fragment() {

    lateinit var binding: FragmentFriendBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFriendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Toolbar()
    }

    private fun Toolbar() {
        val tabLayout = binding.friendTabs

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val tabPosition = tab?.position

                tab?.icon?.setColorFilter(Color.parseColor("#28A872"), PorterDuff.Mode.SRC_IN)

                when (tabPosition) {
                    0 -> {
                        val friendListFragment = FriendListFragment()
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.friend_container, friendListFragment).commit()
                    }
                    1 -> {
                        val blockFragment = BlockFragment()
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.friend_container, blockFragment).commit()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // icon color black으로
                tab?.icon?.setColorFilter(Color.parseColor("#E0E0E0"), PorterDuff.Mode.SRC_IN)
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                //구현하지 않음
            }

        })

        // Set default fragment
        val friendListFragment = FriendListFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.friend_container, friendListFragment).commit()

        tabLayout.getTabAt(0)?.icon?.setColorFilter(Color.parseColor("#28A872"), PorterDuff.Mode.SRC_IN)
    }
}