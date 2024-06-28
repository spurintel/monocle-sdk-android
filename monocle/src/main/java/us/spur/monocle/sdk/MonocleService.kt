package us.spur.monocle.sdk

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Monocle API Service definition for Android
 * @param tk Monocle site-token from https://app.spur.us/monocle
 * @param v Monocle version
 * @param s Monocle script ID
 * @param t Monocle source
 */
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
