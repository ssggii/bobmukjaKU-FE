package com.bobmukja.bobmukjaku.Dto

import okhttp3.Interceptor
import okhttp3.Response

class UserAgentInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val userAgent = "bobmukjaKU/1.0" // 앱의 이름과 버전을 여기에 적절히 넣어줍니다.

        val newRequest = originalRequest.newBuilder()
            .header("User-Agent", userAgent)
            .build()

        return chain.proceed(newRequest)
    }
}