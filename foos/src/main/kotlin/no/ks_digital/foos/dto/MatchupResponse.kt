package no.ks_digital.foos.dto

import no.ks_digital.foos.entity.Matchup
import no.ks_digital.foos.entity.Player

data class PlayerPairDto(
    val playerA: Player,
    val playerB: Player
)

data class MatchupResponse(
    val matchupId: Long,
    val pair1: PlayerPairDto,
    val pair2: PlayerPairDto,
    val pair1Wins: Int,
    val pair2Wins: Int,
    val draws: Int
) {
    companion object {
        fun from(matchup: Matchup) = MatchupResponse(
            matchupId = matchup.matchupId!!,
            pair1 = PlayerPairDto(matchup.player1a, matchup.player1b),
            pair2 = PlayerPairDto(matchup.player2a, matchup.player2b),
            pair1Wins = matchup.pair1Wins,
            pair2Wins = matchup.pair2Wins,
            draws = matchup.draws
        )
    }
}

