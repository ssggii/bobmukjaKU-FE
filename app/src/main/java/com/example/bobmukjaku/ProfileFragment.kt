package com.example.bobmukjaku

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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

        // 시간표 각 배열 정보 저장 0(white), 1(lightgreen)
        val cellInformation = Array(18) { IntArray(5) { 0 } }

        // editTimetable Icon 클릭 이벤트 처리
        binding.editTimetable.setOnClickListener {
            val currentColorFilter = binding.editTimetable.colorFilter

            if (currentColorFilter == null) {
                // 수정 가능한 상태
                // 안내 문구 변경
                binding.timetableInfo.text = "불가능한 모든 시간 선택 후 다시 아이콘을 클릭해주세요."

                val color = ContextCompat.getColor(requireContext(), R.color.main)
                binding.editTimetable.setColorFilter(color, PorterDuff.Mode.SRC_IN)

                // Timetable 클릭 이벤트 처리
                val timetableCells = arrayOf(
                    arrayOf(binding.tt00, binding.tt01, binding.tt02, binding.tt03, binding.tt04),
                    arrayOf(binding.tt10, binding.tt11, binding.tt12, binding.tt13, binding.tt14),
                    arrayOf(binding.tt20, binding.tt21, binding.tt22, binding.tt23, binding.tt24),
                    arrayOf(binding.tt30, binding.tt31, binding.tt32, binding.tt33, binding.tt34),
                    arrayOf(binding.tt40, binding.tt41, binding.tt42, binding.tt43, binding.tt44),
                    arrayOf(binding.tt50, binding.tt51, binding.tt52, binding.tt53, binding.tt54),
                    arrayOf(binding.tt60, binding.tt61, binding.tt62, binding.tt63, binding.tt64),
                    arrayOf(binding.tt70, binding.tt71, binding.tt72, binding.tt73, binding.tt74),
                    arrayOf(binding.tt80, binding.tt81, binding.tt82, binding.tt83, binding.tt84),
                    arrayOf(binding.tt90, binding.tt91, binding.tt92, binding.tt93, binding.tt94),
                    arrayOf(binding.tt100, binding.tt101, binding.tt102, binding.tt103, binding.tt104),
                    arrayOf(binding.tt110, binding.tt111, binding.tt112, binding.tt113, binding.tt114),
                    arrayOf(binding.tt120, binding.tt121, binding.tt122, binding.tt123, binding.tt124),
                    arrayOf(binding.tt130, binding.tt131, binding.tt132, binding.tt133, binding.tt134),
                    arrayOf(binding.tt140, binding.tt141, binding.tt142, binding.tt143, binding.tt144),
                    arrayOf(binding.tt150, binding.tt151, binding.tt152, binding.tt153, binding.tt154),
                    arrayOf(binding.tt160, binding.tt161, binding.tt162, binding.tt163, binding.tt164),
                    arrayOf(binding.tt170, binding.tt171, binding.tt172, binding.tt173, binding.tt174)
                )

                for (rowIndex in timetableCells.indices) {
                    for (colIndex in timetableCells[rowIndex].indices) {
                        // 색상 초기화
                        val cellValue = cellInformation[rowIndex][colIndex]
                        timetableCells[rowIndex][colIndex].setBackgroundColor(
                            if (cellValue == 0)
                                ContextCompat.getColor(requireContext(), android.R.color.white)
                            else
                                ContextCompat.getColor(requireContext(), R.color.ttColor)
                        )

                        timetableCells[rowIndex][colIndex].setOnClickListener {
                            if (binding.editTimetable.colorFilter != null) {
                                // editTimetable이 main 컬러인 경우에만 동작하도록 추가 체크

                                // cell color 정보 저장
                                if (cellInformation[rowIndex][colIndex] == 0) {
                                    timetableCells[rowIndex][colIndex].setBackgroundColor(
                                        ContextCompat.getColor(requireContext(), R.color.ttColor)
                                    )
                                    cellInformation[rowIndex][colIndex] = 1
                                } else {
                                    timetableCells[rowIndex][colIndex].setBackgroundColor(
                                        ContextCompat.getColor(requireContext(), android.R.color.white)
                                    )
                                    cellInformation[rowIndex][colIndex] = 0
                                }
                            }
                        }
                    }
                }
            } else {
                // 수정 불가능한 상태
                // 안내 문구 변경
                binding.timetableInfo.text = "시간표 수정을 원하시면 시간표 옆 아이콘을 클릭하세요."
                binding.editTimetable.clearColorFilter()
            }
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