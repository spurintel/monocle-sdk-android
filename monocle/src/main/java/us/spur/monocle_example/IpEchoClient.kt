package us.spur.monocle

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object IpEchoClient {
    private const val BASE_URL = "https://api.myip.com"
    private var retrofit: Retrofit? = null
    val ipEchoService: IpEchoService
        get() {
            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return retrofit!!.create(IpEchoService::class.java)
        }
}
