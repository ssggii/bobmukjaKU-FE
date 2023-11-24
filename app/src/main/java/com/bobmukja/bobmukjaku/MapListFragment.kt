package com.bobmukja.bobmukjaku

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bobmukja.bobmukjaku.Dto.TopRestuarantDto
import com.bobmukja.bobmukjaku.Model.Member
import com.bobmukja.bobmukjaku.Model.RestaurantList
import com.bobmukja.bobmukjaku.Model.RetrofitClient
import com.bobmukja.bobmukjaku.Model.SharedPreferences
import com.bobmukja.bobmukjaku.RoomDB.RestaurantDatabase
import com.bobmukja.bobmukjaku.databinding.FragmentMapListBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapListFragment : Fragment(), OnMapReadyCallback {
    private lateinit var mapView: MapView
    private val markerInfoWindowMap = mutableMapOf<Marker, DialogFragment>()
    private lateinit var restaurantDb: RestaurantDatabase
    private var restaurants = listOf<RestaurantList>()
    private val markerList = mutableListOf<Marker>()

    lateinit var binding: FragmentMapListBinding
    lateinit var mContext: Context

    private val accessToken = SharedPreferences.getString("accessToken", "")
    private val authorizationHeader = "Bearer $accessToken"
    private val restaurantService = RetrofitClient.restaurantService
    private var topRestaurants = mutableListOf<TopRestuarantDto>()

    var uid: Long = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context

        CoroutineScope(Dispatchers.Main).launch {
            initRestaurantList()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMapListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onMapReady(naverMap: NaverMap) {
        // 지도 디폴트 위치 고정
        val cameraPosition = CameraPosition(
            LatLng(37.54080009, 127.0701751),  // 위치 지정
            13.0 // 줌 레벨
        )
        naverMap.cameraPosition = cameraPosition

        initMapMarker(naverMap)
        setupSearchListener(naverMap)
    }

    private suspend fun initRestaurantList(){
        withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
            restaurantDb = RestaurantDatabase.getDatabase(mContext)
            restaurants = restaurantDb.restaurantListDao().getAllRecord()
            Log.i("finish", restaurants.size.toString())
        }
    }

    private fun setupSearchListener(naverMap: NaverMap) {
        val searchEditText = binding.restaurantSearch

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // 사용하지 않음
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 이전에 표시된 마커들 삭제
                markerList.forEach { it.map = null }
                markerList.clear()

                if (s.toString().contains("한식")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsMclsNm.equals("한식")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("일식")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsMclsNm.equals("일식")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("양식")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsMclsNm.equals("서양식")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("중식")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsMclsNm.equals("중식")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("동남아시아")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsMclsNm.equals("동남아시아")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("외국식")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsMclsNm.equals("기타 외국식")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("건대 베트남") || s.toString().contains("건대베트남")
                    || s.toString() == "베트남" || s.toString().contains("베트남맛집") || s.toString().contains("베트남 맛집")
                    || s.toString().contains("베트남식당") || s.toString().contains("베트남 식당")
                    || s.toString().contains("베트남음식점") || s.toString().contains("베트남 음식점")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsSclsNm.equals("베트남식 전문")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("피자")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsSclsNm == ("피자")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("건대 버거") || s.toString().contains("건대버거")
                    || s.toString() == "버거" || s.toString().contains("버거맛집") || s.toString().contains("버거 맛집")
                    || s.toString().contains("버거식당") || s.toString().contains("버거 식당")
                    || s.toString().contains("버거음식점") || s.toString().contains("버거 음식점")
                    || s.toString().contains("건대 햄버거") || s.toString().contains("건대햄버거")
                    || s.toString() == "햄버거" || s.toString().contains("햄버거맛집") || s.toString().contains("햄버거 맛집")
                    || s.toString().contains("햄버거식당") || s.toString().contains("햄버거 식당")
                    || s.toString().contains("햄버거음식점") || s.toString().contains("햄버거 음식점")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsSclsNm == ("버거")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("치킨")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsSclsNm == ("치킨")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("건대 마라탕") || s.toString().contains("건대마라탕")
                    || s.toString() == ("마라탕") || s.toString().contains("마라탕맛집") || s.toString().contains("마라탕 맛집")
                    || s.toString().contains("마라탕 식당") || s.toString().contains("마라탕식당")
                    || s.toString().contains("마라탕 음식점") || s.toString().contains("마라탕음식점")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsSclsNm == "마라탕/훠궈") {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("건대 초밥") || s.toString().contains("건대초밥")
                    || s.toString() == ("초밥") || s.toString().contains("초밥맛집") || s.toString().contains("초밥 맛집")
                    || s.toString().contains("초밥 식당") || s.toString().contains("초밥식당")
                    || s.toString().contains("초밥 음식점") || s.toString().contains("초밥음식점")
                    || s.toString() == ("회") || s.toString().contains("회맛집") || s.toString().contains("회 맛집")
                    || s.toString().contains("회 식당") || s.toString().contains("회식당")
                    || s.toString().contains("회 음식점") || s.toString().contains("회음식점")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsSclsNm == "일식 회/초밥") {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("건대 파스타") || s.toString().contains("건대파스타")
                    || s.toString() == ("파스타") || s.toString().contains("파스타맛집") || s.toString().contains("파스타 맛집")
                    || s.toString().contains("파스타 식당") || s.toString().contains("파스타식당")
                    || s.toString().contains("파스타 음식점") || s.toString().contains("파스타음식점")
                    || s.toString() == ("스테이크") || s.toString().contains("스테이크맛집") || s.toString().contains("스테이크 맛집")
                    || s.toString().contains("스테이크 식당") || s.toString().contains("스테이크식당")
                    || s.toString().contains("스테이크 음식점") || s.toString().contains("스테이크음식점")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsSclsNm == "파스타/스테이크") {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("중국집")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsSclsNm == ("중국집")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("분식")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsSclsNm == ("김밥/만두/분식")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("카페")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsSclsNm == ("카페")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("족발") || s.toString().contains("보쌈")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsSclsNm == ("족발/보쌈")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("국수") || s.toString().contains("칼국수")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsSclsNm == ("국수/칼국수")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("돼지고기")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsSclsNm == ("돼지고기 구이/찜")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("소고기")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsSclsNm == ("소고기 구이/찜")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("곱창")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsSclsNm == ("곱창 전골/구이")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("오리고기")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsSclsNm == ("닭/오리고기 구이/찜")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("백반") || s.toString().contains("한정식")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsSclsNm == ("백반/한정식")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("샌드위치") || s.toString() == "토스트" || s.toString() == "샐러드"
                    || s.toString().contains("토스트맛집") || s.toString().contains("토스트 맛집")
                    || s.toString().contains("샐러드맛집") || s.toString().contains("샐러드 맛집")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsSclsNm == ("토스트/샌드위치/샐러드")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("건대 떡볶이") || s.toString().contains("건대떡볶이")
                    || s.toString() == ("떡볶이") || s.toString().contains("떡볶이맛집") || s.toString().contains("떡볶이 맛집")
                    || s.toString() == ("만두") || s.toString().contains("만두맛집") || s.toString().contains("만두 맛집")
                    || s.toString().contains("건대 만두") || s.toString().contains("건대만두")
                    || s.toString() == ("김밥") || s.toString().contains("김밥맛집") || s.toString().contains("김밥 맛집")
                    || s.toString().contains("건대 김밥") || s.toString().contains("건대김밥")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsSclsNm == "김밥/만두/분식") {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("화양동")) {
                    for (restaurant in restaurants) {
                        if (restaurant.lnoAdr.contains("화양동")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("자양동")) {
                    for (restaurant in restaurants) {
                        if (restaurant.lnoAdr.contains("자양동")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("구의동")) {
                    for (restaurant in restaurants) {
                        if (restaurant.lnoAdr.contains("구의동")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("군자동")) {
                    for (restaurant in restaurants) {
                        if (restaurant.lnoAdr.contains("군자동")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else {
                    for (restaurant in restaurants) {
                        if (restaurant.bizesNm.contains(s.toString())) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // 이전에 표시된 마커들 삭제
                markerList.forEach { it.map = null }
                markerList.clear()

                if (s.toString().contains("한식")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsMclsNm.equals("한식")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("일식")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsMclsNm.equals("일식")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("양식")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsMclsNm.equals("서양식")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("중식")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsMclsNm.equals("중식")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("동남아시아")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsMclsNm.equals("동남아시아")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("외국식")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsMclsNm.equals("기타 외국식")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("건대 베트남") || s.toString().contains("건대베트남")
                    || s.toString() == "베트남" || s.toString().contains("베트남맛집") || s.toString().contains("베트남 맛집")
                    || s.toString().contains("베트남식당") || s.toString().contains("베트남 식당")
                    || s.toString().contains("베트남음식점") || s.toString().contains("베트남 음식점")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsSclsNm.equals("베트남식 전문")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("피자")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsSclsNm == ("피자")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("건대 버거") || s.toString().contains("건대버거")
                    || s.toString() == "버거" || s.toString().contains("버거맛집") || s.toString().contains("버거 맛집")
                    || s.toString().contains("버거식당") || s.toString().contains("버거 식당")
                    || s.toString().contains("버거음식점") || s.toString().contains("버거 음식점")
                    || s.toString().contains("건대 햄버거") || s.toString().contains("건대햄버거")
                    || s.toString() == "햄버거" || s.toString().contains("햄버거맛집") || s.toString().contains("햄버거 맛집")
                    || s.toString().contains("햄버거식당") || s.toString().contains("햄버거 식당")
                    || s.toString().contains("햄버거음식점") || s.toString().contains("햄버거 음식점")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsSclsNm == ("버거")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("치킨")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsSclsNm == ("치킨")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("건대 마라탕") || s.toString().contains("건대마라탕")
                    || s.toString() == ("마라탕") || s.toString().contains("마라탕맛집") || s.toString().contains("마라탕 맛집")
                    || s.toString().contains("마라탕 식당") || s.toString().contains("마라탕식당")
                    || s.toString().contains("마라탕 음식점") || s.toString().contains("마라탕음식점")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsSclsNm == "마라탕/훠궈") {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("건대 초밥") || s.toString().contains("건대초밥")
                    || s.toString() == ("초밥") || s.toString().contains("초밥맛집") || s.toString().contains("초밥 맛집")
                    || s.toString().contains("초밥 식당") || s.toString().contains("초밥식당")
                    || s.toString().contains("초밥 음식점") || s.toString().contains("초밥음식점")
                    || s.toString() == ("회") || s.toString().contains("회맛집") || s.toString().contains("회 맛집")
                    || s.toString().contains("회 식당") || s.toString().contains("회식당")
                    || s.toString().contains("회 음식점") || s.toString().contains("회음식점")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsSclsNm == "일식 회/초밥") {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("건대 파스타") || s.toString().contains("건대파스타")
                    || s.toString() == ("파스타") || s.toString().contains("파스타맛집") || s.toString().contains("파스타 맛집")
                    || s.toString().contains("파스타 식당") || s.toString().contains("파스타식당")
                    || s.toString().contains("파스타 음식점") || s.toString().contains("파스타음식점")
                    || s.toString() == ("스테이크") || s.toString().contains("스테이크맛집") || s.toString().contains("스테이크 맛집")
                    || s.toString().contains("스테이크 식당") || s.toString().contains("스테이크식당")
                    || s.toString().contains("스테이크 음식점") || s.toString().contains("스테이크음식점")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsSclsNm == "파스타/스테이크") {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("중국집")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsSclsNm == ("중국집")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("분식")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsSclsNm == ("김밥/만두/분식")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("카페")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsSclsNm == ("카페")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("족발") || s.toString().contains("보쌈")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsSclsNm == ("족발/보쌈")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("국수") || s.toString().contains("칼국수")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsSclsNm == ("국수/칼국수")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("돼지고기")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsSclsNm == ("돼지고기 구이/찜")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("소고기")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsSclsNm == ("소고기 구이/찜")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("곱창")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsSclsNm == ("곱창 전골/구이")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("오리고기")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsSclsNm == ("닭/오리고기 구이/찜")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("백반") || s.toString().contains("한정식")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsSclsNm == ("백반/한정식")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("샌드위치") || s.toString() == "토스트" || s.toString() == "샐러드"
                        || s.toString().contains("토스트맛집") || s.toString().contains("토스트 맛집")
                        || s.toString().contains("샐러드맛집") || s.toString().contains("샐러드 맛집")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsSclsNm == ("토스트/샌드위치/샐러드")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("건대 떡볶이") || s.toString().contains("건대떡볶이")
                    || s.toString() == ("떡볶이") || s.toString().contains("떡볶이맛집") || s.toString().contains("떡볶이 맛집")
                    || s.toString() == ("만두") || s.toString().contains("만두맛집") || s.toString().contains("만두 맛집")
                    || s.toString().contains("건대 만두") || s.toString().contains("건대만두")
                    || s.toString() == ("김밥") || s.toString().contains("김밥맛집") || s.toString().contains("김밥 맛집")
                    || s.toString().contains("건대 김밥") || s.toString().contains("건대김밥")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsSclsNm == "김밥/만두/분식") {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("화양동")) {
                    for (restaurant in restaurants) {
                        if (restaurant.lnoAdr.contains("화양동")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("자양동")) {
                    for (restaurant in restaurants) {
                        if (restaurant.lnoAdr.contains("자양동")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("구의동")) {
                    for (restaurant in restaurants) {
                        if (restaurant.lnoAdr.contains("구의동")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString().contains("군자동")) {
                    for (restaurant in restaurants) {
                        if (restaurant.lnoAdr.contains("군자동")) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                } else if (s.toString() == "") {
                    initMapMarker(naverMap)
                } else {
                    for (restaurant in restaurants) {
                        if (restaurant.bizesNm.contains(s.toString())) {
                            addMarker(restaurant, naverMap, markerList, uid)
                        }
                    }
                }
            }
        })
    }

    private fun addMarker(
        restaurant: RestaurantList,
        naverMap: NaverMap,
        markerList: MutableList<Marker>,
        uid: Long
    ) {
        lifecycleScope.launch {
            val marker = Marker() // 마커 추가
            marker.iconTintColor = Color.GREEN
            marker.position = LatLng(restaurant.lat, restaurant.lon)
            marker.width = 60 // 마커 가로 크기
            marker.height = 80 // 마커 세로 크기
            marker.map = naverMap

            val restaurantInfoDialog = RestaurantInfoDialog(restaurant, uid)
            val data = arguments
            restaurantInfoDialog.arguments = data

            marker.setOnClickListener {
                focusMapMarker(marker, naverMap, restaurantInfoDialog)
                true
            }

            markerList.add(marker)
        }
    }

    private fun focusMapMarker(
        selectedMarker: Marker,
        naverMap: NaverMap,
        restaurantInfoDialog: RestaurantInfoDialog
    ) {
        // 다른 마커들을 지도에서 감춤
        selectedMarker.iconTintColor = Color.BLUE

        naverMap.moveCamera(
            CameraUpdate.scrollTo(selectedMarker.position)
                .animate(CameraAnimation.Fly, 1000)
                .finishCallback {
                    naverMap.cameraPosition = CameraPosition(
                        naverMap.cameraPosition.target, // 현재 중심 위치를 그대로 유지
                        16.0 // 원하는 줌 레벨으로 설정
                    )

                    // 애니메이션이 끝난 후에 다이얼로그 표시
                    if (markerInfoWindowMap.containsKey(selectedMarker)) {
                        markerInfoWindowMap[selectedMarker]?.dismiss()
                        markerInfoWindowMap.remove(selectedMarker)
                    } else {
                        markerInfoWindowMap[selectedMarker] = restaurantInfoDialog
                        restaurantInfoDialog.show(
                            childFragmentManager,
                            "RestaurantInfoDialog"
                        )
                    }

                    for (marker in markerList) {
                        if (marker != selectedMarker) {
                            marker.iconTintColor = Color.GREEN
                        }
                    }
                }
        )
    }

    private fun initMapMarker(naverMap: NaverMap) {
        val call = restaurantService.getTopRestaurant(authorizationHeader)
        call.enqueue(object : Callback<List<TopRestuarantDto>> {
            override fun onResponse(call: Call<List<TopRestuarantDto>>, response: Response<List<TopRestuarantDto>>) {
                if (response.isSuccessful) {
                    val restaurantListResponse = response.body() // 서버에서 받은 스크랩 목록
                    if (restaurantListResponse != null) {
                        topRestaurants.clear()
                        topRestaurants.addAll(restaurantListResponse) // topRestaurant 목록 저장
                    } else {
                        topRestaurants.clear()
                    }
                    val successCode = response.code()
                    Log.i("TOP 음식점 목록 로드 ", "성공 $successCode")

                    if (topRestaurants.isNotEmpty()) {
                        for (top in topRestaurants) {
                            for (restaurant in restaurants) {
                                if (top.placeId == restaurant.bizesId) {
                                    val marker = Marker() // 마커 추가
                                    marker.iconTintColor = Color.GREEN
                                    marker.position = LatLng(restaurant.lat, restaurant.lon)
                                    marker.width = 60 // 마커 가로 크기
                                    marker.height = 80 // 마커 세로 크기
                                    marker.map = naverMap

                                    val restaurantInfoDialog = RestaurantInfoDialog(restaurant, uid)
                                    val data = arguments
                                    restaurantInfoDialog.arguments = data

                                    marker.setOnClickListener {
                                        focusMapMarker(marker, naverMap, restaurantInfoDialog)
                                        true
                                    }

                                    markerList.add(marker)
                                }
                            }
                        }
                    } else {
                        if (restaurants.isNotEmpty()) {
                            for (i in 0..9) {
                                val marker = Marker() // 마커 추가
                                marker.iconTintColor = Color.GREEN
                                marker.position = LatLng(restaurants[i].lat, restaurants[i].lon)
                                marker.width = 60 // 마커 가로 크기
                                marker.height = 80 // 마커 세로 크기
                                marker.map = naverMap

                                val restaurantInfoDialog = RestaurantInfoDialog(restaurants[i], uid)
                                val data = arguments
                                restaurantInfoDialog.arguments = data

                                marker.setOnClickListener {
                                    focusMapMarker(marker, naverMap, restaurantInfoDialog)
                                    true
                                }

                                markerList.add(marker)
                            }
                        }
                    }
                } else {
                    val errorCode = response.code()
                    Log.i("TOP 음식점 목록 로드 ", "실패 $errorCode")
                }
            }

            override fun onFailure(call: Call<List<TopRestuarantDto>>, t: Throwable) {
                // 네트워크 오류 또는 기타 에러가 발생했을 때의 처리
                t.message?.let { it1 -> Log.i("[TOP 음식점 목록 로드 에러: ]", it1) }
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = view.findViewById(R.id.map_view)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        getUid()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    private fun getUid() {
        val memberService = RetrofitClient.memberService

        val call = accessToken?.let { memberService.selectOne(authorizationHeader) }
        call?.enqueue(object : Callback<Member> {
            override fun onResponse(call: Call<Member>, response: Response<Member>) {
                if (response.isSuccessful) {
                    val member = response.body()
                    val uidInfo = member?.uid
                    if (uidInfo != null) {
                        uid = uidInfo
                    }
                } else {
                    val errorCode = response.code()
                    Log.i("uid 가져오기 ", "실패 $errorCode")
                }
            }

            override fun onFailure(call: Call<Member>, t: Throwable) {
                // 네트워크 오류 또는 기타 에러가 발생했을 때의 처리
                t.message?.let { it1 -> Log.i("[uid 로드 실패: ]", it1) }
            }
        })
    }
}