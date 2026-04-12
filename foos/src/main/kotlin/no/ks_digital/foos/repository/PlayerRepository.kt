package no.ks_digital.foos.repository

import no.ks_digital.foos.entity.Player
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PlayerRepository : JpaRepository<Player, Long> {
    fun findByName(name: String): Optional<Player>
    fun existsByName(name: String): Boolean
    fun findByNameContainingIgnoreCase(name: String, pageable: Pageable): Page<Player>
}

