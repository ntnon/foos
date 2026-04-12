package no.ks_digital.foos.repository

import no.ks_digital.foos.entity.PlayerStats
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface PlayerStatsRepository : JpaRepository<PlayerStats, Long> {
    @Query("SELECT p FROM PlayerStats p WHERE p.player.playerId = :playerId ORDER BY p.playerStatsId ASC LIMIT 1")
    fun findByPlayerPlayerId(@Param("playerId") playerId: Long): PlayerStats?

    @Query("SELECT p FROM PlayerStats p WHERE LOWER(p.player.name) LIKE LOWER(CONCAT('%',:search,'%'))")
    fun searchByPlayerName(@Param("search") search: String, pageable: Pageable): Page<PlayerStats>

    fun findAllBy(pageable: Pageable): Page<PlayerStats>
}
