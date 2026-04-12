package no.ks_digital.foos.controller

import no.ks_digital.foos.dto.LeaderboardResponse
import no.ks_digital.foos.dto.PlayerStatsResponse
import no.ks_digital.foos.dto.TeamStatsResponse
import no.ks_digital.foos.service.StatsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class StatsController(private val statsService: StatsService) {

    @GetMapping("/api/stats/teams")
    fun getAllTeamStats(
        @RequestParam(required = false) search: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "false") paginate: Boolean
    ): ResponseEntity<*> {
        return if (!paginate && search.isNullOrBlank()) {
            ResponseEntity.ok(statsService.getAllTeamStats())
        } else {
            ResponseEntity.ok(statsService.getTeamStatsPage(search, page, size))
        }
    }

    @GetMapping("/api/stats/teams/{id}")
    fun getTeamStatsById(@PathVariable id: Long): ResponseEntity<TeamStatsResponse> {
        val stats = statsService.getTeamStatsById(id)
        return if (stats != null) ResponseEntity.ok(stats) else ResponseEntity.notFound().build()
    }

    @GetMapping("/api/stats/teams/{player1Id}/{player2Id}")
    fun getTeamStatsByPlayers(
        @PathVariable player1Id: Long,
        @PathVariable player2Id: Long
    ): ResponseEntity<TeamStatsResponse> {
        val stats = statsService.getTeamStats(player1Id, player2Id)
        return if (stats != null) ResponseEntity.ok(stats) else ResponseEntity.notFound().build()
    }

    @GetMapping("/api/stats/players")
    fun getAllPlayerStats(
        @RequestParam(required = false) search: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "false") paginate: Boolean
    ): ResponseEntity<*> {
        return if (!paginate && search.isNullOrBlank()) {
            ResponseEntity.ok(statsService.getAllPlayerStats())
        } else {
            ResponseEntity.ok(statsService.getPlayerStatsPage(search, page, size))
        }
    }

    @GetMapping("/api/stats/players/{playerId}")
    fun getPlayerStats(@PathVariable playerId: Long): ResponseEntity<PlayerStatsResponse> {
        val stats = statsService.getPlayerStats(playerId)
        return if (stats != null) ResponseEntity.ok(stats) else ResponseEntity.notFound().build()
    }

    @GetMapping("/api/stats/leaderboard")
    fun getLeaderboard(): ResponseEntity<LeaderboardResponse> =
        ResponseEntity.ok(statsService.getLeaderboard())
}
