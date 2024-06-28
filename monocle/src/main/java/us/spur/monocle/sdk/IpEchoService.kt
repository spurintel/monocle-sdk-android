package us.spur.monocle.sdk

import retrofit2.Call
import retrofit2.http.GET

interface IpEchoService {
    @get:GET("/")
    val ip: Call<QueryResponse?>?
}