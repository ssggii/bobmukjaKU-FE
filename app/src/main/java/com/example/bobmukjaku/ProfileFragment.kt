package com.example.bobmukjaku

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.bobmukjaku.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    lateinit var binding: FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // profileImg 버튼 클릭 이벤트 처리
        binding.profileImg.setOnClickListener {
            val intent = Intent(requireContext(), ProfileColorActivity::class.java)
            startActivityForResult(intent, PROFILE_COLOR_REQUEST_CODE)
        }

        // profileBtn 버튼 클릭 이벤트 처리
        binding.profileBtn.setOnClickListener {
            val intent = Intent(requireContext(), ProfileColorActivity::class.java)
            startActivityForResult(intent, PROFILE_COLOR_REQUEST_CODE)
        }
    }

    companion object {
        private const val PROFILE_COLOR_REQUEST_CODE = 100
    }

    // Override onActivityResult to set the result when returning from ProfileColorActivity.kt
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PROFILE_COLOR_REQUEST_CODE && resultCode == RESULT_OK) {
            val selectedItemId = data?.getIntExtra("selectedItemId", R.id.forth)
            activity?.setResult(RESULT_OK, Intent().putExtra("selectedItemId", selectedItemId))
        }
    }
}