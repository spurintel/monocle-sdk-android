package us.spur.monocle

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

sealed class BundlePosterError : Exception() {
    data class ServerError(val statusCode: Int) : BundlePosterError()
    object InvalidResponseData : BundlePosterError()
    data class NetworkFailure(val error: Throwable) : BundlePosterError()
}

class BundlePoster(
    private val v: String,
    private val t: String,
    private val s: String,
    private val tk: String,
    private val client: OkHttpClient = OkHttpClient()
) {

    suspend fun postBundle(jsonBody: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val url = HttpUrl.Builder()
                    .scheme("https")
                    .host("mcl.spur.us")
                    .addPathSegments("r/bundle")
                    .addQueryParameter("v", v)
                    .addQueryParameter("t", t)
                    .addQueryParameter("s", s)
                    .addQueryParameter("tk", tk)
                    .build()

                val requestBody = jsonBody.toRequestBody("text/plain;charset=UTF-8".toMediaType())
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    response.body?.string()?.let { responseString ->
                        response.close()
                        Result.success(responseString)
                    } ?: Result.failure(BundlePosterError.InvalidResponseData)
                } else {
                    response.close()
                    Result.failure(BundlePosterError.ServerError(response.code))
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Result.failure(BundlePosterError.NetworkFailure(e))
            }
        }
    }
}