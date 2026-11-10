package com.example.data

import com.example.model.HeroSource
import org.junit.Assert.assertEquals
import org.junit.Test

class HeroSourceStateTest {
    private val movie = HeroSource("app", "com.movieboxpro.androidtv", "MovieBoxPro")
    private val hdmi = HeroSource("input", "input_hdmi2", "HDMI 2")
    private val usb = HeroSource("media", "content://drive/movie", "Movie.mp4", "video/mp4")

    @Test fun refreshingInputsKeepsTheSelectedPreviewWithoutReplacingHistory() {
        assertEquals(hdmi, resolveHeroSource(hdmi, true, null, movie, null))
        // A fresh process has no draft: only the actually used source is restored.
        assertEquals(movie, resolveHeroSource(null, false, null, movie, null))
    }

    @Test fun anOldPreferenceReloadCannotOverwriteANewSuccessfulLaunch() {
        assertEquals(usb, resolveHeroSource(usb, false, usb, movie, null))
    }

    @Test fun restartRestoresTheActualInputOrFileRatherThanTheFeaturedApp() {
        assertEquals(hdmi, resolveHeroSource(null, false, null, hdmi, movie))
        assertEquals(usb, resolveHeroSource(null, false, null, usb, movie))
        assertEquals(movie, resolveHeroSource(null, false, null, null, movie))
    }
}
