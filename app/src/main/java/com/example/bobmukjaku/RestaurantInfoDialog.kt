package com.example.bobmukjaku

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.bobmukjaku.Model.*
import com.example.bobmukjaku.databinding.FragmentRestaurantInfoDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RestaurantInfoDialog(private val restaurant: RestaurantList, private val uid: Long) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentRestaurantInfoDialogBinding

    private val restaurantService = RetrofitClient.restaurantService
    private val accessToken = SharedPreferences.getString("accessToken", "")
    private val authorizationHeader = "Bearer $accessToken"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRestaurantInfoDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 데이터를 설정하여 UI 업데이트
        binding.restaurantName.text = restaurant.bizesNm
        binding.category.text = restaurant.indsMclsNm
        binding.categorySub.text = restaurant.indsSclsNm
        binding.restaurantAdd.text = restaurant.lnoAdr

        init()

        // 하트 버튼
        countHeart()

        // 스크랩 버튼 클릭 리스너 설정
        binding.scrapBtn.setOnClickListener {
            if (binding.scrapBtn.backgroundTintList == ContextCompat.getColorStateList(requireContext(), R.color.ect)) {
                deleteScrap()
            } else {
                addScrap()
            }
        }
    }

    private fun init() {
        val call = restaurantService.getRestaurantScrap(authorizationHeader, placeId = restaurant.bizesId)
        call.enqueue(object : Callback<List<ScrapInfo>> {
            override fun onResponse(call: Call<List<ScrapInfo>>, response: Response<List<ScrapInfo>>) {
                if (response.isSuccessful) {
                    val successResponse = response.body() // 서버에서 받은 스크랩 목록
                    if (successResponse != null) {
                        for (lists in successResponse) {
                            if (lists.uid == uid) {
                                binding.scrapBtn.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.ect)
                            }
                        }
                    }
                } else {
                    val errorCode = response.code()
                    if (errorCode != 404) {
                        Toast.makeText(requireContext(), "스크랩 여부 확인 실패. 에러 $errorCode", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<List<ScrapInfo>>, t: Throwable) {
                // 네트워크 오류 또는 기타 에러가 발생했을 때의 처리
                t.message?.let { it1 -> Log.i("[스크랩 여부 확인 에러: ]", it1) }
            }
        })
    }

    private fun countHeart() {
        val call = restaurantService.getScrapCount(authorizationHeader, placeId = restaurant.bizesId)
        call.enqueue(object : Callback<Int> {
            override fun onResponse(call: Call<Int>, response: Response<Int>) {
                if (response.isSuccessful) {
                    val scrapTotal = response.body()
                    binding.countScrap.text = scrapTotal.toString()
//                    val successCode = response.code()
//                    Toast.makeText(requireContext(), "스크랩 개수 로드. 성공 $successCode $uid", Toast.LENGTH_SHORT).show()
                } else {
                    val errorCode = response.code()
                    Toast.makeText(requireContext(), "스크랩 개수 로드 실패. 에러 $errorCode", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Int>, t: Throwable) {
                // 네트워크 오류 또는 기타 에러가 발생했을 때의 처리
                t.message?.let { it1 -> Log.i("[스크랩 개수 로드 에러: ]", it1) }
            }
        })
    }

    private fun addScrap() {
        val scrapInfo = ScrapPost(uid = uid, placeId = restaurant.bizesId, placeName = restaurant.bizesNm)
        Log.i("scrapInfo", scrapInfo.toString())
        // API 호출
        restaurantService.addScrap(authorizationHeader, scrapInfo).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // 성공적으로 스크랩 등록 완료
                    binding.scrapBtn.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.ect)
                    Toast.makeText(requireContext(), "스크랩 등록이 완료되었습니다.", Toast.LENGTH_SHORT).show()

                    // 업데이트된 하트 수를 가져옴
                    countHeart()
                } else {
                    val errorCode = response.code()
                    Toast.makeText(
                        requireContext(),
                        "스크랩 등록에 실패했습니다. 에러 코드: $errorCode",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                // 네트워크 오류 또는 기타 에러가 발생했을 때의 처리
                t.message?.let { Log.i("[스크랩 등록 실패: ]", it) }
            }
        })
    }

    private fun deleteScrap() {
        val scrapInfo = ScrapInfo(uid = uid, placeId = restaurant.bizesId)
        restaurantService.deleteScrap(authorizationHeader, scrapInfo).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // 성공적으로 스크랩 해제 완료
                    binding.scrapBtn.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.main)
                    Toast.makeText(requireContext(), "스크랩 해제가 완료되었습니다.", Toast.LENGTH_SHORT).show()

                    // 업데이트된 하트 수를 가져옴
                    countHeart()
                } else {
                    val errorCode = response.code()
                    Toast.makeText(
                        requireContext(),
                        "스크랩 해제에 실패했습니다. 에러 코드: $errorCode",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                // 네트워크 오류 또는 기타 에러가 발생했을 때의 처리
                t.message?.let { Log.i("[스크랩 해제 실패: ]", it) }
            }
        })
    }
}