package com.example.bobmukjaku

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bobmukjaku.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private val PROFILE_COLOR_REQUEST_CODE = 100 // Request code to identify the ProfileColorActivity
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        startFragment()
    }

    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {
        if(doubleBackToExitPressedOnce){
            super.onBackPressed()
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "'뒤로가기'를 한번 더 누르면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show()

        Handler().postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)
    }

    private fun startFragment() {
        // 하단 탭이 눌렸을 때 화면을 전환하기 위해선 이벤트 처리하기 위해 BottomNavigationView 객체 생성
        var mainBnv = findViewById<BottomNavigationView>(R.id.main_bnv)

        // OnNavigationItemSelectedListener를 통해 탭 아이템 선택 시 이벤트를 처리
        // navi_menu.xml 에서 설정했던 각 아이템들의 id를 통해 알맞은 프래그먼트로 변경하게 한다.
        mainBnv.run()
        {
            setOnNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.first -> {
                        // 다른 프래그먼트 화면으로 이동하는 기능
                        val chatFragment = ChatFragment()
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fl_container, chatFragment).commit()
                    }
                    R.id.second -> {
                        val data = Bundle()

                        data.putLong("roomId", intent.getLongExtra("roomId", -1))
                        data.putString("roomName", intent.getStringExtra("roomName"))
                        data.putString("meetingDate", intent.getStringExtra("meetingDate"))
                        data.putString("startTime", intent.getStringExtra("startTime"))
                        data.putString("endTime", intent.getStringExtra("endTime"))
                        data.putString("kindOfFood", intent.getStringExtra("kindOfFood"))
                        data.putInt("total", intent.getIntExtra("total", -1))
                        data.putInt("currentNum", intent.getIntExtra("currentNum", -1))

                        val mapFragment = MapFragment()
                        mapFragment.arguments = data
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fl_container, mapFragment).commit()
                    }
                    R.id.third -> {
                        val friendFragment = FriendFragment()
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fl_container, friendFragment).commit()
                    }
                    R.id.forth -> {
                        val profileFragment = ProfileFragment()
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fl_container, profileFragment).commit()
                    }

                }
                true
            }
        }
        // 초기화
        val initialSelectedItem = intent.getIntExtra("selectedItemId", R.id.first)
        mainBnv.selectedItemId = initialSelectedItem
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PROFILE_COLOR_REQUEST_CODE && resultCode == RESULT_OK) {
            // Get the selectedItemId from the data and set it as the selected item on BottomNavigationView
            val selectedItemId = data?.getIntExtra("selectedItemId", R.id.forth)
            val mainBnv = findViewById<BottomNavigationView>(R.id.main_bnv)
            mainBnv.selectedItemId = selectedItemId ?: R.id.forth
        } else if (resultCode == RESULT_OK) {
            val selectedItemId = data?.getIntExtra("selectedItemId", R.id.second)
            val mainBnv = findViewById<BottomNavigationView>(R.id.main_bnv)
            mainBnv.selectedItemId = selectedItemId ?: R.id.second
        }
    }
}