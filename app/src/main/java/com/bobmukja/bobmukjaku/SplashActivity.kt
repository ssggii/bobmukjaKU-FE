package com.bobmukja.bobmukjaku

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bobmukja.bobmukjaku.Model.SharedPreferences
import com.bobmukja.bobmukjaku.RoomDB.RestaurantDatabase
import com.bobmukja.bobmukjaku.databinding.ActivitySplashBinding
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.net.HttpURLConnection
import java.net.URL

class SplashActivity : AppCompatActivity() {

    private lateinit var restaurantDb:RestaurantDatabase
    private lateinit var viewModel: MapListViewModel
    private lateinit var binding:ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        SharedPreferences.initSharedPreferences(this)
        attachContextToViewModel(this)

        CoroutineScope(Dispatchers.Main).launch{

            val stdrYm = SharedPreferences.getString("stdrYm", "null")?:"null"//최근 업데이트 당시, 음식점 정보 기준 날짜
            val newStdrYm = getStrDateFromrestaurantApi("I201","11215710")?:"null"//api로 새로 가져온 음식점 정보 기준 날짜(인자로 준 문자열은 더미)

            if(stdrYm == "null"){ //최초 실행이므로 무조건 음식점 정보 다운로드
                restaurantDb = RestaurantDatabase.getDatabase(baseContext)
                getRestaurantListFromAPI()
                SharedPreferences.putString("stdrYm", newStdrYm)
            }else{//한번이라도 api로 음식점을 업데이트 한 적이 있을 때
                if(newStdrYm != stdrYm){//api에 있는 음식점 데이터가 새로 업데이트 됐으므로, 음식점 데이터를 업데이트 한다.
                    getRestaurantListFromAPI()
                    SharedPreferences.putString("stdrYm", newStdrYm)
                }
            }

            Handler(Looper.getMainLooper()).postDelayed({
                FirebaseMessaging.getInstance().token.addOnSuccessListener {

//                    SharedPreferences.remove("registrationKey")
//                    SharedPreferences.putString("registrationKey", it)
//                    Log.i("등록키", it)
                    val intent = Intent(baseContext, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }, 2000)

        }


        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }



    private suspend fun getRestaurantListFromAPI() {


        //api로부터 음식점 데이터를 가져온다.
        CoroutineScope(Dispatchers.Main).async {
            val indsMclsCdList =
                listOf("I201", "I202", "I203", "I204", "I205") // "I206", "I207"
            val dong = listOf("11215710", "11215820", "11215850", "11215860", "11215870", "11215730")

            var progress = 0
            var progressUnit = binding.progressbar.max / (indsMclsCdList.size * dong.size)
            binding.progressbar.visibility = View.VISIBLE
            binding.progresstxt.visibility = View.VISIBLE

            for (dongs in dong) {
                for (lists in indsMclsCdList) {
                    viewModel.fetchRestaurantList(lists, dongs)
                    val restaurantList = viewModel.restaurantList.value ?: emptyList()
                    CoroutineScope(Dispatchers.IO).async {
                        restaurantDb.restaurantListDao().insertAllRestaurant(*restaurantList.toTypedArray())
                    }
                    if(progress >= 87){
                        //progress바가 거의 다채워지면 그냥 100%로 UI 변환
                        progress = binding.progressbar.max
                    }else {
                        progress += progressUnit
                    }
                    binding.progressbar.progress = progress
                    binding.progresstxt.text = "음식점 정보를 다운로드 중 $progress%"
                    Log.i("vvv", "$progress%")

                }
            }
        }.await()

        binding.progresstxt.text = "다운로드 완료!"
    }

    private fun attachContextToViewModel(context: Context) {
        val repository = RestaurantRepository()
        val viewModelFactory = MapListViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[MapListViewModel::class.java]
    }


    //
    private suspend fun getStrDateFromrestaurantApi(categoryList: String, dongList: String): String? =
        withContext(Dispatchers.IO) {

            val serviceKey = "I%2BMzNcsHcMWL7gORiWo%2BBaZ%2FPl8w4OpluiaN88eg5zIYnjtoQ0pxS6Vpy6OaHBaIf%2BrZf9%2FgjDcrtUBv%2BcuhCw%3D%3D"
            val pageNo = "&pageNo=1"
            val numOfRows = "&numOfRows=1"
            val dong = "&divId=adongCd"
            val key = "&key=$dongList" // 동단위 key(화양동) // (화양동, 자양동, 구의1동, 구의2동, 구의3동, 군자동)
            val indsLclsCd = "&indsLclsCd=I2" // 대분류
            val indsMclsCd = "&indsMclsCd=$categoryList" // 중분류
            val type = "&type=xml"
            var url = "https://apis.data.go.kr/B553077/api/open/sdsc2/storeListInDong?serviceKey=" +
                    "$serviceKey$pageNo$numOfRows$dong$key$indsLclsCd$indsMclsCd$type"

            val apiUrl = URL(url)
            val connection = apiUrl.openConnection() as HttpURLConnection
            val inputStream = connection.inputStream

            // 음식점 데이터 파싱 및 리스트에 저장
            val parser = XmlPullParserFactory.newInstance().newPullParser()
            parser.setInput(inputStream, "UTF-8") // urlStream은 API 호출 결과 스트림

            var eventType = parser.eventType

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if(parser.name == "stdrYm"){
                            return@withContext parser.nextText()
                        }
                    }
                }
                eventType = parser.next()
            }
            return@withContext null
        }
}