package com.studytracker.core.data.package_exchange

import java.security.MessageDigest

/**
 * Content-addressed identity for imported V2 items.
 * Progress/attempts stay attached to the stable item ID; a changed video URL
 * must create a new immutable version even when an old provenance fingerprint
 * was accidentally left unchanged.
 */
internal object V2CatalogVersionIdentity {
    fun versionId(
        itemId: String,
        title: String,
        contentUrl: String?,
        payloadJson: String?
    ): String {
        val fields = listOf(itemId, title, contentUrl ?: "", payloadJson ?: "")
        val canonical = fields.joinToString(separator = "") { "${it.length}:$it" }
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(canonical.toByteArray(Charsets.UTF_8))
            .take(12)
            .joinToString("") { "%02x".format(it) }
        return "ver_${itemId}_c$digest"
    }
}
