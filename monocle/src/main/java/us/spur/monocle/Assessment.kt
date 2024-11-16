package us.spur.monocle

import kotlinx.serialization.Serializable

@Serializable
data class AssessmentResponse(
    val data: String,  // This could be the data returned from the backend
    val status: String  // Example status field
)

@Serializable
data class AssessmentResult(
    val vpn: Boolean,
    val proxied: Boolean,
    val anon: Boolean,
    val rdp: Boolean,
    val dch: Boolean,
    val cc: String,
    val ip: String,
    val ipv6: String? = null,
    val ts: String,
    val complete: Boolean,
    val id: String,
    val sid: String
)

