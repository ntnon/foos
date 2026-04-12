package no.ks_digital.foos.service

import no.ks_digital.foos.dto.Match
import no.ks_digital.foos.dto.MatchPlayer
import no.ks_digital.foos.dto.MatchRequest
import no.ks_digital.foos.dto.MatchTeam
import no.ks_digital.foos.dto.TeamStatsResponse
import no.ks_digital.foos.dto.TeamRequest
import no.ks_digital.foos.entity.MatchEntity
import no.ks_digital.foos.entity.Matchup
import no.ks_digital.foos.entity.PlayerMatch
import no.ks_digital.foos.entity.PlayerRating
import no.ks_digital.foos.entity.Position
import no.ks_digital.foos.entity.Team
import no.ks_digital.foos.entity.TeamColor
import no.ks_digital.foos.entity.TeamMatch
import no.ks_digital.foos.entity.TeamRating
import no.ks_digital.foos.repository.MatchRepository
import no.ks_digital.foos.repository.PlayerMatchRepository
import no.ks_digital.foos.repository.TeamStatsRepository
import no.ks_digital.foos.repository.PlayerRatingRepository
import no.ks_digital.foos.repository.PlayerRepository
import no.ks_digital.foos.repository.PlayerStatsRepository
import no.ks_digital.foos.repository.TeamMatchRepository
import no.ks_digital.foos.repository.TeamRatingRepository
import no.ks_digital.foos.repository.TeamRepository
import no.ks_digital.foos.repository.MatchupRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.*

@Service
@Transactional
class MatchService(
    private val matchRepository: MatchRepository,
    private val teamRepository: TeamRepository,
    private val matchupRepository: MatchupRepository,
    private val playerRepository: PlayerRepository,
    private val playerStatsRepository: PlayerStatsRepository,
    private val teamStatsRepository: TeamStatsRepository,
    private val playerMatchRepository: PlayerMatchRepository,
    private val teamMatchRepository: TeamMatchRepository,
    private val playerRatingRepository: PlayerRatingRepository,
    private val teamRatingRepository: TeamRatingRepository,
    private val statsService: StatsService
) {

    fun getRecentMatches(limit: Int): List<MatchEntity> {
        return matchRepository.findAll()
            .sortedByDescending { it.matchDate }
            .take(limit)
    }

    fun getRecentMatchResults(limit: Int): List<Match> {
        val matches = getRecentMatches(limit)
        return matches.map { match -> toMatch(match) }
    }

    fun getMatchesByPlayer(playerId: Long, limit: Int): List<Match> {
        return matchRepository.findAll()
            .filter { m ->
                m.team1.offense.playerId == playerId || m.team1.defense.playerId == playerId ||
                m.team2.offense.playerId == playerId || m.team2.defense.playerId == playerId
            }
            .sortedByDescending { it.matchDate }
            .take(limit)
            .map { toMatch(it) }
    }

    fun getMatchesByTeamStats(player1Id: Long, player2Id: Long, limit: Int): List<Match> {
        return matchRepository.findAll()
            .filter { m ->
                val t1ids = setOf(m.team1.offense.playerId, m.team1.defense.playerId)
                val t2ids = setOf(m.team2.offense.playerId, m.team2.defense.playerId)
                val pair = setOf(player1Id, player2Id)
                t1ids == pair || t2ids == pair
            }
            .sortedByDescending { it.matchDate }
            .take(limit)
            .map { toMatch(it) }
    }

    fun getMatch(id: Long): Match {
        val match = matchRepository.findById(id)
            .orElseThrow { NoSuchElementException("Match not found: $id") }
        return toMatch(match)
    }

    private fun toMatch(match: MatchEntity): Match {
        val team1PlayerIds = listOf(match.team1.offense.playerId!!, match.team1.defense.playerId!!).sorted()
        val team2PlayerIds = listOf(match.team2.offense.playerId!!, match.team2.defense.playerId!!).sorted()

        val p1a = team1PlayerIds[0]; val p1b = team1PlayerIds[1]
        val p2a = team2PlayerIds[0]; val p2b = team2PlayerIds[1]

        val matchup = matchupRepository.findMatchup(p1a, p1b, p2a, p2b)
        val team1Wins: Int; val team2Wins: Int
        if (matchup.isPresent) {
            val m = matchup.get()
            val team1IsPair1 = m.player1a.playerId == p1a && m.player1b.playerId == p1b
            team1Wins = if (team1IsPair1) m.pair1Wins else m.pair2Wins
            team2Wins = if (team1IsPair1) m.pair2Wins else m.pair1Wins
        } else { team1Wins = 0; team2Wins = 0 }

        // Read Elo snapshots from PlayerRating (joined via PlayerMatch)
        fun eloForPlayer(playerId: Long): PlayerRating? {
            val pm = playerMatchRepository.findByMatchMatchId(match.matchId!!)
                .firstOrNull { it.player.playerId == playerId }
            return pm?.let { playerRatingRepository.findByPlayerMatchPlayerMatchId(it.playerMatchId!!) }
        }

        fun matchPlayer(playerId: Long, name: String): MatchPlayer {
            val rating = eloForPlayer(playerId)
            return MatchPlayer(
                playerId = playerId,
                playerName = name,
                initialRating = rating?.ratingBefore ?: 1600.0,
                ratingChange = rating?.ratingChange ?: 0.0,
                newRating = rating?.ratingAfter ?: 1600.0
            )
        }

        val team1PairStats = (teamStatsRepository.findByPlayer1PlayerIdAndPlayer2PlayerId(p1a, p1b)
            ?: teamStatsRepository.findByPlayer1PlayerIdAndPlayer2PlayerId(p1b, p1a))
            ?.let { TeamStatsResponse.from(it) }
            ?: throw IllegalStateException("Team stats missing for players $p1a and $p1b")

        val team2PairStats = (teamStatsRepository.findByPlayer1PlayerIdAndPlayer2PlayerId(p2a, p2b)
            ?: teamStatsRepository.findByPlayer1PlayerIdAndPlayer2PlayerId(p2b, p2a))
            ?.let { TeamStatsResponse.from(it) }
            ?: throw IllegalStateException("Team stats missing for players $p2a and $p2b")

        val (winnerTeam, winnerColor) = when {
            match.team1GameScore > match.team2GameScore -> Pair("Team 1", match.team1.teamColor.toString())
            match.team2GameScore > match.team1GameScore -> Pair("Team 2", match.team2.teamColor.toString())
            else -> Pair(null, null)
        }

        return Match(
            matchId = match.matchId ?: 0L,
            matchDate = match.matchDate.toString(),
            team1 = MatchTeam(
                teamId = match.team1.teamId ?: 0L,
                teamColor = match.team1.teamColor.toString(),
                gameScore = match.team1GameScore,
                pairWins = team1Wins,
                offense = matchPlayer(match.team1.offense.playerId!!, match.team1.offense.name),
                defense = matchPlayer(match.team1.defense.playerId!!, match.team1.defense.name),
                pairStats = team1PairStats
            ),
            team2 = MatchTeam(
                teamId = match.team2.teamId ?: 0L,
                teamColor = match.team2.teamColor.toString(),
                gameScore = match.team2GameScore,
                pairWins = team2Wins,
                offense = matchPlayer(match.team2.offense.playerId!!, match.team2.offense.name),
                defense = matchPlayer(match.team2.defense.playerId!!, match.team2.defense.name),
                pairStats = team2PairStats
            ),
            winner = winnerTeam,
            winnerColor = winnerColor
        )
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun createMatch(request: MatchRequest, matchDate: LocalDate = LocalDate.now()): Match {
        // ── 1. Resolve Players → Teams ────────────────────────────────────────
        val team1 = resolveOrCreateTeam(request.team1)
        val team2 = resolveOrCreateTeam(request.team2)
        if (team1.teamId == team2.teamId) throw IllegalArgumentException("A match must involve two different teams")
        if (request.team1GameScore < 0 || request.team2GameScore < 0) throw IllegalArgumentException("Scores cannot be negative")

        // ── 2. Save Match ─────────────────────────────────────────────────────
        val saved = matchRepository.save(MatchEntity(
            matchDate = matchDate,
            team1 = team1, team2 = team2,
            team1GameScore = request.team1GameScore,
            team2GameScore = request.team2GameScore
        ))

        val team1Won = saved.team1GameScore > saved.team2GameScore
        val team2Won = saved.team2GameScore > saved.team1GameScore

        // ── 3. Save PlayerMatch (junction) ────────────────────────────────────
        val pm1o = playerMatchRepository.save(PlayerMatch(player = team1.offense, match = saved, teamColor = team1.teamColor, position = Position.OFFENSE, won = team1Won))
        val pm1d = playerMatchRepository.save(PlayerMatch(player = team1.defense, match = saved, teamColor = team1.teamColor, position = Position.DEFENSE, won = team1Won))
        val pm2o = playerMatchRepository.save(PlayerMatch(player = team2.offense, match = saved, teamColor = team2.teamColor, position = Position.OFFENSE, won = team2Won))
        val pm2d = playerMatchRepository.save(PlayerMatch(player = team2.defense, match = saved, teamColor = team2.teamColor, position = Position.DEFENSE, won = team2Won))

        // ── 4. Snapshot pre-match Elo, compute new ratings, save PlayerRating ─
        fun playerElo(id: Long) = playerStatsRepository.findByPlayerPlayerId(id)?.eloRating ?: 1600.0
        fun pairElo(id1: Long, id2: Long) = (teamStatsRepository.findByPlayer1PlayerIdAndPlayer2PlayerId(id1, id2)
            ?: teamStatsRepository.findByPlayer1PlayerIdAndPlayer2PlayerId(id2, id1))?.eloRating ?: 1600.0

        val t1oEloBefore = playerElo(team1.offense.playerId!!)
        val t1dEloBefore = playerElo(team1.defense.playerId!!)
        val t2oEloBefore = playerElo(team2.offense.playerId!!)
        val t2dEloBefore = playerElo(team2.defense.playerId!!)

        val t1Games = playerStatsRepository.findByPlayerPlayerId(team1.offense.playerId!!)?.totalMatches ?: 0
        val t1dGames = playerStatsRepository.findByPlayerPlayerId(team1.defense.playerId!!)?.totalMatches ?: 0
        val t2Games = playerStatsRepository.findByPlayerPlayerId(team2.offense.playerId!!)?.totalMatches ?: 0
        val t2dGames = playerStatsRepository.findByPlayerPlayerId(team2.defense.playerId!!)?.totalMatches ?: 0

        val (t1oEloAfter, t1dEloAfter) = statsService.eloService.calculateNewTeamRatings(
            t1oEloBefore, t1dEloBefore, t2oEloBefore, t2dEloBefore,
            t1Games, t1dGames, saved.team1GameScore, saved.team2GameScore, team1Won)
        val (t2oEloAfter, t2dEloAfter) = statsService.eloService.calculateNewTeamRatings(
            t2oEloBefore, t2dEloBefore, t1oEloBefore, t1dEloBefore,
            t2Games, t2dGames, saved.team2GameScore, saved.team1GameScore, team2Won)

        playerRatingRepository.save(PlayerRating(playerMatch = pm1o, ratingBefore = t1oEloBefore, ratingAfter = t1oEloAfter, ratingChange = t1oEloAfter - t1oEloBefore))
        playerRatingRepository.save(PlayerRating(playerMatch = pm1d, ratingBefore = t1dEloBefore, ratingAfter = t1dEloAfter, ratingChange = t1dEloAfter - t1dEloBefore))
        playerRatingRepository.save(PlayerRating(playerMatch = pm2o, ratingBefore = t2oEloBefore, ratingAfter = t2oEloAfter, ratingChange = t2oEloAfter - t2oEloBefore))
        playerRatingRepository.save(PlayerRating(playerMatch = pm2d, ratingBefore = t2dEloBefore, ratingAfter = t2dEloAfter, ratingChange = t2dEloAfter - t2dEloBefore))

        // ── 5. Save TeamMatch (junction) ──────────────────────────────────────
        val tm1 = teamMatchRepository.save(TeamMatch(team = team1, match = saved, won = team1Won, gameScore = saved.team1GameScore))
        val tm2 = teamMatchRepository.save(TeamMatch(team = team2, match = saved, won = team2Won, gameScore = saved.team2GameScore))

        // ── 6. Snapshot pre-match pair Elo, compute new, save TeamRating ──────
        val t1PairEloBefore = pairElo(team1.offense.playerId!!, team1.defense.playerId!!)
        val t2PairEloBefore = pairElo(team2.offense.playerId!!, team2.defense.playerId!!)

        val t1PairGames = (teamStatsRepository.findByPlayer1PlayerIdAndPlayer2PlayerId(team1.offense.playerId!!, team1.defense.playerId!!)
            ?: teamStatsRepository.findByPlayer1PlayerIdAndPlayer2PlayerId(team1.defense.playerId!!, team1.offense.playerId!!))?.totalMatches ?: 0
        val t2PairGames = (teamStatsRepository.findByPlayer1PlayerIdAndPlayer2PlayerId(team2.offense.playerId!!, team2.defense.playerId!!)
            ?: teamStatsRepository.findByPlayer1PlayerIdAndPlayer2PlayerId(team2.defense.playerId!!, team2.offense.playerId!!))?.totalMatches ?: 0

        val pairPointFactor = statsService.eloService.calculatePointFactor(saved.team1GameScore, saved.team2GameScore)
        val t1PairExpected = statsService.eloService.calculatePlayerExpectedScore(t1PairEloBefore, t2PairEloBefore)
        val t2PairExpected = statsService.eloService.calculatePlayerExpectedScore(t2PairEloBefore, t1PairEloBefore)
        val t1PairK = statsService.eloService.calculateKFactor(t1PairGames)
        val t2PairK = statsService.eloService.calculateKFactor(t2PairGames)
        val t1PairEloAfter = t1PairEloBefore + (t1PairK * pairPointFactor) * ((if (team1Won) 1.0 else 0.0) - t1PairExpected)
        val t2PairEloAfter = t2PairEloBefore + (t2PairK * pairPointFactor) * ((if (team2Won) 1.0 else 0.0) - t2PairExpected)

        teamRatingRepository.save(TeamRating(teamMatch = tm1, ratingBefore = t1PairEloBefore, ratingAfter = t1PairEloAfter, ratingChange = t1PairEloAfter - t1PairEloBefore))
        teamRatingRepository.save(TeamRating(teamMatch = tm2, ratingBefore = t2PairEloBefore, ratingAfter = t2PairEloAfter, ratingChange = t2PairEloAfter - t2PairEloBefore))

        // ── 7. Update aggregate stats (reads from rating tables) ──────────────
        updateMatchup(team1.teamId!!, team2.teamId!!, when {
            saved.team1GameScore > saved.team2GameScore -> team1.teamId!!
            saved.team2GameScore > saved.team1GameScore -> team2.teamId!!
            else -> 0L
        })
        statsService.updateStatsForMatch(saved, t1oEloAfter, t1dEloAfter, t2oEloAfter, t2dEloAfter, t1PairEloAfter, t2PairEloAfter)

        return toMatch(saved)
    }

    private fun resolveOrCreateTeam(teamRequest: TeamRequest): Team {
        val offense = playerRepository.findById(teamRequest.offense)
            .orElseThrow { NoSuchElementException("Player not found: ${teamRequest.offense}") }
        val defense = playerRepository.findById(teamRequest.defense)
            .orElseThrow { NoSuchElementException("Player not found: ${teamRequest.defense}") }
        return teamRepository.findByOffenseAndDefenseAndColor(offense.playerId!!, defense.playerId!!, teamRequest.teamColor)
            .orElseGet { teamRepository.save(Team(offense = offense, defense = defense, teamColor = teamRequest.teamColor)) }
    }

    fun updateMatch(id: Long, matchDate: LocalDate? = null, team1GameScore: Int? = null, team2GameScore: Int? = null): Match {
        var entity = matchRepository.findById(id).orElseThrow { NoSuchElementException("Match not found: $id") }
        if (matchDate != null) entity = entity.copy(matchDate = matchDate)
        if (team1GameScore != null) { if (team1GameScore < 0) throw IllegalArgumentException("Score cannot be negative"); entity = entity.copy(team1GameScore = team1GameScore) }
        if (team2GameScore != null) { if (team2GameScore < 0) throw IllegalArgumentException("Score cannot be negative"); entity = entity.copy(team2GameScore = team2GameScore) }
        return toMatch(matchRepository.save(entity))
    }

    fun deleteMatch(id: Long) {
        if (!matchRepository.existsById(id)) throw NoSuchElementException("Match not found: $id")
        matchRepository.deleteById(id)
    }

    fun getTeamStats(teamId: Long): Map<String, Any> {
        val matches = matchRepository.findByTeamId(teamId)
        var wins = 0; var losses = 0; var draws = 0
        matches.forEach { match ->
            val team1IsTeam = match.team1.teamId == teamId
            when {
                match.team1GameScore > match.team2GameScore && team1IsTeam -> wins++
                match.team2GameScore > match.team1GameScore && !team1IsTeam -> wins++
                match.team1GameScore == match.team2GameScore -> draws++
                else -> losses++
            }
        }
        return mapOf("teamId" to teamId, "totalMatches" to matches.size, "wins" to wins, "draws" to draws, "losses" to losses)
    }

    private fun updateMatchup(team1Id: Long, team2Id: Long, winnerTeamId: Long) {
        val team1 = teamRepository.findById(team1Id).orElseThrow { NoSuchElementException("Team not found: $team1Id") }
        val team2 = teamRepository.findById(team2Id).orElseThrow { NoSuchElementException("Team not found: $team2Id") }

        val pair1 = listOf(team1.offense.playerId!!, team1.defense.playerId!!).sorted()
        val pair2 = listOf(team2.offense.playerId!!, team2.defense.playerId!!).sorted()
        val p1a = pair1[0]; val p1b = pair1[1]
        val p2a = pair2[0]; val p2b = pair2[1]

        val existingMatchup = matchupRepository.findMatchup(p1a, p1b, p2a, p2b)
        val pair1Won = winnerTeamId == team1Id
        val pair2Won = winnerTeamId == team2Id
        val swapped = existingMatchup.isPresent &&
            existingMatchup.get().player1a.playerId == p2a &&
            existingMatchup.get().player1b.playerId == p2b

        val (newPair1Wins, newPair2Wins, newDraws) = if (existingMatchup.isPresent) {
            val m = existingMatchup.get()
            when {
                pair1Won && !swapped -> Triple(m.pair1Wins + 1, m.pair2Wins, m.draws)
                pair1Won && swapped  -> Triple(m.pair1Wins, m.pair2Wins + 1, m.draws)
                pair2Won && !swapped -> Triple(m.pair1Wins, m.pair2Wins + 1, m.draws)
                pair2Won && swapped  -> Triple(m.pair1Wins + 1, m.pair2Wins, m.draws)
                else                 -> Triple(m.pair1Wins, m.pair2Wins, m.draws + 1)
            }
        } else {
            when { pair1Won -> Triple(1, 0, 0); pair2Won -> Triple(0, 1, 0); else -> Triple(0, 0, 1) }
        }

        val matchup = if (existingMatchup.isPresent) {
            existingMatchup.get().copy(pair1Wins = newPair1Wins, pair2Wins = newPair2Wins, draws = newDraws)
        } else {
            val (p1aE, p1bE) = if (team1.offense.playerId!! <= team1.defense.playerId!!) team1.offense to team1.defense else team1.defense to team1.offense
            val (p2aE, p2bE) = if (team2.offense.playerId!! <= team2.defense.playerId!!) team2.offense to team2.defense else team2.defense to team2.offense
            Matchup(player1a = p1aE, player1b = p1bE, player2a = p2aE, player2b = p2bE, pair1Wins = newPair1Wins, pair2Wins = newPair2Wins, draws = newDraws)
        }
        matchupRepository.save(matchup)
    }
}

