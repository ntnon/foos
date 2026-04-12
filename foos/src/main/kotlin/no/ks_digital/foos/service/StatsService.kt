package no.ks_digital.foos.service

import no.ks_digital.foos.dto.LeaderboardResponse
import no.ks_digital.foos.dto.PartnerSummary
import no.ks_digital.foos.dto.PlayerLeaderboard
import no.ks_digital.foos.dto.PlayerPositionBreakdown
import no.ks_digital.foos.dto.PlayerStatsResponse
import no.ks_digital.foos.dto.RivalSummary
import no.ks_digital.foos.dto.RivalTeamSummary
import no.ks_digital.foos.dto.TeamLeaderboard
import no.ks_digital.foos.dto.TeamStatsResponse
import no.ks_digital.foos.entity.MatchEntity
import no.ks_digital.foos.entity.PlayerStats
import no.ks_digital.foos.entity.TeamColor
import no.ks_digital.foos.entity.TeamStats
import no.ks_digital.foos.repository.PlayerMatchRepository
import no.ks_digital.foos.repository.PlayerRatingRepository
import no.ks_digital.foos.repository.PlayerStatsRepository
import no.ks_digital.foos.repository.TeamRatingRepository
import no.ks_digital.foos.repository.TeamStatsRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class StatsService(
    private val teamStatsRepository: TeamStatsRepository,
    private val playerStatsRepository: PlayerStatsRepository,
    private val playerMatchRepository: PlayerMatchRepository,
    private val playerRatingRepository: PlayerRatingRepository,
    private val teamRatingRepository: TeamRatingRepository,
    val eloService: EloService
) {

    fun updateStatsForMatch(
        match: MatchEntity,
        newT1OffenseElo: Double, newT1DefenseElo: Double,
        newT2OffenseElo: Double, newT2DefenseElo: Double,
        newT1TeamElo: Double, newT2TeamElo: Double
    ) {
        updateTeamStats(match.team1.offense.playerId!!, match.team1.defense.playerId!!, match, newT1TeamElo)
        updateTeamStats(match.team2.offense.playerId!!, match.team2.defense.playerId!!, match, newT2TeamElo)
        updatePlayerStats(match.team1.offense.playerId!!, match, newT1OffenseElo)
        updatePlayerStats(match.team1.defense.playerId!!, match, newT1DefenseElo)
        updatePlayerStats(match.team2.offense.playerId!!, match, newT2OffenseElo)
        updatePlayerStats(match.team2.defense.playerId!!, match, newT2DefenseElo)
    }

    private fun updateTeamStats(player1Id: Long, player2Id: Long, match: MatchEntity, newEloRating: Double) {
        val allPlayers = listOf(match.team1.offense, match.team1.defense, match.team2.offense, match.team2.defense)
        val player1 = allPlayers.first { it.playerId == player1Id }
        val player2 = allPlayers.first { it.playerId == player2Id }

        val stats = teamStatsRepository.findByPlayer1PlayerIdAndPlayer2PlayerId(player1Id, player2Id)
            ?: teamStatsRepository.findByPlayer1PlayerIdAndPlayer2PlayerId(player2Id, player1Id)
            ?: teamStatsRepository.saveAndFlush(TeamStats(player1 = player1, player2 = player2))

        val isTeam1 = (match.team1.offense.playerId == player1Id && match.team1.defense.playerId == player2Id) ||
                      (match.team1.offense.playerId == player2Id && match.team1.defense.playerId == player1Id)

        val teamScore     = if (isTeam1) match.team1GameScore else match.team2GameScore
        val opponentScore = if (isTeam1) match.team2GameScore else match.team1GameScore
        val teamColor     = if (isTeam1) match.team1.teamColor else match.team2.teamColor
        val won  = teamScore > opponentScore
        val lost = teamScore < opponentScore
        val drew = teamScore == opponentScore

        val winInc:     Int = if (won) 1 else 0
        val lossInc:    Int = if (lost) 1 else 0
        val drawInc:    Int = if (drew) 1 else 0
        val redInc:     Int = if (teamColor == TeamColor.RED) 1 else 0
        val blueInc:    Int = if (teamColor == TeamColor.BLUE) 1 else 0
        val redWinInc:  Int = if (teamColor == TeamColor.RED && won) 1 else 0
        val blueWinInc: Int = if (teamColor == TeamColor.BLUE && won) 1 else 0
        val streak      = if (won) stats.currentWinStreak + 1 else 0

        teamStatsRepository.saveAndFlush(stats.copy(
            totalMatches = stats.totalMatches + 1,
            wins   = stats.wins + winInc,
            losses = stats.losses + lossInc,
            draws  = stats.draws + drawInc,
            winRate = (stats.wins + winInc).toDouble() / (stats.totalMatches + 1),
            currentWinStreak = streak,
            longestWinStreak = maxOf(stats.longestWinStreak, streak),
            averageScoreDifference = ((stats.averageScoreDifference * stats.totalMatches) + (teamScore - opponentScore)) / (stats.totalMatches + 1),
            totalPointsScored  = stats.totalPointsScored + teamScore,
            totalPointsAllowed = stats.totalPointsAllowed + opponentScore,
            avgPointsScoredPerMatch  = (stats.totalPointsScored + teamScore).toDouble() / (stats.totalMatches + 1),
            avgPointsAllowedPerMatch = (stats.totalPointsAllowed + opponentScore).toDouble() / (stats.totalMatches + 1),
            matchesAsRed  = stats.matchesAsRed + redInc,
            winsAsRed     = stats.winsAsRed + redWinInc,
            matchesAsBlue = stats.matchesAsBlue + blueInc,
            winsAsBlue    = stats.winsAsBlue + blueWinInc,
            colorWinRateRed  = if (stats.matchesAsRed + redInc > 0)
                (stats.winsAsRed + redWinInc).toDouble() / (stats.matchesAsRed + redInc) else 0.0,
            colorWinRateBlue = if (stats.matchesAsBlue + blueInc > 0)
                (stats.winsAsBlue + blueWinInc).toDouble() / (stats.matchesAsBlue + blueInc) else 0.0,
            preferredColor = if (stats.colorWinRateRed > stats.colorWinRateBlue) "RED" else "BLUE",
            eloRating  = newEloRating,
            lastPlayed = LocalDateTime.now(),
            updatedAt  = LocalDateTime.now()
        ))
    }

    private fun updatePlayerStats(playerId: Long, match: MatchEntity, newEloRating: Double) {
        val allPlayers = listOf(match.team1.offense, match.team1.defense, match.team2.offense, match.team2.defense)
        val player = allPlayers.first { it.playerId == playerId }
        val stats = playerStatsRepository.findByPlayerPlayerId(playerId)
            ?: playerStatsRepository.saveAndFlush(PlayerStats(player = player))

        val isOffense     = match.team1.offense.playerId == playerId || match.team2.offense.playerId == playerId
        val isDefense     = match.team1.defense.playerId == playerId || match.team2.defense.playerId == playerId
        val isTeam1       = match.team1.offense.playerId == playerId || match.team1.defense.playerId == playerId
        val teamColor     = if (isTeam1) match.team1.teamColor else match.team2.teamColor
        val teamScore     = if (isTeam1) match.team1GameScore else match.team2GameScore
        val opponentScore = if (isTeam1) match.team2GameScore else match.team1GameScore
        val won  = teamScore > opponentScore
        val lost = teamScore < opponentScore
        val drew = teamScore == opponentScore

        val winInc:     Int = if (won) 1 else 0
        val lossInc:    Int = if (lost) 1 else 0
        val drawInc:    Int = if (drew) 1 else 0
        val offInc:     Int = if (isOffense) 1 else 0
        val defInc:     Int = if (isDefense) 1 else 0
        val offWinInc:  Int = if (isOffense && won) 1 else 0
        val defWinInc:  Int = if (isDefense && won) 1 else 0
        val redInc:     Int = if (teamColor == TeamColor.RED) 1 else 0
        val blueInc:    Int = if (teamColor == TeamColor.BLUE) 1 else 0
        val redWinInc:  Int = if (teamColor == TeamColor.RED && won) 1 else 0
        val blueWinInc: Int = if (teamColor == TeamColor.BLUE && won) 1 else 0
        val streak      = if (won) stats.currentWinStreak + 1 else 0

        playerStatsRepository.saveAndFlush(stats.copy(
            totalMatches = stats.totalMatches + 1,
            wins   = stats.wins + winInc,
            losses = stats.losses + lossInc,
            draws  = stats.draws + drawInc,
            winRate = (stats.wins + winInc).toDouble() / (stats.totalMatches + 1),
            currentWinStreak = streak,
            longestWinStreak = maxOf(stats.longestWinStreak, streak),
            matchesAsOffense = stats.matchesAsOffense + offInc,
            winsAsOffense    = stats.winsAsOffense + offWinInc,
            matchesAsDefense = stats.matchesAsDefense + defInc,
            winsAsDefense    = stats.winsAsDefense + defWinInc,
            positionWinRateOffense = if (stats.matchesAsOffense + offInc > 0)
                (stats.winsAsOffense + offWinInc).toDouble() / (stats.matchesAsOffense + offInc) else 0.0,
            positionWinRateDefense = if (stats.matchesAsDefense + defInc > 0)
                (stats.winsAsDefense + defWinInc).toDouble() / (stats.matchesAsDefense + defInc) else 0.0,
            bestPosition = if (stats.positionWinRateOffense > stats.positionWinRateDefense) "OFFENSE" else "DEFENSE",
            matchesAsRed  = stats.matchesAsRed + redInc,
            winsAsRed     = stats.winsAsRed + redWinInc,
            matchesAsBlue = stats.matchesAsBlue + blueInc,
            winsAsBlue    = stats.winsAsBlue + blueWinInc,
            colorWinRateRed  = if (stats.matchesAsRed + redInc > 0)
                (stats.winsAsRed + redWinInc).toDouble() / (stats.matchesAsRed + redInc) else 0.0,
            colorWinRateBlue = if (stats.matchesAsBlue + blueInc > 0)
                (stats.winsAsBlue + blueWinInc).toDouble() / (stats.matchesAsBlue + blueInc) else 0.0,
            mostWinningColor = if (stats.colorWinRateRed > stats.colorWinRateBlue) "RED" else "BLUE",
            eloRating  = newEloRating,
            lastPlayed = LocalDateTime.now(),
            updatedAt  = LocalDateTime.now()
        ))
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun teamsForPlayer(playerId: Long) = teamStatsRepository.findAllByPlayerId(playerId)

    private fun buildBestPartners(playerId: Long): List<PartnerSummary> =
        teamsForPlayer(playerId)
            .filter { it.totalMatches >= 1 }
            .map { ts ->
                val partner = if (ts.player1.playerId == playerId) ts.player2 else ts.player1
                PartnerSummary(partnerId = partner.playerId!!, partnerName = partner.name,
                    matchesTogether = ts.totalMatches, winsTogether = ts.wins, winRate = ts.winRate)
            }
            .sortedByDescending { it.winRate }.take(3)

    private fun buildWorstEnemies(playerId: Long): List<RivalSummary> {
        val opponentMatches = playerMatchRepository.findOpponentsByPlayerId(playerId)
        data class Rec(val name: String, var matches: Int = 0, var losses: Int = 0)
        val records = mutableMapOf<Long, Rec>()
        for (om in opponentMatches) {
            val rec = records.getOrPut(om.player.playerId!!) { Rec(om.player.name) }
            rec.matches++
            if (om.won) rec.losses++
        }
        return records.entries.filter { it.value.matches >= 1 }
            .sortedByDescending { it.value.losses }.take(3)
            .map { (id, r) -> RivalSummary(rivalId = id, rivalName = r.name,
                matchesAgainst = r.matches, lossesAgainst = r.losses, lossRate = r.losses.toDouble() / r.matches) }
    }

    private fun buildRivalTeams(ts: TeamStats): List<RivalTeamSummary> {
        val myId1 = ts.player1.playerId!!; val myId2 = ts.player2.playerId!!
        return teamStatsRepository.findAll()
            .filter { other ->
                other.teamStatsId != ts.teamStatsId &&
                other.player1.playerId != myId1 && other.player1.playerId != myId2 &&
                other.player2.playerId != myId1 && other.player2.playerId != myId2 && other.wins > 0
            }
            .sortedByDescending { it.winRate }.take(3)
            .map { r -> RivalTeamSummary(rivalTeamStatsId = r.teamStatsId,
                rival1Id = r.player1.playerId!!, rival1Name = r.player1.name,
                rival2Id = r.player2.playerId!!, rival2Name = r.player2.name,
                matchesAgainst = r.totalMatches, lossesAgainst = r.wins, lossRate = r.winRate) }
    }

    private fun buildPositionBreakdown(ts: TeamStats): List<PlayerPositionBreakdown> {
        val p1 = playerStatsRepository.findByPlayerPlayerId(ts.player1.playerId!!)
        val p2 = playerStatsRepository.findByPlayerPlayerId(ts.player2.playerId!!)
        return listOfNotNull(
            p1?.let { PlayerPositionBreakdown(ts.player1.playerId!!, ts.player1.name,
                it.matchesAsOffense, it.matchesAsDefense, it.winsAsOffense, it.winsAsDefense,
                it.positionWinRateOffense, it.positionWinRateDefense) },
            p2?.let { PlayerPositionBreakdown(ts.player2.playerId!!, ts.player2.name,
                it.matchesAsOffense, it.matchesAsDefense, it.winsAsOffense, it.winsAsDefense,
                it.positionWinRateOffense, it.positionWinRateDefense) }
        )
    }

    private fun enrichTeamStats(ts: TeamStats): TeamStatsResponse {
        val history = teamRatingRepository.findAllByPairOrderedByDate(ts.player1.playerId!!, ts.player2.playerId!!)
            .map { it.ratingAfter }
        return TeamStatsResponse.from(ts, eloHistory = history,
            rivalTeams = buildRivalTeams(ts), positionBreakdown = buildPositionBreakdown(ts))
    }

    private fun enrichPlayerStats(stats: PlayerStats): PlayerStatsResponse {
        val history = playerRatingRepository.findAllByPlayerIdOrderedByDate(stats.player.playerId!!)
            .map { it.ratingAfter }
        return PlayerStatsResponse.from(stats, eloHistory = history,
            bestPartners = buildBestPartners(stats.player.playerId!!),
            worstEnemies = buildWorstEnemies(stats.player.playerId!!))
    }

    // ── Public query methods ──────────────────────────────────────────────────

    fun getTeamStatsById(teamStatsId: Long): TeamStatsResponse? =
        teamStatsRepository.findById(teamStatsId).map { enrichTeamStats(it) }.orElse(null)

    fun getAllTeamStats(): List<TeamStatsResponse> =
        teamStatsRepository.findAll().sortedByDescending { it.eloRating }.map { enrichTeamStats(it) }

    fun getTeamStatsPage(search: String?, page: Int, size: Int): Page<TeamStatsResponse> {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "eloRating"))
        val raw = if (!search.isNullOrBlank())
            teamStatsRepository.searchByPlayerName(search, pageable)
        else
            teamStatsRepository.findAllBy(pageable)
        return raw.map { enrichTeamStats(it) }
    }

    fun getAllPlayerStats(): List<PlayerStatsResponse> =
        playerStatsRepository.findAll().sortedByDescending { it.eloRating }.map { enrichPlayerStats(it) }

    fun getPlayerStatsPage(search: String?, page: Int, size: Int): Page<PlayerStatsResponse> {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "eloRating"))
        val raw = if (!search.isNullOrBlank())
            playerStatsRepository.searchByPlayerName(search, pageable)
        else
            playerStatsRepository.findAllBy(pageable)
        return raw.map { enrichPlayerStats(it) }
    }

    fun getTeamStats(player1Id: Long, player2Id: Long): TeamStatsResponse? {
        val stats = teamStatsRepository.findByPlayer1PlayerIdAndPlayer2PlayerId(player1Id, player2Id)
            ?: teamStatsRepository.findByPlayer1PlayerIdAndPlayer2PlayerId(player2Id, player1Id)
        return stats?.let { enrichTeamStats(it) }
    }

    fun getPlayerStats(playerId: Long): PlayerStatsResponse? =
        playerStatsRepository.findByPlayerPlayerId(playerId)?.let { enrichPlayerStats(it) }

    fun getLeaderboard(): LeaderboardResponse {
        val teams   = getAllTeamStats().filter { it.totalMatches >= 1 }
        val players = getAllPlayerStats().filter { it.totalMatches >= 1 }
        return LeaderboardResponse(
            teams = TeamLeaderboard(
                byElo       = teams.sortedByDescending { it.eloRating }.take(10),
                byWinRate   = teams.filter { it.totalMatches >= 3 }.sortedByDescending { it.winRate }.take(10),
                byWins      = teams.sortedByDescending { it.wins }.take(10),
                byWinStreak = teams.sortedByDescending { it.longestWinStreak }.take(10),
                mostActive  = teams.sortedByDescending { it.totalMatches }.take(10)
            ),
            players = PlayerLeaderboard(
                byElo       = players.sortedByDescending { it.eloRating }.take(10),
                byWinRate   = players.filter { it.totalMatches >= 3 }.sortedByDescending { it.winRate }.take(10),
                byWins      = players.sortedByDescending { it.wins }.take(10),
                byWinStreak = players.sortedByDescending { it.longestWinStreak }.take(10),
                mostActive  = players.sortedByDescending { it.totalMatches }.take(10),
                bestOffense = players.filter { it.matchesAsOffense >= 2 }.sortedByDescending { it.positionWinRateOffense }.take(10),
                bestDefense = players.filter { it.matchesAsDefense >= 2 }.sortedByDescending { it.positionWinRateDefense }.take(10)
            )
        )
    }
}
