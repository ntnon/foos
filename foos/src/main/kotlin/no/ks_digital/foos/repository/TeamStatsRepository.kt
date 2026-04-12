package no.ks_digital.foos.repository

import no.ks_digital.foos.entity.TeamStats
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface TeamStatsRepository : JpaRepository<TeamStats, Long> {
    fun findByPlayer1PlayerIdAndPlayer2PlayerId(player1Id: Long, player2Id: Long): TeamStats?

    @Query("SELECT t FROM TeamStats t WHERE t.player1.playerId = :playerId OR t.player2.playerId = :playerId")
    fun findAllByPlayerId(@Param("playerId") playerId: Long): List<TeamStats>

    @Query("SELECT t FROM TeamStats t WHERE LOWER(t.player1.name) LIKE LOWER(CONCAT('%',:search,'%')) OR LOWER(t.player2.name) LIKE LOWER(CONCAT('%',:search,'%'))")
    fun searchByPlayerName(@Param("search") search: String, pageable: Pageable): Page<TeamStats>

    fun findAllBy(pageable: Pageable): Page<TeamStats>
}

