package no.ks_digital.foos.controller

import no.ks_digital.foos.entity.Player
import no.ks_digital.foos.service.PlayerService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/players")
@CrossOrigin(origins = ["http://localhost:4200"])
class PlayerController(private val playerService: PlayerService) {

    @GetMapping
    fun getAllPlayers(
        @RequestParam(required = false) search: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "false") paginate: Boolean
    ): ResponseEntity<*> {
        // If paginate=false (default) and no search, return plain list for backwards compat
        return if (!paginate && search.isNullOrBlank()) {
            ResponseEntity.ok(playerService.getAllPlayers())
        } else {
            ResponseEntity.ok(playerService.getPlayers(search, page, size))
        }
    }

    @GetMapping("/{id}")
    fun getPlayerById(@PathVariable id: Long): ResponseEntity<Player> {
        return playerService.getPlayerById(id)
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())
    }

    @GetMapping("/name/{name}")
    fun getPlayerByName(@PathVariable name: String): ResponseEntity<Player> {
        return playerService.getPlayerByName(name)
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())
    }

    @PostMapping
    fun createPlayer(@RequestBody request: CreatePlayerRequest): ResponseEntity<Player> {
        return try {
            val player = playerService.createPlayer(request.name)
            ResponseEntity.status(HttpStatus.CREATED).body(player)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
    }

    @PutMapping("/{id}")
    fun updatePlayer(
        @PathVariable id: Long,
        @RequestBody request: UpdatePlayerRequest
    ): ResponseEntity<Player> {
        return try {
            val player = playerService.updatePlayer(id, request.name)
            ResponseEntity.ok(player)
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
    }

    @DeleteMapping("/{id}")
    fun deletePlayer(@PathVariable id: Long): ResponseEntity<Void> {
        return try {
            playerService.deletePlayer(id)
            ResponseEntity.noContent().build()
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }
}

data class CreatePlayerRequest(val name: String)
data class UpdatePlayerRequest(val name: String)
