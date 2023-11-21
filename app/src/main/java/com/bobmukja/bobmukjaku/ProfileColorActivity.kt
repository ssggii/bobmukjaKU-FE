package com.bobmukja.bobmukjaku

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.bobmukja.bobmukjaku.Model.RetrofitClient
import com.bobmukja.bobmukjaku.Model.SharedPreferences
import com.bobmukja.bobmukjaku.MyApp.MyApp
import com.bobmukja.bobmukjaku.databinding.ActivityProfileColorBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileColorActivity : AppCompatActivity() {

    lateinit var binding: ActivityProfileColorBinding
    private var selectedButtonId = -1 // 현재 선택된 배경 버튼

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileColorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MyApp.setAppContext(this)
        init()
    }

    private fun colorChoice() {
        val buttonIds = arrayOf(
            R.id.bg1, R.id.bg2, R.id.bg3, R.id.bg4, R.id.bg5, R.id.bg6, R.id.bg7, R.id.bg8, R.id.bg9,
            R.id.bg10, R.id.bg11, R.id.bg12, R.id.bg13, R.id.bg14, R.id.bg15, R.id.bg16, R.id.bg17, R.id.bg18
        )

        for (buttonId in buttonIds) {
            val button = findViewById<ImageButton>(buttonId)
            button.setOnClickListener {
                if (selectedButtonId != buttonId) {
                    setUnselectedBackgrounds()
                    setSelectedBackground(buttonId) // 선택한 버튼 배경 테두리 나타남
                    selectedButtonId = buttonId
                }
            }
        }
    }

    private fun setUnselectedBackgrounds() {
        val buttonIds = arrayOf(
            R.id.bg1, R.id.bg2, R.id.bg3, R.id.bg4, R.id.bg5, R.id.bg6, R.id.bg7, R.id.bg8, R.id.bg9,
            R.id.bg10, R.id.bg11, R.id.bg12, R.id.bg13, R.id.bg14, R.id.bg15, R.id.bg16, R.id.bg17, R.id.bg18
        )

        for (buttonId in buttonIds) {
            val button = findViewById<ImageButton>(buttonId)
            val resourceName = "bg" + resources.getResourceEntryName(buttonId).removePrefix("bg").toInt()
            val resourceId = resources.getIdentifier(resourceName, "drawable", packageName)
            button.setBackgroundResource(resourceId)
        }
    }

    private fun setSelectedBackground(selectedId: Int) {
        val button = findViewById<ImageButton>(selectedId)
        val resourceName = "btn_bg" + resources.getResourceEntryName(selectedId).removePrefix("bg").toInt()
        val colorResName = "bg" + resources.getResourceEntryName(selectedId).removePrefix("bg").toInt()
        val resourceId = resources.getIdentifier(resourceName, "drawable", packageName)
        button.setBackgroundResource(resourceId)

        // 서버에 배경 색상 업데이트
        val memberService = RetrofitClient.memberService
        val accessToken = SharedPreferences.getString("accessToken", "")

        val authorizationHeader = "Bearer $accessToken"

        val requestBody = mapOf("profileColor" to colorResName)

        val call = accessToken?.let {
            memberService.updateMember(
                authorizationHeader,
                requestBody
            )
        }
        call?.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // 성공적으로 업데이트됨
                    Log.i("프로필 배경색 업데이트 ", "성공")
                } else {
                    val errorCode = response.code()
                    Log.i("프로필 배경색 업데이트 ", "실패 $errorCode")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                // 네트워크 오류 처리
                t.message?.let { it1 -> Log.i("프로필 배경색 업데이트 실패. 기타 에러", it1) }
            }
        })

        val colorAttrId = resources.getIdentifier(colorResName, "color", packageName)
        val color = ContextCompat.getColor(this, colorAttrId)
        val drawable = ContextCompat.getDrawable(this, R.drawable.ku_bg)

        drawable?.mutate()?.let {
            val wrappedDrawable = DrawableCompat.wrap(it)
            DrawableCompat.setTint(wrappedDrawable, color)
            binding.profileBG2.background = wrappedDrawable
        }
    }

    private fun init() {
        colorChoice()

        binding.changeButton.setOnClickListener {
            val intent = Intent()
            intent.putExtra("selectedItemId", R.id.forth)
            setResult(RESULT_OK, intent)
            finish()
        }
    }
}