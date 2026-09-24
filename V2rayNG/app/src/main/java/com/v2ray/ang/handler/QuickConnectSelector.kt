package com.v2ray.ang.handler

/**
 * Pure selection policy kept outside Android components so the most important
 * Quick Connect decision can be unit tested.
 */
object QuickConnectSelector {
    fun select(
        available: List<String>,
        measuredDelays: Map<String, Long>,
        previous: String?,
        health: Map<String, ConnectionHealth> = emptyMap()
    ): String? {
        if (available.isEmpty()) return null
        return available
            .mapNotNull { guid ->
                val delay = measuredDelays[guid] ?: 0L
                val record = health[guid]
                val failurePenalty = (record?.consecutiveFailures ?: 0) * 1_000L
                val reliabilityBonus = ((record?.successes ?: 0) * 8L).coerceAtMost(160L)
                if (delay > 0L) guid to (delay + failurePenalty - reliabilityBonus) else null
            }
            .minByOrNull { it.second }
            ?.first
            ?: previous?.takeIf(available::contains)
            ?: available.first()
    }
}
