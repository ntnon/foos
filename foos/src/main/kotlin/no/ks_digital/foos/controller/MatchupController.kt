package no.ks_digital.foos.controller

import no.ks_digital.foos.dto.MatchupResponse
import no.ks_digital.foos.dto.TeamStatsResponse
import no.ks_digital.foos.service.MatchupService
import no.ks_digital.foos.service.StatsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/matchups")
class MatchupController(
    private val matchupService: MatchupService,
    private val statsService: StatsService
) {

    @GetMapping
    fun getAllMatchups(): ResponseEntity<List<MatchupResponse>> =
        ResponseEntity.ok(matchupService.getAllMatchups())

    @GetMapping("/player/{playerId}")
    fun getMatchupsForPlayer(@PathVariable playerId: Long): ResponseEntity<List<MatchupResponse>> =
        ResponseEntity.ok(matchupService.getMatchupsForPlayer(playerId))

    // Returns all teams sorted by win rate, with preferred color — backed by TeamStats
    @GetMapping("/pair-stats")
    fun getPairStats(): ResponseEntity<List<TeamStatsResponse>> =
        ResponseEntity.ok(statsService.getAllTeamStats())
}




