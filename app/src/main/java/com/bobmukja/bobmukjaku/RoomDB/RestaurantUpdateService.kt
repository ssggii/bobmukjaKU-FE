package com.bobmukja.bobmukjaku.RoomDB

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import com.bobmukja.bobmukjaku.Model.RestaurantList
import com.bobmukja.bobmukjaku.Model.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.net.HttpURLConnection
import java.net.URL

class RestaurantUpdateService:Service() {


    private lateinit var restaurantDb:RestaurantDatabase
    private var flag = false
    //private var notificationOnOff = false
    private lateinit var newStdrYm:String

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("countcount", "servicestart")
        if(!flag){
            newStdrYm = intent?.getStringExtra("newStdrYm")?:"000000"
            getRestaurantListFromAPI()
        }
        return START_STICKY
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun getRestaurantListFromAPI() {
        restaurantDb = RestaurantDatabase.getDatabase(baseContext)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        flag = true

        //api로부터 음식점 데이터를 가져온다.

        val indsMclsCdList =
            listOf("I201", "I202", "I203", "I204", "I205", "I206", "I210")
        val dong = listOf("11215710", "11215820", "11215830", "11215840", "11215847", "11215850", "11215860", "11215870", "11215730")


        var progress = 0
        var progressUnit = 100 / (indsMclsCdList.size * dong.size)


        CoroutineScope(Dispatchers.Default).launch {
            for (dongs in dong) {
                for (lists in indsMclsCdList) {
                    progress += progressUnit
                    restaurantApi(lists,dongs)
                    val intent = Intent("com.bobmukja.bobmukjaku.SPLASHACTIVITY")
                    intent.putExtra("state", "downloading")
                    intent.putExtra("progress", progress)
                    sendBroadcast(intent)
                    Log.i("countcount", progress.toString())
                }
            }
            flag = false

            SharedPreferences.putString("stdrYm", newStdrYm)//변경된 기준날짜 저장
            val intent = Intent("com.bobmukja.bobmukjaku.SPLASHACTIVITY")
            intent.putExtra("state", "finish")
            sendBroadcast(intent)
            //stopForeground(Service.STOP_FOREGROUND_REMOVE)
        }
    }

    private suspend fun restaurantApi(categoryList: String, dongList: String) =
        withContext(Dispatchers.IO) {
            val restaurantList = mutableListOf<RestaurantList>()

            val serviceKey = "I%2BMzNcsHcMWL7gORiWo%2BBaZ%2FPl8w4OpluiaN88eg5zIYnjtoQ0pxS6Vpy6OaHBaIf%2BrZf9%2FgjDcrtUBv%2BcuhCw%3D%3D"
            val pageNo = "&pageNo=1"
            val numOfRows = "&numOfRows=300"
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
            var currentRestaurant: RestaurantList? = null

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "item" -> currentRestaurant = RestaurantList("", "", "", "", "", 0.0, 0.0)
                            "bizesId" -> currentRestaurant?.bizesId = parser.nextText()
                            "bizesNm" -> currentRestaurant?.bizesNm = parser.nextText()
                            "indsMclsNm" -> currentRestaurant?.indsMclsNm = parser.nextText()
                            "indsSclsNm" -> currentRestaurant?.indsSclsNm = parser.nextText()
                            "lnoAdr" -> currentRestaurant?.lnoAdr = parser.nextText()
                            "lat" -> currentRestaurant?.lat = parser.nextText().toDouble()
                            "lon" -> currentRestaurant?.lon = parser.nextText().toDouble()
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name == "item" && currentRestaurant != null) {
                            restaurantList += currentRestaurant
                        }
                    }
                }
                eventType = parser.next()
            }
            restaurantDb.restaurantListDao().insertAllRestaurant(*restaurantList.toTypedArray())
        }
}