package com.example.data

import com.example.model.VlcPlayback
import org.junit.Assert.*
import org.junit.Test

class VlcPlaybackStateTest {
    @Test fun stoppedSessionDoesNotEraseTheLastKnownPosition() {
        val last = VlcPlayback("Movie", "media/5", 10_000, 4_000, "file:///drive/movie.mkv")
        assertEquals(last, mergeVlcPlayback(last, last.copy(positionMs = null)))
    }
    @Test fun aDifferentVideoDoesNotInheritThePreviousPosition() {
        val last = VlcPlayback("Movie", "media/5", 10_000, 4_000, "file:///drive/movie.mkv")
        val next = VlcPlayback("Another", "media/6", 20_000)
        assertEquals(next, mergeVlcPlayback(last, next))
    }
    @Test fun aPausedSnapshotKeepsTheFileButDoesNotKeepAnOldPlayingStatus() {
        val last = VlcPlayback("Movie", "media/5", 10_000, 4_000, "file:///drive/movie.mkv", true)
        val paused = last.copy(mediaUri = null, positionMs = null, isPlaying = false)
        assertEquals(last.copy(isPlaying = false), mergeVlcPlayback(last, paused))
    }
}
