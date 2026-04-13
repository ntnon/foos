package no.ks_digital.foos.dto

/**
 * MatchPlayer - A player's Elo snapshot for a specific match
 */
data class MatchPlayer(
    val playerId: Long,
    val playerName: String,
    val initialRating: Double,
    val ratingChange: Double,
    val newRating: Double
)

/**
 * MatchTeam - A team's result in a match, with player Elo snapshots and team stats
 */
data class MatchTeam(
    val teamId: Long,
    val teamColor: String,
    val gameScore: Int,
    val pairWins: Int,
    val offense: MatchPlayer,
    val defense: MatchPlayer,
    val pairStats: TeamStatsResponse
)

/**
 * Match - The rich response DTO for a foosball match.
 */
data class MatchResponse(
    val matchId: Long,
    val matchDate: String,
    val team1: MatchTeam,
    val team2: MatchTeam,
    val winner: String?,
    val winnerColor: String?
)
