package us.spur.monocle
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object MonocleClient {
    private const val BASE_URL = "https://mcl.spur.dev"
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
