package us.spur.monocle

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface MonocleService {
    @POST("/r/bundle")
    fun getBundle(
        @Query("tk") token: String?,
        @Query("v") version: String?,
        @Query("s") scriptId: String?,
        @Query("t") source: String?,
        @Body monoclePlugins: MonoclePlugins?
    ): Call<BundleResponse?>?
}
