package us.spur.monocle.sdk

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Monocle API Service definition for Android
 * @param tk Monocle site-token String unique to your deployment. From https://app.spur.us/monocle
 * @param v Monocle version String
 * @param s Monocle script ID String.  This is expected to be a UUID unique to the device or app install (like ANDROID_ID), but must be unique for at least the session.
 * @param t Monocle source String.  This identifies the platform using Monocle for backend analytics.
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
