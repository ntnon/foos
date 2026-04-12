package no.ks_digital.foos.controller

import no.ks_digital.foos.entity.Team
import no.ks_digital.foos.entity.TeamColor
import no.ks_digital.foos.service.TeamService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/teams")
@CrossOrigin(origins = ["http://localhost:4200"])
class TeamController(private val teamService: TeamService) {

    @GetMapping
    fun getAllTeams(
        @RequestParam(required = false) search: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "false") paginate: Boolean
    ): ResponseEntity<*> {
        return if (!paginate && search.isNullOrBlank()) {
            ResponseEntity.ok(teamService.getAllTeams())
        } else {
            ResponseEntity.ok(teamService.getTeams(search, page, size))
        }
    }

    @GetMapping("/{id}")
    fun getTeamById(@PathVariable id: Long): ResponseEntity<Team> {
        val teamOpt = teamService.getTeamById(id)
        return if (teamOpt.isPresent) ResponseEntity.ok(teamOpt.get())
               else ResponseEntity.notFound().build()
    }

    @PostMapping
    fun createTeam(@RequestBody request: CreateTeamRequest): ResponseEntity<Team> {
        return try {
            val teamColor = request.teamColor ?: TeamColor.BLUE
            val team = teamService.createTeam(request.offense, request.defense, teamColor)
            ResponseEntity.status(HttpStatus.CREATED).body(team)
        } catch (_: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        } catch (_: NoSuchElementException) {
            ResponseEntity.badRequest().build()
        }
    }

    @PutMapping("/{id}")
    fun updateTeam(
        @PathVariable id: Long,
        @RequestBody request: UpdateTeamRequest
    ): ResponseEntity<Team> {
        return try {
            val teamColor = request.teamColor ?: TeamColor.BLUE
            val team = teamService.updateTeam(id, request.offense, request.defense, teamColor)
            ResponseEntity.ok(team)
        } catch (_: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (_: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
    }
}

data class CreateTeamRequest(val offense: Long, val defense: Long, val teamColor: TeamColor? = null)
data class UpdateTeamRequest(val offense: Long? = null, val defense: Long? = null, val teamColor: TeamColor? = null)
