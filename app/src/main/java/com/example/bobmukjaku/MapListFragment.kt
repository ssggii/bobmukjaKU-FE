import android.content.Context
import android.graphics.PointF
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.bobmukjaku.*
import com.example.bobmukjaku.databinding.FragmentMapListBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import kotlinx.coroutines.launch

class MapListFragment : Fragment(), OnMapReadyCallback {
    private lateinit var mapView: MapView
    private val markerInfoWindowMap = mutableMapOf<Marker, DialogFragment>()
    private lateinit var viewModel: MapListViewModel

    lateinit var binding: FragmentMapListBinding
    lateinit var mContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        val repository = RestaurantRepository()
        val viewModelFactory = MapListViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[MapListViewModel::class.java]
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

        lifecycleScope.launch {
            val indsMclsCdList = listOf("I201", "I202", "I203", "I204", "I205", "I206", "I211")

            for (categoryList in indsMclsCdList) {
                viewModel.fetchRestaurantList(categoryList)
                val restaurantList = viewModel.restaurantList.value ?: emptyList()

                val markerList = mutableListOf<Marker>()

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
//                        marker.iconTintColor = ContextCompat.getColor(requireContext(), R.color.kor) // 한식
                            marker.map = naverMap

                            val restaurantInfoDialog = RestaurantInfoDialog(restaurant)
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
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = view.findViewById(R.id.map_view)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
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
}