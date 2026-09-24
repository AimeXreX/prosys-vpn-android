package com.v2ray.ang

import com.v2ray.ang.handler.QuickConnectSelector
import com.v2ray.ang.handler.ConnectionHealth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QuickConnectSelectorTest {
    @Test
    fun `selects lowest positive delay`() {
        assertEquals(
            "fast",
            QuickConnectSelector.select(
                available = listOf("slow", "failed", "fast"),
                measuredDelays = mapOf("slow" to 420L, "failed" to -1L, "fast" to 85L),
                previous = "slow"
            )
        )
    }

    @Test
    fun `keeps previous server when every measurement fails`() {
        assertEquals(
            "previous",
            QuickConnectSelector.select(
                available = listOf("first", "previous"),
                measuredDelays = mapOf("first" to -1L, "previous" to 0L),
                previous = "previous"
            )
        )
    }

    @Test
    fun `returns null without available servers`() {
        assertNull(QuickConnectSelector.select(emptyList(), emptyMap(), null))
    }

    @Test
    fun `avoids a slightly faster route with repeated failures`() {
        assertEquals(
            "stable",
            QuickConnectSelector.select(
                available = listOf("flaky", "stable"),
                measuredDelays = mapOf("flaky" to 70L, "stable" to 130L),
                previous = null,
                health = mapOf(
                    "flaky" to ConnectionHealth("flaky", consecutiveFailures = 2),
                    "stable" to ConnectionHealth("stable", successes = 8)
                )
            )
        )
    }
}
