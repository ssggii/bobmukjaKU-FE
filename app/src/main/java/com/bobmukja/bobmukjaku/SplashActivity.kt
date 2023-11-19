package com.bobmukja.bobmukjaku

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.bobmukja.bobmukjaku.Model.SharedPreferences
import com.bobmukja.bobmukjaku.RoomDB.RestaurantUpdateService
import com.bobmukja.bobmukjaku.databinding.ActivitySplashBinding
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.net.HttpURLConnection
import java.net.URL

class SplashActivity : AppCompatActivity() {

    private lateinit var binding:ActivitySplashBinding
    lateinit var stdrYm:String
    lateinit var newStdrYm:String

    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {
        if(doubleBackToExitPressedOnce){
            super.onBackPressed()
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "'뒤로가기'를 한번 더 누르면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show()

        Handler().postDelayed({
            doubleBackToExitPressedOnce = false
        }, 500)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)


        SharedPreferences.initSharedPreferences(applicationContext)
        registerReceiver(receiver, IntentFilter("com.bobmukja.bobmukjaku.SPLASHACTIVITY"))

        CoroutineScope(Dispatchers.Main).launch{

            stdrYm = SharedPreferences.getString("stdrYm", "000000")?:"null"//최근 업데이트 당시, 음식점 정보 기준 날짜
            newStdrYm = getStrDateFromrestaurantApi("I201","11215710")?:"null"//api로 새로 가져온 음식점 정보 기준 날짜(인자로 준 문자열은 더미)

            Log.i("stdrYm", stdrYm)
            Log.i("newStdrYm", newStdrYm)

            if(newStdrYm != stdrYm){//api의 갱신 기준날짜가 변경됐으므로 음식점 데이터를 업데이트
                //getRestaurantListFromAPI()
                binding.progressbar.visibility = View.VISIBLE
                binding.progressbar.max = 1000
                binding.progresstxt.visibility = View.VISIBLE

                val intent = Intent(this@SplashActivity, RestaurantUpdateService::class.java)
                intent.putExtra("newStdrYm", newStdrYm)
                startForegroundService(intent)
            }else{
                Handler(Looper.getMainLooper()).postDelayed({
                    FirebaseMessaging.getInstance().token.addOnSuccessListener {

                        val intent = Intent(baseContext, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }, 0)
            }
        }


        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

    var currentProgress = 0
    private val receiver = object:BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            val state = intent?.getStringExtra("state")
            val progress = intent?.getIntExtra("progress", 0)?:0
            currentProgress = progress
            Log.i("broadcastrcv", "call")

            if(state != null){
                when(state){
                    "finish"->{
                        binding.progressbar.progress = binding.progressbar.max
                        binding.progresstxt.text = "다운로드 완료!"
                        SharedPreferences.putString("stdrYm", newStdrYm)//변경된 기준날짜 저장

                        Handler(Looper.getMainLooper()).postDelayed({

                            val intent = Intent(baseContext, LoginActivity::class.java)
                            startActivity(intent)
                            finish()

                        }, 0)
                    }
                    else->{
                        binding.progressbar.progress = progress?:0
                        binding.progresstxt.text = "음식점 정보를 다운로드중 ${progress/10}%"
                    }
                }
            }
        }

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

    override fun onResume() {
        super.onResume()
        if(intent.getStringExtra("path") == "fromNotification"){
            val intent = Intent(this, RestaurantUpdateService::class.java)
            stopService(intent)
        }
    }
}