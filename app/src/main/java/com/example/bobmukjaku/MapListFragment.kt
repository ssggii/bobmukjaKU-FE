import android.graphics.PointF
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import com.example.bobmukjaku.R
import com.example.bobmukjaku.databinding.FragmentMapListBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.InfoWindow

class MapListFragment : Fragment(), OnMapReadyCallback {
    private lateinit var mapView: MapView
    lateinit var binding: FragmentMapListBinding

    data class Restaurant(
        val name: String,
        val category: String,
        val subCategory: String,
        val latitude: Double,
        val longitude: Double
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMapListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = view.findViewById(R.id.map_view)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    override fun onMapReady(naverMap: NaverMap) {

        // 지도 디폴트 위치 고정
        naverMap.moveCamera(com.naver.maps.map.CameraUpdate.scrollTo(LatLng(37.54130009, 127.0701751)).animate(com.naver.maps.map.CameraAnimation.Easing))
        val restaurantList = readRestaurantDataFromTextFile()

//        // 네이버 맵의 가시 영역에 해당하는 좌표 값 계산
//        val visibleRegion = naverMap.projection.toScreenLocation(naverMap.cameraPosition.target)
//        val leftTop = naverMap.projection.fromScreenLocation(
//            PointF(visibleRegion.x - mapView.width / 2, visibleRegion.y - mapView.height / 2)
//        )
//        val rightBottom = naverMap.projection.fromScreenLocation(
//            PointF(visibleRegion.x + mapView.width / 2, visibleRegion.y + mapView.height / 2)
//        )
//
//        // 가시 영역 좌표 값 출력
//        val minx = leftTop.longitude // 서쪽 경도
//        val miny = rightBottom.latitude // 남쪽 위도
//        val maxx = rightBottom.longitude // 동쪽 경도
//        val maxy = leftTop.latitude // 북쪽 위도
//
//        Log.d("MapListFragment", "minx: $minx, miny: $miny, maxx: $maxx, maxy: $maxy")

        for (restaurant in restaurantList) {
//            if (restaurant.latitude in miny..maxy && restaurant.longitude in minx..maxx) {
                val marker = Marker() // 마커 추가
                marker.position = LatLng(restaurant.latitude, restaurant.longitude)
                marker.map = naverMap

                val infoWindow = InfoWindow() // 정보창 추가
                infoWindow.adapter = object : InfoWindow.DefaultTextAdapter(requireContext()) {
                    override fun getText(infoWindow: InfoWindow): CharSequence {
                        return "${restaurant.name}\n${restaurant.category}"
                    }
                }

                marker.setOnClickListener {
                    if (infoWindow.map == null) {
                        infoWindow.open(marker)
                    } else {
                        infoWindow.close()
                    }
                    true
                }
            }
//        }
    }



    private fun readRestaurantDataFromTextFile(): List<MapListFragment.Restaurant> {

        val restaurantList = mutableListOf<MapListFragment.Restaurant>()
        try {
            val inputStream = requireContext().assets.open("info_restaurants.txt")
            inputStream.bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    val fields = line.split("\t")
                    if (fields.size == 5) { // Check if the line contains all the required fields
                        val name = fields[0] //상호명
                        val category = fields[1] //상권업종중분류명
                        val subCategory = fields[2] //상권업종소분류명
                        val latitude = fields[3].toDouble() //위도
                        val longitude = fields[4].toDouble() //경도
                        val restaurant = MapListFragment.Restaurant(
                            name,
                            category,
                            subCategory,
                            latitude,
                            longitude
                        )
                        restaurantList.add(restaurant)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return restaurantList
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