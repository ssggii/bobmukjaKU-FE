package com.bobmukja.bobmukjaku

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bobmukja.bobmukjaku.Model.Member
import com.bobmukja.bobmukjaku.Model.RetrofitClient
import com.bobmukja.bobmukjaku.Model.SharedPreferences
import com.bobmukja.bobmukjaku.Model.TimeBlock
import com.bobmukja.bobmukjaku.databinding.FragmentProfileBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate

class ProfileFragment : Fragment() {

    lateinit var binding: FragmentProfileBinding
    lateinit var mContext: Context
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 사용자 정보 가져와서 화면에 설정
        displayInfo()

        // 사용자 배경색 정보 가져와서 화면에 설정
        displayProfileColor()

        // 시간표 첫 로드
        displayTimeTable()

        // 시간표 설정하기
        setTimeTable()

        // 정보 수정 버튼 이벤트
        modifyInfo()

        //로그아웃 버튼 이벤트
//        logoutInfo()

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

    private fun displayTimeTable() {
        val memberService = RetrofitClient.memberService
        val accessToken = SharedPreferences.getString("accessToken", "")
        val authorizationHeader = "Bearer $accessToken"

        val call = accessToken?.let { memberService.getTimeTable(authorizationHeader) }
        call?.enqueue(object : Callback<List<TimeBlock>> {
            override fun onResponse(call: Call<List<TimeBlock>>, response: Response<List<TimeBlock>>) {
                if (response.isSuccessful) {
                    val timeBlocks = response.body()

                    if (timeBlocks != null) {
                        // 시간표 정보를 가져온 경우에만 처리
                        updateTimetableUI(timeBlocks)
                    }
                } else {
                    val errorCode = response.code()
                    Log.i("시간표 정보 조회 실패 ", "에러 코드: $errorCode")
                }
            }

            override fun onFailure(call: Call<List<TimeBlock>>, t: Throwable) {
                // 네트워크 오류 또는 기타 에러가 발생했을 때의 처리
                t.message?.let { it1 -> Log.i("[시간표 조회 에러: ]", it1) }
            }
        })
    }

    // 시간표 UI 업데이트 함수
    private fun updateTimetableUI(timeBlocks: List<TimeBlock>) {
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

        for (timeBlock in timeBlocks) {
            val dayOfWeek = timeBlock.dayOfWeek.toIntOrNull()
            val time = timeBlock.time

            if (dayOfWeek != null && dayOfWeek in 1..5) {
                val colIndex = dayOfWeek - 1  // API에서 요일은 1부터 시작하므로 -1
                val rowIndex = getIndexFromTime(time)

                if (rowIndex in timetableCells.indices) {
                    timetableCells[rowIndex][colIndex].setBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.ttColor)
                    )
                }
            }
        }
    }

    // 시간을 인덱스로 변환하는 함수
    private fun getIndexFromTime(time: String): Int {
        val startTime = 9  // 시작 시간은 9시
        val timeUnit = 30  // 시간 단위는 30분
        val parts = time.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()
        val elapsedMinutes = (hour - startTime) * 60 + minute
        return elapsedMinutes / timeUnit
    }

    private fun setTimeTable() {
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
                saveTimeTable(cellInformation)
            }
        }
    }

    // 시간표 정보를 서버에 저장하는 함수
    private fun saveTimeTable(timeTable: Array<IntArray>) {
        val memberService = RetrofitClient.memberService
        val accessToken = SharedPreferences.getString("accessToken", "")
        val authorizationHeader = "Bearer $accessToken"

        val timeBlockList = mutableListOf<TimeBlock>()

        for (rowIndex in timeTable.indices) {
            for (colIndex in timeTable[rowIndex].indices) {
                if (timeTable[rowIndex][colIndex] == 1) {
                    // 값이 1인 경우에만 시간표 정보를 생성하여 리스트에 추가
                    val dayOfWeek = (colIndex + 1).toString()  // 요일은 1부터 시작하므로 +1
                    val time = getTimeFromIndex(rowIndex)
                    timeBlockList.add(TimeBlock(dayOfWeek, time))
                }
            }
        }

        val call = accessToken?.let {
            memberService.saveTimeTable(authorizationHeader, timeBlockList)
        }

        call?.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // 시간표 정보 저장 성공 처리
                    Toast.makeText(
                        requireContext(),
                        "시간표 정보가 저장되었습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val errorCode = response.code()
                    Toast.makeText(
                        requireContext(),
                        "시간표 정보를 저장하지 못했습니다. 다시 시도해주세요.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                // 네트워크 오류 또는 기타 에러가 발생했을 때의 처리
                t.message?.let { it1 -> Log.i("[시간표 저장 에러: ]", it1) }
            }
        })
    }

    // 인덱스를 시간으로 변환하는 함수
    private fun getTimeFromIndex(index: Int): String {
        val startTime = 9  // 시작 시간은 9시
        val timeUnit = 30  // 시간 단위는 30분
        val hour = startTime + (index * timeUnit) / 60
        val minute = (index * timeUnit) % 60
        return String.format("%02d:%02d", hour, minute)
    }

    private fun displayInfo() {
        val memberService = RetrofitClient.memberService
        val accessToken = SharedPreferences.getString("accessToken", "")

        val authorizationHeader = "Bearer $accessToken"

        val call = accessToken?.let { memberService.selectOne(authorizationHeader) }
        call?.enqueue(object : Callback<Member> {
            override fun onResponse(call: Call<Member>, response: Response<Member>) {
                if (response.isSuccessful) {
                    val member = response.body()
                    val nickname = member?.memberNickName
                    var level = member?.rate.toString().toInt()
                    if (level <= 20) {
                        binding.level.text = "1"
                        binding.profileImg.setBackgroundResource(R.drawable.ku_1)
                    } else if (level <= 40) {
                        level -= 20
                        binding.level.text = "2"
                        binding.profileImg.setBackgroundResource(R.drawable.ku_2)
                    } else if (level <= 60) {
                        level -= 40
                        binding.level.text = "3"
                        binding.profileImg.setBackgroundResource(R.drawable.ku_3)
                    } else if (level <= 80) {
                        level -= 60
                        binding.level.text = "4"
                        binding.profileImg.setBackgroundResource(R.drawable.ku_4)
                    } else {
                        level -= 80
                        binding.level.text = "5"
                        binding.profileImg.setBackgroundResource(R.drawable.ku_5)
                    }

                    val levelProgressBar = binding.greenLevel
                    val totalWidth = binding.totalLevelBar.width // 전체 바의 너비

                    val levelRange = 20

                    val params = levelProgressBar.layoutParams as ViewGroup.MarginLayoutParams
                    val greenBarWidth = (level.toFloat() / levelRange) * totalWidth
                    params.width = greenBarWidth.toInt()

                    levelProgressBar.layoutParams = params

                    val certificatedDate = member?.certificatedAt
                    binding.nickname.text = nickname
                    binding.studentCheck.text = certificatedDate
                } else {
                    val errorCode = response.code()
                    Log.i("사용자 정보 로드 ", "실패 $errorCode")
                }
            }

            override fun onFailure(call: Call<Member>, t: Throwable) {
                // 네트워크 오류 처리
                t.message?.let { it1 -> Log.i("사용자 정보 로드 실패. 기타 에러", it1) }
            }
        })
    }

    private fun displayProfileColor() {
        val memberService = RetrofitClient.memberService
        val accessToken = SharedPreferences.getString("accessToken", "")

        val authorizationHeader = "Bearer $accessToken"

        val call = accessToken?.let { memberService.selectOne(authorizationHeader) }
        call?.enqueue(object : Callback<Member> {
            override fun onResponse(call: Call<Member>, response: Response<Member>) {
                if (response.isSuccessful) {
                    val member = response.body()
                    val colorResName = member?.profileColor

                    // 리소스 식별자 가져오기
                    val resourceId = mContext.resources.getIdentifier(
                        colorResName, "drawable", mContext.packageName
                    )

                    if (resourceId != 0) {
                        // 리소스가 존재할 경우 배경색 설정
                        binding.profileBG.setBackgroundResource(resourceId)
                    } else {
                        Toast.makeText(
                            mContext,
                            "프로필 배경색 리소스를 찾을 수 없습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    val errorCode = response.code()
                    Log.i("프로필 배경색 로드 ", "실패 $errorCode")
                }
            }

            override fun onFailure(call: Call<Member>, t: Throwable) {
                // 네트워크 오류 처리
                t.message?.let { it1 -> Log.i("프로필 배경색 가져오기 실패. 기타 에러", it1) }
            }
        })
    }

    private fun modifyInfo() {
        binding.modifyBtn.setOnClickListener {
            val intent = Intent(requireContext(), ModifyInfoActivity::class.java)
            startActivityForResult(intent, PROFILE_MODIFY_REQUEST_CODE)
        }
    }

//    private fun logoutInfo(){
//        val accessToken = SharedPreferences.getString("accessToken", "")
//        val authorizationHeader = "Bearer $accessToken"
//        binding.logoutBtn.setOnClickListener {
//
//            RetrofitClient.memberService.logout(authorizationHeader)
//                .enqueue(object: Callback<Unit>{
//                    override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
//                        if(response.isSuccessful){
//                            //로그아웃 성공
//                            //로그인 화면으로 전환(로그인화면에서 자동으로 SharedPreference에 있는 jwt token(accessToken, refreshToken)삭제
//                            val intent = Intent(requireContext(), LoginActivity::class.java)
//                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                            startActivity(intent)
//                        }else{
//                            Toast.makeText(requireContext(), "로그아웃 실패", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//
//                    override fun onFailure(call: Call<Unit>, t: Throwable) {
//                        //로그아웃 실패
//                        Toast.makeText(requireContext(), "로그아웃 실패", Toast.LENGTH_SHORT).show()
//                    }
//                })
//        }
//    }

//        private fun deleteMember(){
//            val accessToken = SharedPreferences.getString("accessToken", "")
//            val authorizationHeader = "Bearer $accessToken"
//            binding.userDeleteBtn.setOnClickListener {
//
//                RetrofitClient.memberService.deleteMember(authorizationHeader)
//                    .enqueue(object: Callback<Unit>{
//                        override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
//                            if(response.isSuccessful){
//                                //회원탈퇴 성공
//                                //로그인 화면으로 전환(로그인화면에서 자동으로 SharedPreference에 있는 jwt token(accessToken, refreshToken)삭제
//                                val intent = Intent(requireContext(), LoginActivity::class.java)
//                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                                startActivity(intent)
//                            }else{
//                                Toast.makeText(requireContext(), "회원탈퇴 실패", Toast.LENGTH_SHORT).show()
//                            }
//                        }
//
//                        override fun onFailure(call: Call<Unit>, t: Throwable) {
//                            //로그아웃 실패
//                            Toast.makeText(requireContext(), "회원탈퇴 실패", Toast.LENGTH_SHORT).show()
//                        }
//                    })
//            }
//        }

    companion object {
        private const val PROFILE_COLOR_REQUEST_CODE = 100
        private const val PROFILE_MODIFY_REQUEST_CODE = 101
    }

    // 프로필 정보 수정 후 다시 프로필 화면으로 돌아감
    // 프로필 배경 변경 후 다시 프로필 화면으로 돌아감
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == PROFILE_COLOR_REQUEST_CODE && resultCode == RESULT_OK)) {
            // 사용자 배경색 정보 가져와서 화면에 설정
            displayProfileColor()
            val selectedItemId = data?.getIntExtra("selectedItemId", R.id.forth)
            activity?.setResult(RESULT_OK, Intent().putExtra("selectedItemId", selectedItemId))
        } else if ((requestCode == PROFILE_MODIFY_REQUEST_CODE && resultCode == RESULT_OK)) {
            // 사용자 닉네임 정보 가져와서 화면에 설정
            displayInfo()
            val selectedItemId = data?.getIntExtra("selectedItemId", R.id.forth)
            activity?.setResult(RESULT_OK, Intent().putExtra("selectedItemId", selectedItemId))
        }
    }
}