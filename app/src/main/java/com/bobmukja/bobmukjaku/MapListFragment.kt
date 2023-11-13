import android.content.Context
import android.graphics.PointF
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bobmukja.bobmukjaku.*
import com.bobmukja.bobmukjaku.Model.*
import com.bobmukja.bobmukjaku.RoomDB.RestaurantDatabase
import com.bobmukja.bobmukjaku.databinding.FragmentMapListBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapListFragment : Fragment(), OnMapReadyCallback {
    private lateinit var mapView: MapView
    private val markerInfoWindowMap = mutableMapOf<Marker, DialogFragment>()
    private lateinit var viewModel: MapListViewModel
    private lateinit var restaurantDb: RestaurantDatabase
    private var restaurants = listOf<RestaurantList>()
    private val markerList = mutableListOf<Marker>()

    lateinit var binding: FragmentMapListBinding
    lateinit var mContext: Context

    private val accessToken = SharedPreferences.getString("accessToken", "")
    private val authorizationHeader = "Bearer $accessToken"
    var uid: Long = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        val repository = RestaurantRepository()
        val viewModelFactory = MapListViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[MapListViewModel::class.java]

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
            LatLng(37.54130009, 127.0701751),  // 위치 지정
            16.0 // 줌 레벨
        )
        naverMap.cameraPosition = cameraPosition

//        initFilter()
        initMapMarker(naverMap)

        // default 설정
        var selected = ""
        var category = "I201"
        var dong = "11215710"
//        getFoodList(naverMap, category, dong)

        setupSearchListener(naverMap)

        // 위치 필터
//        binding.hyDong.setOnClickListener {
//            selected = "화양동"
//            dong = "11215710"
//            unselectedColor()
//            selectedColor(binding.hyDong)
//            binding.townBtn.text = selected
//            binding.townFilter.visibility = View.GONE
//            getFoodList(naverMap, category, dong)
//        }
//        binding.jyDong.setOnClickListener {
//            selected = "자양동"
//            dong = "11215820"
//            unselectedColor()
//            selectedColor(binding.hyDong)
//            binding.townBtn.text = selected
//            binding.townFilter.visibility = View.GONE
//            getFoodList(naverMap, category, dong)
//        }
//        binding.GE1Dong.setOnClickListener {
//            selected = "구의1동"
//            dong = "11215850"
//            unselectedColor()
//            selectedColor(binding.hyDong)
//            binding.townBtn.text = selected
//            binding.townFilter.visibility = View.GONE
//            getFoodList(naverMap, category, dong)
//        }
//        binding.GE2Dong.setOnClickListener {
//            selected = "구의2동"
//            dong = "11215860"
//            unselectedColor()
//            selectedColor(binding.hyDong)
//            binding.townBtn.text = selected
//            binding.townFilter.visibility = View.GONE
//            getFoodList(naverMap, category, dong)
//        }
//        binding.GE3Dong.setOnClickListener {
//            selected = "구의3동"
//            dong = "11215870"
//            unselectedColor()
//            selectedColor(binding.hyDong)
//            binding.townBtn.text = selected
//            binding.townFilter.visibility = View.GONE
//            getFoodList(naverMap, category, dong)
//        }
//        binding.GJDong.setOnClickListener {
//            selected = "군자동"
//            dong = "11215730"
//            unselectedColor()
//            selectedColor(binding.hyDong)
//            binding.townBtn.text = selected
//            binding.townFilter.visibility = View.GONE
//            getFoodList(naverMap, category, dong)
//        }
//
//        // 음식 필터
//        binding.KoreaF.setOnClickListener {
//            selected = "한식"
//            category = "I201"
//            unselectedColor()
//            selectedColor(binding.KoreaF)
//            binding.foodBtn.text = selected
//            binding.foodFilter.visibility = View.GONE
//            getFoodList(naverMap, category, dong)
////            updateMarkers(naverMap, category)
//        }
//        binding.JapanF.setOnClickListener {
//            selected = "일식"
//            category = "I203"
//            unselectedColor()
//            selectedColor(binding.JapanF)
//            binding.foodBtn.text = selected
//            binding.foodFilter.visibility = View.GONE
//            getFoodList(naverMap, category, dong)
////            updateMarkers(naverMap, category)
//        }
//        binding.ForeignF.setOnClickListener {
//            selected = "양식"
//            category = "I204"
//            unselectedColor()
//            selectedColor(binding.ForeignF)
//            binding.foodBtn.text = selected
//            binding.foodFilter.visibility = View.GONE
//            getFoodList(naverMap, category, dong)
////            updateMarkers(naverMap, category)
//        }
//        binding.ChinaF.setOnClickListener {
//            selected = "중식"
//            category = "I202"
//            unselectedColor()
//            selectedColor(binding.ChinaF)
//            binding.foodBtn.text = selected
//            binding.foodFilter.visibility = View.GONE
//            getFoodList(naverMap, category, dong)
////            updateMarkers(naverMap, category)
//        }
//        binding.ectF.setOnClickListener {
//            selected = "기타"
//            category = "I205"
//            unselectedColor()
//            selectedColor(binding.ectF)
//            binding.foodBtn.text = selected
//            binding.foodFilter.visibility = View.GONE
//            getFoodList(naverMap, category, dong)
////            updateEtcMarkers(naverMap)
//        }
    }

    private suspend fun initRestaurantList(){
        CoroutineScope(Dispatchers.IO).async {
            restaurantDb = RestaurantDatabase.getDatabase(mContext)
            restaurants = restaurantDb.restaurantListDao().getAllRecord()
            Log.i("finish", restaurants.size.toString())
//            CoroutineScope(Dispatchers.Main).launch {
//            }
        }.await()
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
                            lifecycleScope.launch {
                                val marker = Marker() // 마커 추가
                                marker.position = LatLng(restaurant.lat, restaurant.lon)
                                marker.width = 45 // 마커 가로 크기
                                marker.height = 60 // 마커 세로 크기
                                marker.map = naverMap

                                val restaurantInfoDialog = RestaurantInfoDialog(restaurant, uid)
                                val data = arguments
                                restaurantInfoDialog.arguments = data

                                marker.setOnClickListener {
                                    if (markerInfoWindowMap.containsKey(marker)) {
                                        markerInfoWindowMap[marker]?.dismiss()
                                        markerInfoWindowMap.remove(marker)
                                    } else {
                                        markerInfoWindowMap[marker] = restaurantInfoDialog
                                        restaurantInfoDialog.show(childFragmentManager, "RestaurantInfoDialog")
                                    }
                                    true
                                }

                                markerList.add(marker)
                            }
                        }
                    }
                } else if (s.toString().contains("일식")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsMclsNm.equals("일식")) {
                            lifecycleScope.launch {
                                val marker = Marker() // 마커 추가
                                marker.position = LatLng(restaurant.lat, restaurant.lon)
                                marker.width = 45 // 마커 가로 크기
                                marker.height = 60 // 마커 세로 크기
                                marker.map = naverMap

                                val restaurantInfoDialog = RestaurantInfoDialog(restaurant, uid)
                                val data = arguments
                                restaurantInfoDialog.arguments = data

                                marker.setOnClickListener {
                                    if (markerInfoWindowMap.containsKey(marker)) {
                                        markerInfoWindowMap[marker]?.dismiss()
                                        markerInfoWindowMap.remove(marker)
                                    } else {
                                        markerInfoWindowMap[marker] = restaurantInfoDialog
                                        restaurantInfoDialog.show(
                                            childFragmentManager,
                                            "RestaurantInfoDialog"
                                        )
                                    }
                                    true
                                }

                                markerList.add(marker)
                            }
                        }
                    }
                } else if (s.toString().contains("양식")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsMclsNm.equals("서양식")) {
                            lifecycleScope.launch {
                                val marker = Marker() // 마커 추가
                                marker.position = LatLng(restaurant.lat, restaurant.lon)
                                marker.width = 45 // 마커 가로 크기
                                marker.height = 60 // 마커 세로 크기
                                marker.map = naverMap

                                val restaurantInfoDialog = RestaurantInfoDialog(restaurant, uid)
                                val data = arguments
                                restaurantInfoDialog.arguments = data

                                marker.setOnClickListener {
                                    if (markerInfoWindowMap.containsKey(marker)) {
                                        markerInfoWindowMap[marker]?.dismiss()
                                        markerInfoWindowMap.remove(marker)
                                    } else {
                                        markerInfoWindowMap[marker] = restaurantInfoDialog
                                        restaurantInfoDialog.show(
                                            childFragmentManager,
                                            "RestaurantInfoDialog"
                                        )
                                    }
                                    true
                                }

                                markerList.add(marker)
                            }
                        }
                    }
                } else if (s.toString().contains("중식")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsMclsNm.equals("중식")) {
                            lifecycleScope.launch {
                                val marker = Marker() // 마커 추가
                                marker.position = LatLng(restaurant.lat, restaurant.lon)
                                marker.width = 45 // 마커 가로 크기
                                marker.height = 60 // 마커 세로 크기
                                marker.map = naverMap

                                val restaurantInfoDialog = RestaurantInfoDialog(restaurant, uid)
                                val data = arguments
                                restaurantInfoDialog.arguments = data

                                marker.setOnClickListener {
                                    if (markerInfoWindowMap.containsKey(marker)) {
                                        markerInfoWindowMap[marker]?.dismiss()
                                        markerInfoWindowMap.remove(marker)
                                    } else {
                                        markerInfoWindowMap[marker] = restaurantInfoDialog
                                        restaurantInfoDialog.show(childFragmentManager, "RestaurantInfoDialog")
                                    }
                                    true
                                }

                                markerList.add(marker)
                            }
                        }
                    }
                } else if (s.toString().contains("동남아시아")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsMclsNm.equals("동남아시아")) {
                            lifecycleScope.launch {
                                val marker = Marker() // 마커 추가
                                marker.position = LatLng(restaurant.lat, restaurant.lon)
                                marker.width = 45 // 마커 가로 크기
                                marker.height = 60 // 마커 세로 크기
                                marker.map = naverMap

                                val restaurantInfoDialog = RestaurantInfoDialog(restaurant, uid)
                                val data = arguments
                                restaurantInfoDialog.arguments = data

                                marker.setOnClickListener {
                                    if (markerInfoWindowMap.containsKey(marker)) {
                                        markerInfoWindowMap[marker]?.dismiss()
                                        markerInfoWindowMap.remove(marker)
                                    } else {
                                        markerInfoWindowMap[marker] = restaurantInfoDialog
                                        restaurantInfoDialog.show(
                                            childFragmentManager,
                                            "RestaurantInfoDialog"
                                        )
                                    }
                                    true
                                }

                                markerList.add(marker)
                            }
                        }
                    }
                } else if (s.toString().contains("외국식")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsMclsNm.equals("기타 외국식")) {
                            lifecycleScope.launch {
                                val marker = Marker() // 마커 추가
                                marker.position = LatLng(restaurant.lat, restaurant.lon)
                                marker.width = 45 // 마커 가로 크기
                                marker.height = 60 // 마커 세로 크기
                                marker.map = naverMap

                                val restaurantInfoDialog = RestaurantInfoDialog(restaurant, uid)
                                val data = arguments
                                restaurantInfoDialog.arguments = data

                                marker.setOnClickListener {
                                    if (markerInfoWindowMap.containsKey(marker)) {
                                        markerInfoWindowMap[marker]?.dismiss()
                                        markerInfoWindowMap.remove(marker)
                                    } else {
                                        markerInfoWindowMap[marker] = restaurantInfoDialog
                                        restaurantInfoDialog.show(
                                            childFragmentManager,
                                            "RestaurantInfoDialog"
                                        )
                                    }
                                    true
                                }

                                markerList.add(marker)
                            }
                        }
                    }
                } else if (s.toString().contains("건대 베트남") || s.toString().contains("건대베트남")
                    || s.toString() == "베트남" || s.toString().contains("베트남맛집") || s.toString().contains("베트남 맛집")
                    || s.toString().contains("베트남식당") || s.toString().contains("베트남 식당")
                    || s.toString().contains("베트남음식점") || s.toString().contains("베트남 음식점")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsSclsNm.equals("베트남식 전문")) {
                            lifecycleScope.launch {
                                val marker = Marker() // 마커 추가
                                marker.position = LatLng(restaurant.lat, restaurant.lon)
                                marker.width = 45 // 마커 가로 크기
                                marker.height = 60 // 마커 세로 크기
                                marker.map = naverMap

                                val restaurantInfoDialog = RestaurantInfoDialog(restaurant, uid)
                                val data = arguments
                                restaurantInfoDialog.arguments = data

                                marker.setOnClickListener {
                                    if (markerInfoWindowMap.containsKey(marker)) {
                                        markerInfoWindowMap[marker]?.dismiss()
                                        markerInfoWindowMap.remove(marker)
                                    } else {
                                        markerInfoWindowMap[marker] = restaurantInfoDialog
                                        restaurantInfoDialog.show(
                                            childFragmentManager,
                                            "RestaurantInfoDialog"
                                        )
                                    }
                                    true
                                }

                                markerList.add(marker)
                            }
                        }
                    }
                } else if (s.toString().contains("건대 피자") || s.toString().contains("건대피자")
                    || s.toString() == "피자" || s.toString().contains("피자맛집") || s.toString().contains("피자 맛집")
                    || s.toString().contains("피자식당") || s.toString().contains("피자 식당")
                    || s.toString().contains("피자음식점") || s.toString().contains("피자 음식점")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsSclsNm == ("피자")) {
                            lifecycleScope.launch {
                                val marker = Marker() // 마커 추가
                                marker.position = LatLng(restaurant.lat, restaurant.lon)
                                marker.width = 45 // 마커 가로 크기
                                marker.height = 60 // 마커 세로 크기
                                marker.map = naverMap

                                val restaurantInfoDialog = RestaurantInfoDialog(restaurant, uid)
                                val data = arguments
                                restaurantInfoDialog.arguments = data

                                marker.setOnClickListener {
                                    if (markerInfoWindowMap.containsKey(marker)) {
                                        markerInfoWindowMap[marker]?.dismiss()
                                        markerInfoWindowMap.remove(marker)
                                    } else {
                                        markerInfoWindowMap[marker] = restaurantInfoDialog
                                        restaurantInfoDialog.show(
                                            childFragmentManager,
                                            "RestaurantInfoDialog"
                                        )
                                    }
                                    true
                                }

                                markerList.add(marker)
                            }
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
                            lifecycleScope.launch {
                                val marker = Marker() // 마커 추가
                                marker.position = LatLng(restaurant.lat, restaurant.lon)
                                marker.width = 45 // 마커 가로 크기
                                marker.height = 60 // 마커 세로 크기
                                marker.map = naverMap

                                val restaurantInfoDialog = RestaurantInfoDialog(restaurant, uid)
                                val data = arguments
                                restaurantInfoDialog.arguments = data

                                marker.setOnClickListener {
                                    if (markerInfoWindowMap.containsKey(marker)) {
                                        markerInfoWindowMap[marker]?.dismiss()
                                        markerInfoWindowMap.remove(marker)
                                    } else {
                                        markerInfoWindowMap[marker] = restaurantInfoDialog
                                        restaurantInfoDialog.show(
                                            childFragmentManager,
                                            "RestaurantInfoDialog"
                                        )
                                    }
                                    true
                                }

                                markerList.add(marker)
                            }
                        }
                    }
                } else if (s.toString().contains("치킨")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsSclsNm == ("치킨")) {
                            lifecycleScope.launch {
                                val marker = Marker() // 마커 추가
                                marker.position = LatLng(restaurant.lat, restaurant.lon)
                                marker.width = 45 // 마커 가로 크기
                                marker.height = 60 // 마커 세로 크기
                                marker.map = naverMap

                                val restaurantInfoDialog = RestaurantInfoDialog(restaurant, uid)
                                val data = arguments
                                restaurantInfoDialog.arguments = data

                                marker.setOnClickListener {
                                    if (markerInfoWindowMap.containsKey(marker)) {
                                        markerInfoWindowMap[marker]?.dismiss()
                                        markerInfoWindowMap.remove(marker)
                                    } else {
                                        markerInfoWindowMap[marker] = restaurantInfoDialog
                                        restaurantInfoDialog.show(
                                            childFragmentManager,
                                            "RestaurantInfoDialog"
                                        )
                                    }
                                    true
                                }

                                markerList.add(marker)
                            }
                        }
                    }
                } else if (s.toString().contains("건대 마라탕") || s.toString().contains("건대마라탕")
                    || s.toString() == ("마라탕") || s.toString().contains("마라탕맛집") || s.toString().contains("마라탕 맛집")
                    || s.toString().contains("마라탕 식당") || s.toString().contains("마라탕식당")
                    || s.toString().contains("마라탕 음식점") || s.toString().contains("마라탕음식점")) {
                    for (restaurant in restaurants) {
                        if (restaurant.indsSclsNm == "마라탕/훠궈") {
                            lifecycleScope.launch {
                                val marker = Marker() // 마커 추가
                                marker.position = LatLng(restaurant.lat, restaurant.lon)
                                marker.width = 45 // 마커 가로 크기
                                marker.height = 60 // 마커 세로 크기
                                marker.map = naverMap

                                val restaurantInfoDialog = RestaurantInfoDialog(restaurant, uid)
                                val data = arguments
                                restaurantInfoDialog.arguments = data

                                marker.setOnClickListener {
                                    if (markerInfoWindowMap.containsKey(marker)) {
                                        markerInfoWindowMap[marker]?.dismiss()
                                        markerInfoWindowMap.remove(marker)
                                    } else {
                                        markerInfoWindowMap[marker] = restaurantInfoDialog
                                        restaurantInfoDialog.show(
                                            childFragmentManager,
                                            "RestaurantInfoDialog"
                                        )
                                    }
                                    true
                                }

                                markerList.add(marker)
                            }
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
                            lifecycleScope.launch {
                                val marker = Marker() // 마커 추가
                                marker.position = LatLng(restaurant.lat, restaurant.lon)
                                marker.width = 45 // 마커 가로 크기
                                marker.height = 60 // 마커 세로 크기
                                marker.map = naverMap

                                val restaurantInfoDialog = RestaurantInfoDialog(restaurant, uid)
                                val data = arguments
                                restaurantInfoDialog.arguments = data

                                marker.setOnClickListener {
                                    if (markerInfoWindowMap.containsKey(marker)) {
                                        markerInfoWindowMap[marker]?.dismiss()
                                        markerInfoWindowMap.remove(marker)
                                    } else {
                                        markerInfoWindowMap[marker] = restaurantInfoDialog
                                        restaurantInfoDialog.show(
                                            childFragmentManager,
                                            "RestaurantInfoDialog"
                                        )
                                    }
                                    true
                                }

                                markerList.add(marker)
                            }
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
                            lifecycleScope.launch {
                                val marker = Marker() // 마커 추가
                                marker.position = LatLng(restaurant.lat, restaurant.lon)
                                marker.width = 45 // 마커 가로 크기
                                marker.height = 60 // 마커 세로 크기
                                marker.map = naverMap

                                val restaurantInfoDialog = RestaurantInfoDialog(restaurant, uid)
                                val data = arguments
                                restaurantInfoDialog.arguments = data

                                marker.setOnClickListener {
                                    if (markerInfoWindowMap.containsKey(marker)) {
                                        markerInfoWindowMap[marker]?.dismiss()
                                        markerInfoWindowMap.remove(marker)
                                    } else {
                                        markerInfoWindowMap[marker] = restaurantInfoDialog
                                        restaurantInfoDialog.show(
                                            childFragmentManager,
                                            "RestaurantInfoDialog"
                                        )
                                    }
                                    true
                                }

                                markerList.add(marker)
                            }
                        }
                    }
                } else if (s.toString().contains("화양동")) {
                    for (restaurant in restaurants) {
                        if (restaurant.lnoAdr.contains("화양동")) {
                            lifecycleScope.launch {
                                val marker = Marker() // 마커 추가
                                marker.position = LatLng(restaurant.lat, restaurant.lon)
                                marker.width = 45 // 마커 가로 크기
                                marker.height = 60 // 마커 세로 크기
                                marker.map = naverMap

                                val restaurantInfoDialog = RestaurantInfoDialog(restaurant, uid)
                                val data = arguments
                                restaurantInfoDialog.arguments = data

                                marker.setOnClickListener {
                                    if (markerInfoWindowMap.containsKey(marker)) {
                                        markerInfoWindowMap[marker]?.dismiss()
                                        markerInfoWindowMap.remove(marker)
                                    } else {
                                        markerInfoWindowMap[marker] = restaurantInfoDialog
                                        restaurantInfoDialog.show(
                                            childFragmentManager,
                                            "RestaurantInfoDialog"
                                        )
                                    }
                                    true
                                }

                                markerList.add(marker)
                            }
                        }
                    }
                } else if (s.toString().contains("자양동")) {
                    for (restaurant in restaurants) {
                        if (restaurant.lnoAdr.contains("자양동")) {
                            lifecycleScope.launch {
                                val marker = Marker() // 마커 추가
                                marker.position = LatLng(restaurant.lat, restaurant.lon)
                                marker.width = 45 // 마커 가로 크기
                                marker.height = 60 // 마커 세로 크기
                                marker.map = naverMap

                                val restaurantInfoDialog = RestaurantInfoDialog(restaurant, uid)
                                val data = arguments
                                restaurantInfoDialog.arguments = data

                                marker.setOnClickListener {
                                    if (markerInfoWindowMap.containsKey(marker)) {
                                        markerInfoWindowMap[marker]?.dismiss()
                                        markerInfoWindowMap.remove(marker)
                                    } else {
                                        markerInfoWindowMap[marker] = restaurantInfoDialog
                                        restaurantInfoDialog.show(
                                            childFragmentManager,
                                            "RestaurantInfoDialog"
                                        )
                                    }
                                    true
                                }

                                markerList.add(marker)
                            }
                        }
                    }
                } else if (s.toString().contains("구의동")) {
                    for (restaurant in restaurants) {
                        if (restaurant.lnoAdr.contains("구의동")) {
                            lifecycleScope.launch {
                                val marker = Marker() // 마커 추가
                                marker.position = LatLng(restaurant.lat, restaurant.lon)
                                marker.width = 45 // 마커 가로 크기
                                marker.height = 60 // 마커 세로 크기
                                marker.map = naverMap

                                val restaurantInfoDialog = RestaurantInfoDialog(restaurant, uid)
                                val data = arguments
                                restaurantInfoDialog.arguments = data

                                marker.setOnClickListener {
                                    if (markerInfoWindowMap.containsKey(marker)) {
                                        markerInfoWindowMap[marker]?.dismiss()
                                        markerInfoWindowMap.remove(marker)
                                    } else {
                                        markerInfoWindowMap[marker] = restaurantInfoDialog
                                        restaurantInfoDialog.show(
                                            childFragmentManager,
                                            "RestaurantInfoDialog"
                                        )
                                    }
                                    true
                                }

                                markerList.add(marker)
                            }
                        }
                    }
                } else if (s.toString().contains("군자동")) {
                    for (restaurant in restaurants) {
                        if (restaurant.lnoAdr.contains("군자동")) {
                            lifecycleScope.launch {
                                val marker = Marker() // 마커 추가
                                marker.position = LatLng(restaurant.lat, restaurant.lon)
                                marker.width = 45 // 마커 가로 크기
                                marker.height = 60 // 마커 세로 크기
                                marker.map = naverMap

                                val restaurantInfoDialog = RestaurantInfoDialog(restaurant, uid)
                                val data = arguments
                                restaurantInfoDialog.arguments = data

                                marker.setOnClickListener {
                                    if (markerInfoWindowMap.containsKey(marker)) {
                                        markerInfoWindowMap[marker]?.dismiss()
                                        markerInfoWindowMap.remove(marker)
                                    } else {
                                        markerInfoWindowMap[marker] = restaurantInfoDialog
                                        restaurantInfoDialog.show(
                                            childFragmentManager,
                                            "RestaurantInfoDialog"
                                        )
                                    }
                                    true
                                }

                                markerList.add(marker)
                            }
                        }
                    }
                } else {
                    for (restaurant in restaurants) {
                        if (restaurant.bizesNm.contains(s.toString())) {
                            lifecycleScope.launch {
                                val marker = Marker() // 마커 추가
                                marker.position = LatLng(restaurant.lat, restaurant.lon)
                                marker.width = 45 // 마커 가로 크기
                                marker.height = 60 // 마커 세로 크기
                                marker.map = naverMap

                                val restaurantInfoDialog = RestaurantInfoDialog(restaurant, uid)
                                val data = arguments
                                restaurantInfoDialog.arguments = data

                                marker.setOnClickListener {
                                    if (markerInfoWindowMap.containsKey(marker)) {
                                        markerInfoWindowMap[marker]?.dismiss()
                                        markerInfoWindowMap.remove(marker)
                                    } else {
                                        markerInfoWindowMap[marker] = restaurantInfoDialog
                                        restaurantInfoDialog.show(childFragmentManager, "RestaurantInfoDialog")
                                    }
                                    true
                                }

                                markerList.add(marker)
                            }
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // 사용하지 않음
            }
        })
    }

    private fun initMapMarker(naverMap: NaverMap) {
        for (restaurant in restaurants) {
            lifecycleScope.launch {
                val marker = Marker() // 마커 추가
                marker.position = LatLng(restaurant.lat, restaurant.lon)
                marker.width = 45 // 마커 가로 크기
                marker.height = 60 // 마커 세로 크기
                marker.map = naverMap

                val restaurantInfoDialog = RestaurantInfoDialog(restaurant, uid)
                val data = arguments
                restaurantInfoDialog.arguments = data

                marker.setOnClickListener {
                    if (markerInfoWindowMap.containsKey(marker)) {
                        markerInfoWindowMap[marker]?.dismiss()
                        markerInfoWindowMap.remove(marker)
                    } else {
                        markerInfoWindowMap[marker] = restaurantInfoDialog
                        restaurantInfoDialog.show(childFragmentManager, "RestaurantInfoDialog")
                    }
                    true
                }

                markerList.add(marker)
            }
        }
    }

    private fun getFoodList(naverMap: NaverMap, category: String, dong: String) {
        lifecycleScope.launch {
//            val dong = listOf("11215710", "11215820", "11215850", "11215860", "11215870", "11215730") // 동단위 key(화양동, 자양동, 구의1동, 구의2동, 구의3동, 군자동)
            val indsMclsCdList = listOf("I201", "I202", "I203", "I204", "I205", "I206", "I211")

            viewModel.fetchRestaurantList(category, dong)
            val restaurantList = viewModel.restaurantList.value ?: emptyList()

            val onCameraIdleListener = NaverMap.OnCameraIdleListener {
                // 네이버 맵의 가시 영역에 해당하는 좌표 값 계산
                val visibleRegion = naverMap.projection.toScreenLocation(naverMap.cameraPosition.target)
                val leftTop = naverMap.projection.fromScreenLocation(
                    PointF(visibleRegion.x - mapView.width / 2, visibleRegion.y - mapView.height / 2)
                )
                val rightBottom = naverMap.projection.fromScreenLocation(
                    PointF(visibleRegion.x + mapView.width / 2, visibleRegion.y + mapView.height / 2)
                )

                // 가시 영역 좌표 값 출력
                val minx = leftTop.longitude // 서쪽 경도
                val miny = rightBottom.latitude // 남쪽 위도
                val maxx = rightBottom.longitude // 동쪽 경도
                val maxy = leftTop.latitude // 북쪽 위도

                Log.d("MapListFragment", "minx: $minx, miny: $miny, maxx: $maxx, maxy: $maxy")

                // 이전에 표시된 마커들 삭제
                markerList.forEach { it.map = null }
                markerList.clear()

                for (restaurant in restaurantList) {
                    if (restaurant.lat in miny..maxy && restaurant.lon in minx..maxx) {
                        val marker = Marker() // 마커 추가
                        marker.position = LatLng(restaurant.lat, restaurant.lon)
                        marker.width = 45 // 마커 가로 크기
                        marker.height = 60 // 마커 세로 크기
                        marker.map = naverMap

                        val restaurantInfoDialog = RestaurantInfoDialog(restaurant, uid)
                        val data = arguments
                        restaurantInfoDialog.arguments = data

                        marker.setOnClickListener {
                            if (markerInfoWindowMap.containsKey(marker)) {
                                markerInfoWindowMap[marker]?.dismiss()
                                markerInfoWindowMap.remove(marker)
                            } else {
                                markerInfoWindowMap[marker] = restaurantInfoDialog
                                restaurantInfoDialog.show(childFragmentManager, "RestaurantInfoDialog")
                            }
                            true
                        }

                        markerList.add(marker)
                    }
                }
            }
            naverMap.addOnCameraIdleListener(onCameraIdleListener)
        }
    }

//    private fun initFilter() {
//        // 위치 필터
//        binding.townBtn.setOnClickListener {
//            if (binding.townFilter.visibility == View.GONE) {
//                binding.townFilter.visibility = View.VISIBLE
//                binding.foodFilter.visibility = View.GONE
//            } else {
//                binding.townFilter.visibility = View.GONE
//            }
//        }
//
//        // 음식 필터
//        binding.foodBtn.setOnClickListener {
//            if (binding.foodFilter.visibility == View.GONE) {
//                binding.foodFilter.visibility = View.VISIBLE
//                binding.townFilter.visibility = View.GONE
//            } else {
//                binding.foodFilter.visibility = View.GONE
//            }
//        }
//    }

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

    private fun selectedColor(selectedButton: AppCompatButton) {
        val textColor = ContextCompat.getColor(requireContext(), R.color.white)
        val color = ContextCompat.getColor(requireContext(), R.color.darkGray)
        selectedButton.setTextColor(textColor)
        selectedButton.background.setColorFilter(color, PorterDuff.Mode.SRC_IN)
    }

//    private fun unselectedColor() {
//        val foodList = listOf(
//            binding.KoreaF,
//            binding.JapanF,
//            binding.ForeignF,
//            binding.ChinaF,
//            binding.ectF
//        )
//        val dongList = listOf(
//            binding.hyDong,
//            binding.jyDong,
//            binding.GE1Dong,
//            binding.GE2Dong,
//            binding.GE3Dong,
//            binding.GJDong
//        )
//
//        val textColor = ContextCompat.getColor(requireContext(), R.color.white)
//        val originalTextColor = ContextCompat.getColor(requireContext(), R.color.black)
//        val color = ContextCompat.getColor(requireContext(), R.color.gray)
//
//        for (food in foodList) {
//            if (food.currentTextColor == textColor) {
//                food.setTextColor(originalTextColor)
//                food.background.setColorFilter(color, PorterDuff.Mode.SRC_IN)
//            }
//        }
//        for (dong in dongList) {
//            if (dong.currentTextColor == textColor) {
//                dong.setTextColor(originalTextColor)
//                dong.background.setColorFilter(color, PorterDuff.Mode.SRC_IN)
//            }
//        }
//
//    }
}