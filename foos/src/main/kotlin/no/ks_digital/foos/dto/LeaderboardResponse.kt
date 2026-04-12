package no.ks_digital.foos.dto

data class LeaderboardResponse(
    val teams: TeamLeaderboard,
    val players: PlayerLeaderboard
)

data class TeamLeaderboard(
    val byElo: List<TeamStatsResponse>,
    val byWinRate: List<TeamStatsResponse>,
    val byWins: List<TeamStatsResponse>,
    val byWinStreak: List<TeamStatsResponse>,
    val mostActive: List<TeamStatsResponse>
)

data class PlayerLeaderboard(
    val byElo: List<PlayerStatsResponse>,
    val byWinRate: List<PlayerStatsResponse>,
    val byWins: List<PlayerStatsResponse>,
    val byWinStreak: List<PlayerStatsResponse>,
    val mostActive: List<PlayerStatsResponse>,
    val bestOffense: List<PlayerStatsResponse>,
    val bestDefense: List<PlayerStatsResponse>
)
