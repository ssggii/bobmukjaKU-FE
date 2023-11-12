package com.bobmukja.bobmukjaku

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bobmukja.bobmukjaku.Model.SharedPreferences
import com.bobmukja.bobmukjaku.RoomDB.RestaurantDatabase
import com.bobmukja.bobmukjaku.databinding.ActivitySplashBinding
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

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

            //앱 최초 실행 여부 판단
            //최초 실행시 음식점 API로부터 데이터를 받아 RoomDB에 저장, 이후 앱을 실행 시킬 경우엔 해당 작업 skip
            if(SharedPreferences.getString("firstExecution", "no") == "no"){
                restaurantDb = RestaurantDatabase.getDatabase(baseContext)
                getRestaurantListFromAPI()
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

            var i = 0
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
                        Toast.makeText(this@SplashActivity, "다채워짐", Toast.LENGTH_SHORT).show()
                        progress = binding.progressbar.max
                    }else {
                        progress += progressUnit
                    }
                    binding.progressbar.progress = progress
                    binding.progresstxt.text = "음식점 정보를 다운로드 중 $progress%"
                    Log.i("vvv", "$progress%")

                }
            }
            SharedPreferences.putString("firstExecution","yes")
        }.await()

        binding.progresstxt.text = "다운로드 완료!"
    }

    private fun attachContextToViewModel(context: Context) {
        val repository = RestaurantRepository()
        val viewModelFactory = MapListViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[MapListViewModel::class.java]
    }
}