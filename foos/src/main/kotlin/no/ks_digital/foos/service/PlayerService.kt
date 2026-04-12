package no.ks_digital.foos.service

import no.ks_digital.foos.entity.Player
import no.ks_digital.foos.repository.PlayerRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.util.*

@Service
class PlayerService(private val playerRepository: PlayerRepository) {

    fun getAllPlayers(): List<Player> {
        return playerRepository.findAll(Sort.by("name"))
    }

    fun getPlayers(search: String?, page: Int, size: Int): Page<Player> {
        val pageable = PageRequest.of(page, size, Sort.by("name"))
        return if (!search.isNullOrBlank())
            playerRepository.findByNameContainingIgnoreCase(search, pageable)
        else
            playerRepository.findAll(pageable)
    }

    fun getPlayerById(id: Long): Optional<Player> = playerRepository.findById(id)

    fun getPlayerByName(name: String): Optional<Player> = playerRepository.findByName(name)

    fun createPlayer(name: String): Player {
        if (playerRepository.existsByName(name)) {
            throw IllegalArgumentException("Player with name '$name' already exists")
        }
        return playerRepository.save(Player(name = name))
    }

    fun updatePlayer(id: Long, name: String): Player {
        val player = playerRepository.findById(id)
            .orElseThrow { NoSuchElementException("Player not found with id: $id") }
        if (playerRepository.existsByName(name) && player.name != name) {
            throw IllegalArgumentException("Player with name '$name' already exists")
        }
        return playerRepository.save(player.copy(name = name))
    }

    fun deletePlayer(id: Long) {
        if (!playerRepository.existsById(id)) throw NoSuchElementException("Player not found with id: $id")
        playerRepository.deleteById(id)
    }
}

