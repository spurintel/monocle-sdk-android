package us.spur.monocle.sdk
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Monocle API Client definition for Android
 * @author spur.us
 * @see https://github.com/spur/monocle-sdk-android
 * @param BASE_URL URL to load Monocle
 * @param VERSION Monocle version
 * @param SOURCE Monocle source
 * @param ID Monocle ID
 */
object MonocleClient {
    private const val BASE_URL = "https://mcl.spur.us"
    private const val VERSION = "0.0.20"
    private const val SOURCE = "android"
    private const val ID = "someguidgoeshere"
    private var retrofit: Retrofit? = null
    val apiService: MonocleService
        get() {
            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return retrofit!!.create(MonocleService::class.java)
        }
}
