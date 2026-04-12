package no.ks_digital.foos.dto

import no.ks_digital.foos.entity.TeamStats

data class PlayerPositionBreakdown(
    val playerId: Long,
    val playerName: String,
    val matchesAsOffense: Int,
    val matchesAsDefense: Int,
    val winsAsOffense: Int,
    val winsAsDefense: Int,
    val offenseWinRate: Double,
    val defenseWinRate: Double
)

data class RivalTeamSummary(
    val rivalTeamStatsId: Long,
    val rival1Id: Long,
    val rival1Name: String,
    val rival2Id: Long,
    val rival2Name: String,
    val matchesAgainst: Int,
    val lossesAgainst: Int,
    val lossRate: Double
)

data class TeamStatsResponse(
    val teamStatsId: Long,
    val player1Id: Long,
    val player1Name: String,
    val player2Id: Long,
    val player2Name: String,
    val totalMatches: Int,
    val wins: Int,
    val losses: Int,
    val draws: Int,
    val winRate: Double,
    val preferredColor: String?,
    val currentWinStreak: Int,
    val longestWinStreak: Int,
    val hotStreak: Boolean,
    val averageScoreDifference: Double,
    val totalPointsScored: Int,
    val totalPointsAllowed: Int,
    val avgPointsScoredPerMatch: Double,
    val avgPointsAllowedPerMatch: Double,
    val matchesAsRed: Int,
    val winsAsRed: Int,
    val matchesAsBlue: Int,
    val winsAsBlue: Int,
    val colorWinRateRed: Double,
    val colorWinRateBlue: Double,
    val avgMatchesPerWeek: Double,
    val lastPlayed: String,
    val eloRating: Double,
    val eloHistory: List<Double>,
    val eloTrend: List<Double>,
    val rivalTeams: List<RivalTeamSummary>,
    val positionBreakdown: List<PlayerPositionBreakdown>
) {
    companion object {
        fun from(
            stats: TeamStats,
            eloHistory: List<Double> = emptyList(),
            rivalTeams: List<RivalTeamSummary> = emptyList(),
            positionBreakdown: List<PlayerPositionBreakdown> = emptyList()
        ): TeamStatsResponse {
            val trend = if (eloHistory.size >= 2)
                eloHistory.takeLast(6).zipWithNext { a, b -> b - a }
            else emptyList()

            return TeamStatsResponse(
                teamStatsId = stats.teamStatsId,
                player1Id = stats.player1.playerId!!,
                player1Name = stats.player1.name,
                player2Id = stats.player2.playerId!!,
                player2Name = stats.player2.name,
                totalMatches = stats.totalMatches,
                wins = stats.wins,
                losses = stats.losses,
                draws = stats.draws,
                winRate = stats.winRate,
                preferredColor = stats.preferredColor,
                currentWinStreak = stats.currentWinStreak,
                longestWinStreak = stats.longestWinStreak,
                hotStreak = stats.currentWinStreak >= 3,
                averageScoreDifference = stats.averageScoreDifference,
                totalPointsScored = stats.totalPointsScored,
                totalPointsAllowed = stats.totalPointsAllowed,
                avgPointsScoredPerMatch = stats.avgPointsScoredPerMatch,
                avgPointsAllowedPerMatch = stats.avgPointsAllowedPerMatch,
                matchesAsRed = stats.matchesAsRed,
                winsAsRed = stats.winsAsRed,
                matchesAsBlue = stats.matchesAsBlue,
                winsAsBlue = stats.winsAsBlue,
                colorWinRateRed = stats.colorWinRateRed,
                colorWinRateBlue = stats.colorWinRateBlue,
                avgMatchesPerWeek = stats.avgMatchesPerWeek,
                lastPlayed = stats.lastPlayed.toString(),
                eloRating = stats.eloRating,
                eloHistory = eloHistory,
                eloTrend = trend,
                rivalTeams = rivalTeams,
                positionBreakdown = positionBreakdown
            )
        }
    }
}

