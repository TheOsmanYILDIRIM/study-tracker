package com.studytracker

import com.studytracker.core.ui.components.youtubeThumbnailUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LearningItemVisualTest {

    @Test
    fun watchUrlResolvesThumbnail() {
        assertEquals(
            "https://i.ytimg.com/vi/abcDEF12345/hqdefault.jpg",
            youtubeThumbnailUrl("https://www.youtube.com/watch?v=abcDEF12345")
        )
    }

    @Test
    fun shortUrlResolvesThumbnail() {
        assertEquals(
            "https://i.ytimg.com/vi/abcDEF12345/hqdefault.jpg",
            youtubeThumbnailUrl("https://youtu.be/abcDEF12345?t=42")
        )
    }

    @Test
    fun shortsUrlResolvesThumbnail() {
        assertEquals(
            "https://i.ytimg.com/vi/abcDEF12345/hqdefault.jpg",
            youtubeThumbnailUrl("https://www.youtube.com/shorts/abcDEF12345")
        )
    }

    @Test
    fun nonYoutubeUrlHasNoThumbnail() {
        assertNull(youtubeThumbnailUrl("https://example.com/video.mp4"))
        assertNull(youtubeThumbnailUrl(null))
    }
}
