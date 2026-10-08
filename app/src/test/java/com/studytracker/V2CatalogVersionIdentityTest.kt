package com.studytracker

import com.studytracker.core.data.package_exchange.V2CatalogVersionIdentity
import org.junit.Assert.*
import org.junit.Test

class V2CatalogVersionIdentityTest {
    private val item = "item_mat9_vid_uslu_giris"
    private val old = "https://www.youtube.com/watch?v=FtJE835vtoM"
    private val newer = "https://www.youtube.com/watch?v=mHq3Dz1Kyw4"
    private val stalePayload = """{"provenance":{"fingerprint":"same-old-value"}}"""

    @Test fun changedVideoCreatesNewVersionWithoutChangingItemIdentity() {
        val oldVersion = V2CatalogVersionIdentity.versionId(item, "Üslü Sayılar", old, stalePayload)
        val newVersion = V2CatalogVersionIdentity.versionId(item, "Üslü Sayılar", newer, stalePayload)
        assertNotEquals(oldVersion, newVersion)
        assertTrue(newVersion.startsWith("ver_${item}_c"))
    }

    @Test fun repeatedImportOfSameContentIsIdempotent() {
        val first = V2CatalogVersionIdentity.versionId(item, "Üslü Sayılar", newer, stalePayload)
        val second = V2CatalogVersionIdentity.versionId(item, "Üslü Sayılar", newer, stalePayload)
        assertEquals(first, second)
    }

    @Test fun payloadRevisionCreatesVersionAndOtherItemRemainsIndependent() {
        val base = V2CatalogVersionIdentity.versionId(item, "Üslü Sayılar", newer, stalePayload)
        assertNotEquals(base, V2CatalogVersionIdentity.versionId(item, "Üslü Sayılar", newer, """{"teacher":"İlyas Güneş"}"""))
        assertNotEquals(base, V2CatalogVersionIdentity.versionId("item_mat9_other", "Üslü Sayılar", newer, stalePayload))
    }

    @Test fun fieldBoundariesCannotCollide() {
        assertNotEquals(
            V2CatalogVersionIdentity.versionId("ab", "c", "d", "ef"),
            V2CatalogVersionIdentity.versionId("a", "bc", "d", "ef")
        )
    }
}
