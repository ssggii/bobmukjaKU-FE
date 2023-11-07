package com.bobmukja.bobmukjaku

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.bobmukja.bobmukjaku.Model.SharedPreferences
import com.google.firebase.messaging.FirebaseMessaging

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            FirebaseMessaging.getInstance().token.addOnSuccessListener {
                SharedPreferences.initSharedPreferences(applicationContext)
                SharedPreferences.remove("registrationKey")
                SharedPreferences.putString("registrationKey", it)
                Log.i("등록키", it)
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }, 2000)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }
}