package com.bobmukja.bobmukjaku.Dto

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.bobmukja.bobmukjaku.LoginActivity
import com.bobmukja.bobmukjaku.Model.SharedPreferences
import com.bobmukja.bobmukjaku.MyApp.MyApp
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class ContentTypeInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain) : Response = with(chain) {

        //최초 요청
        val newRequestBuilder = request().newBuilder()
            .addHeader("Content-Type", "application/json")

        val response = proceed(newRequestBuilder.build())
        Log.i("codecode", response.code().toString())

        if(response.code() != 403){
            //accessToken이 만료되지 않은 경우, 응답 그대로 pass
            return response
        }

        //accessToken이 만료됐으니 refreshToken을 헤더에 담아서 보내본다.
        val refreshToken = SharedPreferences.getString("refreshToken", "")?:""
        Log.i("newAT", "RT : $refreshToken")

        if(refreshToken != ""){
            //앱에 refreshToken이 저장돼있으니, refreshToken을 헤더에 담아 재요청
            val requestForRT = chain.request().newBuilder()
                .removeHeader("Authorization")
                .addHeader("Authorization-refresh", "Bearer $refreshToken")
                //.method(chain.request().method(), null)
                .build()


            val responseForRT = chain.proceed(requestForRT)

            if(responseForRT.code() == 403){
                //refreshToken도 만료
                Log.i("newAT", "refreshToken도 만료")

                val handler = Handler(Looper.getMainLooper())

                handler.post(Runnable {

                    val context = MyApp.getAppContext()
                    val intent = Intent(context, LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    context?.startActivity(intent)
                })
                response
            }else{
                //refreshToken이 유효하여 accessToken을 새로 발급받았다. API로 재요청

                val newAccessToken = responseForRT.header("Authorization","")?:""
                SharedPreferences.putString("accessToken", newAccessToken)
                Log.i("newAT", "새로운 AT : $newAccessToken")
                val reRequest = newRequestBuilder
                    .removeHeader("Authorization")
                    .header("Authorization", "Bearer $newAccessToken")
                    .build()
                chain.proceed(reRequest)
            }
        }else{
            //앱에 refreshToken이 저장되어있지 않다.
            //이때도 refreshToken이 만료된 경우와 동일하게 403응답코드 반환하도록 처리
            //-> 앞에서 accessToken이 만료돼서 응답코드 403이므로 그대로 application에 넘겨주면 된다.
            response
        }
    }
}