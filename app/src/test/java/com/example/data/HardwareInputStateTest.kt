package com.example.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HardwareInputStateTest {
    @Test fun observedTvSnapshotDoesNotShowEveryPortConnected() {
        val states = listOf(1, 0, 1, 1) // HDMI 1, HDMI 2, HDMI 3, AV from TV diagnostics
        assertEquals(listOf("No signal / standby", "Connected", "No signal / standby", "No signal / standby"),
            states.map(::hardwareInputStatus))
        assertEquals("State unavailable", hardwareInputStatus(null))
        assertEquals("State unavailable", hardwareInputStatus(99))
        assertEquals("Disconnected", hardwareInputStatus(2))
    }

    @Test fun missingOrReorderedPortsKeepTheirActualNumbers() {
        val ids = listOf(
            "com.droidlogic.tvinput/.services.Hdmi3InputService/HW7",
            "com.droidlogic.tvinput/.services.Hdmi2InputService/HW6"
        )
        assertEquals(listOf(3, 2), ids.map(::hardwareHdmiPort))
        assertEquals(10, hardwareHdmiPort("vendor/.Hdmi10InputService/HW1"))
        assertNull(hardwareHdmiPort("vendor/.AV1InputService/HW1"))
    }
}
