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

        // 스크랩 버튼 클릭 리스너 설정
        binding.scrapBtn.setOnClickListener {
            addScrap()
        }
    }

    private fun addScrap() {
        val scrapInfo = ScrapInfo(uid = uid, placeId = restaurant.bizesId)
        Log.i("scrapInfo", scrapInfo.toString())
        // API 호출
        restaurantService.addScrap(authorizationHeader, scrapInfo).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // 성공적으로 스크랩 등록 완료
                    binding.scrapBtn.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.ect)
                    Toast.makeText(requireContext(), "스크랩 등록이 완료되었습니다.", Toast.LENGTH_SHORT).show()
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
}