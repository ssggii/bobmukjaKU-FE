package com.bobmukja.bobmukjaku

import com.bobmukja.bobmukjaku.Model.RestaurantList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.net.HttpURLConnection
import java.net.URL

class RestaurantRepository {
    private val restaurantCache = mutableMapOf<String, List<RestaurantList>>()

    suspend fun getRestaurantList(categoryList: String, dongList: String): List<RestaurantList> {
        val cacheKey = "$categoryList-$dongList"
        if (restaurantCache.containsKey(cacheKey)) {
            return restaurantCache[cacheKey] ?: emptyList()
        }

        val restaurantList = restaurantApi(categoryList, dongList)
        restaurantCache[cacheKey] = restaurantList
        return restaurantList
    }

    private suspend fun restaurantApi(categoryList: String, dongList: String): List<RestaurantList> =
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
        return@withContext restaurantList
    }
}