package us.spur.monocle

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

sealed class BundlePosterError : Exception() {
    data class ServerError(val statusCode: Int, val body: String?) : BundlePosterError() {
        override val message: String
            get() = "Server error $statusCode: $body"
    }
    object InvalidResponseData : BundlePosterError()
    data class NetworkFailure(val error: Throwable) : BundlePosterError()
}

class BundlePoster(
    private val v: String,
    private val t: String,
    private val s: String,
    private val tk: String,
    private val cpd: String,
    private val client: OkHttpClient = OkHttpClient()
) {

    suspend fun postBundle(jsonBody: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val url = HttpUrl.Builder()
                    .scheme("https")
                    .host("js.mcl.io")
                    .addPathSegments("r/bundle")
                    .addQueryParameter("v", v)
                    .addQueryParameter("t", t)
                    .addQueryParameter("s", s)
                    .addQueryParameter("tk", tk)
                    .addQueryParameter("cpd", cpd)
                    .build()

                Log.d("Monocle", "Posting bundle to: $url")
                Log.d("Monocle", "Bundle body: $jsonBody")

                val requestBody = jsonBody.toRequestBody("text/plain;charset=UTF-8".toMediaType())
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val statusCode = response.code
                val responseBody = response.body?.string()
                
                Log.d("Monocle", "Response code: $statusCode")
                Log.d("Monocle", "Response body: $responseBody")

                if (response.isSuccessful) {
                    if (responseBody != null) {
                        Result.success(responseBody)
                    } else {
                        Result.failure(BundlePosterError.InvalidResponseData)
                    }
                } else {
                    Result.failure(BundlePosterError.ServerError(statusCode, responseBody))
                }
            } catch (e: IOException) {
                Log.e("Monocle", "Network failure during bundle post", e)
                Result.failure(BundlePosterError.NetworkFailure(e))
            } finally {
                // response is already consumed by .string() and closed implicitly if we used use {}, 
                // but here we extracted it manually. 
            }
        }
    }
}
