//package us.spur.monocle
//
//import com.google.gson.Gson
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import java.io.IOException
//
//object MonocleClient2 {
//    private const val BASE_URL = "https://mcl.spur.dev"
//    private val client = OkHttpClient()
//    private val gson = Gson()
//
//    fun <T> fetch(path: String, responseType: Class<T>): T? {
//        val request = Request.Builder()
//            .url("$BASE_URL/$path")
//            .header("X-Version", "0.0.20")
//            .header("X-Source", "android")
//            .header("X-ID", "someguidgoeshere")
//            .build()
//
//        client.newCall(request).execute().use { response ->
//            if (!response.isSuccessful) throw IOException("Unexpected code $response")
//
//            val body = response.body?.string()
//            return if (body != null) gson.fromJson(body, responseType) else null
//        }
//    }
//}
