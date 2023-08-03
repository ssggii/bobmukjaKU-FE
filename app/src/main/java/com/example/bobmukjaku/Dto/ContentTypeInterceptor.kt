package com.example.bobmukjaku.Dto

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class ContentTypeInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain) : Response = with(chain) {
        val newRequest = request().newBuilder()
            .addHeader("Content-Type", "application/json")
            .build()
        proceed(newRequest)
    }
}