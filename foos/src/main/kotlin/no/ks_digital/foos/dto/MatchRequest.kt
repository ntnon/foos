package no.ks_digital.foos.dto

data class MatchRequest(
    val team1: TeamRequest,
    val team2: TeamRequest,
    val team1GameScore: Int,
    val team2GameScore: Int
)
