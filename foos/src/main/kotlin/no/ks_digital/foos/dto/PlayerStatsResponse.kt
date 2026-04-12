package no.ks_digital.foos.dto

import no.ks_digital.foos.entity.PlayerStats

data class PartnerSummary(
    val partnerId: Long,
    val partnerName: String,
    val matchesTogether: Int,
    val winsTogether: Int,
    val winRate: Double
)

data class RivalSummary(
    val rivalId: Long,
    val rivalName: String,
    val matchesAgainst: Int,
    val lossesAgainst: Int,
    val lossRate: Double
)

data class PlayerStatsResponse(
    val playerStatsId: Long,
    val playerId: Long,
    val playerName: String,
    val totalMatches: Int,
    val wins: Int,
    val losses: Int,
    val draws: Int,
    val winRate: Double,
    val bestPosition: String?,
    val mostWinningColor: String?,
    val currentWinStreak: Int,
    val longestWinStreak: Int,
    val hotStreak: Boolean,           // currently on a streak of 3+
    val matchesAsOffense: Int,
    val winsAsOffense: Int,
    val matchesAsDefense: Int,
    val winsAsDefense: Int,
    val positionWinRateOffense: Double,
    val positionWinRateDefense: Double,
    val matchesAsRed: Int,
    val winsAsRed: Int,
    val matchesAsBlue: Int,
    val winsAsBlue: Int,
    val colorWinRateRed: Double,
    val colorWinRateBlue: Double,
    val lastPlayed: String,
    val eloRating: Double,
    val eloHistory: List<Double>,
    val eloTrend: List<Double>,       // last 5 Elo deltas
    // Computed relational fields (populated by StatsService)
    val bestPartners: List<PartnerSummary>,
    val worstEnemies: List<RivalSummary>,  // top 3 players they lose against most
) {
    companion object {
        fun from(
            stats: PlayerStats,
            eloHistory: List<Double> = emptyList(),
            bestPartners: List<PartnerSummary> = emptyList(),
            worstEnemies: List<RivalSummary> = emptyList()
        ): PlayerStatsResponse {
            val trend = if (eloHistory.size >= 2)
                eloHistory.takeLast(6).zipWithNext { a, b -> b - a }
            else emptyList()

            return PlayerStatsResponse(
                playerStatsId = stats.playerStatsId,
                playerId = stats.player.playerId!!,
                playerName = stats.player.name,
                totalMatches = stats.totalMatches,
                wins = stats.wins,
                losses = stats.losses,
                draws = stats.draws,
                winRate = stats.winRate,
                bestPosition = stats.bestPosition,
                mostWinningColor = stats.mostWinningColor,
                currentWinStreak = stats.currentWinStreak,
                longestWinStreak = stats.longestWinStreak,
                hotStreak = stats.currentWinStreak >= 3,
                matchesAsOffense = stats.matchesAsOffense,
                winsAsOffense = stats.winsAsOffense,
                matchesAsDefense = stats.matchesAsDefense,
                winsAsDefense = stats.winsAsDefense,
                positionWinRateOffense = stats.positionWinRateOffense,
                positionWinRateDefense = stats.positionWinRateDefense,
                matchesAsRed = stats.matchesAsRed,
                winsAsRed = stats.winsAsRed,
                matchesAsBlue = stats.matchesAsBlue,
                winsAsBlue = stats.winsAsBlue,
                colorWinRateRed = stats.colorWinRateRed,
                colorWinRateBlue = stats.colorWinRateBlue,
                lastPlayed = stats.lastPlayed.toString(),
                eloRating = stats.eloRating,
                eloHistory = eloHistory,
                eloTrend = trend,
                bestPartners = bestPartners,
                worstEnemies = worstEnemies
            )
        }
    }
}
