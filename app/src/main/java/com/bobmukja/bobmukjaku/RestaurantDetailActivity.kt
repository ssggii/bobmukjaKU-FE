package com.bobmukja.bobmukjaku

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bobmukja.bobmukjaku.Model.*
import com.bobmukja.bobmukjaku.databinding.ActivityRestaurantDetailBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RestaurantDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRestaurantDetailBinding
    private lateinit var reviewAdapter: RestaurantDetailAdapter2

    private val restaurantService = RetrofitClient.restaurantService
    private val accessToken = SharedPreferences.getString("accessToken", "")
    private val authorizationHeader = "Bearer $accessToken"

    // Intent로부터 데이터를 받아옵니다.
    private var bizesId: String? = null
    private var bizesNm: String? = null
    private var lnoAdr: String? = null
    private var indsMclsNm: String? = null
    private var indsSclsNm: String? = null
    private var uid: Long = 0L

    var reviewList = mutableListOf<ReviewResponse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRestaurantDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Intent로부터 데이터를 가져옵니다.
        bizesId = intent.getStringExtra("bizesId")
        bizesNm = intent.getStringExtra("bizesNm")
        lnoAdr = intent.getStringExtra("lnoAdr")
        indsMclsNm = intent.getStringExtra("indsMclsNm")
        indsSclsNm = intent.getStringExtra("indsSclsNm")
        uid = intent.getLongExtra("uid", 0L)

        // 데이터를 설정하여 UI 업데이트
        binding.restaurantName.text = bizesNm
        binding.category.text = indsMclsNm
        binding.categorySub.text = indsSclsNm
        binding.restaurantAdd.text = lnoAdr

        getRestaurantScrap()
        getRestaurantReview()
        countHeart()

        // 스크랩 버튼 클릭 리스너 설정
        binding.scrapBtn.setOnClickListener {
            if (binding.scrapBtn.backgroundTintList == ContextCompat.getColorStateList(this, R.color.ect)) {
                deleteScrap()
            } else {
                addScrap()
            }
        }

        // 공유하기 버튼 클릭 리스너 설정
        binding.shareBtn.setOnClickListener {
            val roomId = intent.getLongExtra("roomId", -1)
            if (roomId.toInt() != -1) {
                val intent = Intent(this@RestaurantDetailActivity, ChatActivity::class.java)

                intent.putExtra("placeName", bizesNm)
                intent.putExtra("placeAddress", lnoAdr)
                if (reviewList != null && reviewList.isNotEmpty()) {
                    intent.putExtra("imageUrl", reviewList[0].imageUrl)
                } else if (reviewList[0].imageUrl == "nodata"){
                    intent.putExtra("imageUrl", "nodata")
                } else {
                    intent.putExtra("imageUrl", "nodata")
                }

                intent.putExtra("roomId", intent.getLongExtra("roomId", -1))
                intent.putExtra("roomName", intent.getStringExtra("roomName"))
                intent.putExtra("meetingDate", intent.getStringExtra("meetingDate"))
                intent.putExtra("startTime", intent.getStringExtra("startTime"))
                intent.putExtra("endTime", intent.getStringExtra("endTime"))
                intent.putExtra("kindOfFood", intent.getStringExtra("kindOfFood"))
                intent.putExtra("total", intent.getIntExtra("total", -1))
                intent.putExtra("currentNum", intent.getIntExtra("currentNum", -1))

                startActivity(intent)
            } else {
                Toast.makeText(this@RestaurantDetailActivity, "음식점 정보를 공유할 채팅방이 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getRestaurantReview() {
        // 리뷰 목록 API
        bizesId?.let {
            RetrofitClient.restaurantService.getRestaurantReview(
                authorizationHeader,
                it
            )
        }?.enqueue(object : Callback<List<ReviewResponse>> {
            override fun onResponse(
                call: Call<List<ReviewResponse>>,
                response: Response<List<ReviewResponse>>
            ) {
                if (response.isSuccessful) {
                    val reviewListResponse = response.body() // 서버에서 받은 리뷰 목록
                    if (reviewListResponse != null) {
                        reviewList.clear()
                        reviewList.addAll(reviewListResponse) // reviewList에 업데이트된 리뷰 목록 저장
                        reviewAdapter.updateItems(reviewList) // 어댑터에 업데이트된 목록 전달

                        binding.totalReview.text = reviewList.size.toString()
                    } else {
                        reviewList.clear()
                        reviewAdapter.updateItems(reviewList)
                    }
                    val successCode = response.code()
                    Log.i("음식점 리뷰 목록 로드", "성공 $successCode $bizesId")
                } else {
                    val errorCode = response.code()
                    Log.i("음식점 리뷰 목록 로드", "실패. 에러 $errorCode")
                }
            }

            override fun onFailure(call: Call<List<ReviewResponse>>, t: Throwable) {
                // 네트워크 오류 또는 기타 에러가 발생했을 때의 처리
                t.message?.let { it1 -> Log.i("[음식점 리뷰 목록 로드 에러: ]", it1) }
            }
        })

        binding.reviewList.layoutManager =
            LinearLayoutManager(binding.root.context, LinearLayoutManager.VERTICAL, false)
        reviewAdapter = RestaurantDetailAdapter2(reviewList)
        reviewAdapter.onItemClickListener = object : RestaurantDetailAdapter2.OnItemClickListener {
            override fun onItemClick(pos: Int, reviewInfo: ReviewResponse) {
            }
        }
        binding.reviewList.adapter = reviewAdapter
    }

    private fun getRestaurantScrap() {
        bizesId?.let { restaurantService.getRestaurantScrap(authorizationHeader, placeId = it) }
            ?.enqueue(object : Callback<List<ScrapPost>> {
                override fun onResponse(
                    call: Call<List<ScrapPost>>,
                    response: Response<List<ScrapPost>>
                ) {
                    if (response.isSuccessful) {
                        val successResponse = response.body() // 서버에서 받은 스크랩 목록
                        if (successResponse != null) {
                            for (lists in successResponse) {
                                if (lists.uid == uid) {
                                    binding.scrapBtn.backgroundTintList =
                                        ContextCompat.getColorStateList(
                                            this@RestaurantDetailActivity,
                                            R.color.ect
                                        )
                                }
                            }
                        }
                    } else {
                        val errorCode = response.code()
                        if (errorCode != 404) {
                            Log.i("스크랩 여부 확인 실패 ", "에러 $errorCode")
                        }
                    }
                }

                override fun onFailure(call: Call<List<ScrapPost>>, t: Throwable) {
                    // 네트워크 오류 또는 기타 에러가 발생했을 때의 처리
                    t.message?.let { it1 -> Log.i("[스크랩 여부 확인 에러: ]", it1) }
                }
            })
    }

    private fun countHeart() {
        bizesId?.let { restaurantService.getScrapCount(authorizationHeader, placeId = it) }
            ?.enqueue(object : Callback<Int> {
                override fun onResponse(call: Call<Int>, response: Response<Int>) {
                    if (response.isSuccessful) {
                        val scrapTotal = response.body()
                        binding.countScrap.text = scrapTotal.toString()
                    } else {
                        val errorCode = response.code()
                        Log.i("스크랩 개수 로드 실패 ", "에러 $errorCode")
                    }
                }

                override fun onFailure(call: Call<Int>, t: Throwable) {
                    // 네트워크 오류 또는 기타 에러가 발생했을 때의 처리
                    t.message?.let { it1 -> Log.i("[스크랩 개수 로드 에러: ]", it1) }
                }
            })
    }

    private fun addScrap() {
        val scrapInfo = ScrapPost(uid = uid, placeId = bizesId.toString(), placeName = bizesNm.toString())

        // API 호출
        restaurantService.addScrap(authorizationHeader, scrapInfo).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // 성공적으로 스크랩 등록 완료
                    binding.scrapBtn.backgroundTintList = ContextCompat.getColorStateList(this@RestaurantDetailActivity, R.color.ect)
                    Toast.makeText(this@RestaurantDetailActivity, "스크랩 등록이 완료되었습니다.", Toast.LENGTH_SHORT).show()

                    // 업데이트된 하트 수를 가져옴
                    countHeart()
                } else {
                    val errorCode = response.code()
                    Toast.makeText(
                        this@RestaurantDetailActivity,
                        "스크랩 등록에 실패했습니다. 다시 시도해주세요.",
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
        val scrapInfo = ScrapInfo(uid = uid, placeId = bizesId.toString())

        restaurantService.deleteScrap(authorizationHeader, scrapInfo).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // 성공적으로 스크랩 해제 완료
                    binding.scrapBtn.backgroundTintList = ContextCompat.getColorStateList(this@RestaurantDetailActivity, R.color.main)
                    Toast.makeText(this@RestaurantDetailActivity, "스크랩 해제가 완료되었습니다.", Toast.LENGTH_SHORT).show()

                    // 업데이트된 하트 수를 가져옴
                    countHeart()
                } else {
                    val errorCode = response.code()
                    Toast.makeText(
                        this@RestaurantDetailActivity,
                        "스크랩 해제에 실패했습니다. 다시 시도해주세요.",
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
