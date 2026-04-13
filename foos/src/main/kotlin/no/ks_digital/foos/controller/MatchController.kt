package no.ks_digital.foos.controller

import no.ks_digital.foos.dto.MatchResponse
import no.ks_digital.foos.dto.MatchRequest
import no.ks_digital.foos.service.MatchService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/matches")
@CrossOrigin(origins = ["http://localhost:4200"])
class MatchController(private val matchService: MatchService) {

    @GetMapping
    fun getAllMatches(
        @RequestParam(defaultValue = "10") limit: Int
    ): ResponseEntity<List<MatchResponse>> =
        ResponseEntity.ok(matchService.getRecentMatchResults(limit))

    @GetMapping("/recent")
    fun getRecentMatches(
        @RequestParam(defaultValue = "10") limit: Int
    ): ResponseEntity<List<MatchResponse>> =
        ResponseEntity.ok(matchService.getRecentMatchResults(limit))

    @PostMapping
    fun createMatch(@RequestBody request: MatchRequest): ResponseEntity<MatchResponse> {
        return try {
            ResponseEntity.status(HttpStatus.CREATED).body(matchService.createMatch(request))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        } catch (e: NoSuchElementException) {
            ResponseEntity.badRequest().build()
        }
    }

    @GetMapping("/player/{playerId}")
    fun getMatchesByPlayer(
        @PathVariable playerId: Long,
        @RequestParam(defaultValue = "20") limit: Int
    ): ResponseEntity<List<MatchResponse>> =
        ResponseEntity.ok(matchService.getMatchesByPlayer(playerId, limit))

    @GetMapping("/team/{player1Id}/{player2Id}")
    fun getMatchesByTeam(
        @PathVariable player1Id: Long,
        @PathVariable player2Id: Long,
        @RequestParam(defaultValue = "20") limit: Int
    ): ResponseEntity<List<MatchResponse>> =
        ResponseEntity.ok(matchService.getMatchesByTeamStats(player1Id, player2Id, limit))

    @GetMapping("/{id}")
    fun getMatch(@PathVariable id: Long): ResponseEntity<MatchResponse> {
        return try {
            ResponseEntity.ok(matchService.getMatch(id))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }

    @PutMapping("/{id}")
    fun updateMatch(
        @PathVariable id: Long,
        @RequestBody request: UpdateMatchRequest
    ): ResponseEntity<MatchResponse> {
        return try {
            ResponseEntity.ok(matchService.updateMatch(id, request.matchDate, request.team1GameScore, request.team2GameScore))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
    }

    @DeleteMapping("/{id}")
    fun deleteMatch(@PathVariable id: Long): ResponseEntity<Void> {
        return try {
            matchService.deleteMatch(id)
            ResponseEntity.noContent().build()
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }
}

data class UpdateMatchRequest(
    val matchDate: LocalDate? = null,
    val team1GameScore: Int? = null,
    val team2GameScore: Int? = null
)
