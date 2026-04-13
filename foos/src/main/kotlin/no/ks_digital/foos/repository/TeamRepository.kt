package no.ks_digital.foos.repository

import no.ks_digital.foos.entity.Team
import no.ks_digital.foos.entity.TeamColor
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface TeamRepository : JpaRepository<Team, Long> {

    @Query("SELECT t FROM Team t WHERE t.offense.playerId = :offenseId AND t.defense.playerId = :defenseId AND t.teamColor = :color")
    fun findByOffenseAndDefenseAndColor(
        @Param("offenseId") offenseId: Long,
        @Param("defenseId") defenseId: Long,
        @Param("color") color: TeamColor
    ): Optional<Team>

    @Query("""
        SELECT COUNT(m) FROM Match m
        WHERE m.team1.teamColor = :color
          AND ((m.team1.offense.playerId = :playerAId AND m.team1.defense.playerId = :playerBId)
            OR (m.team1.offense.playerId = :playerBId AND m.team1.defense.playerId = :playerAId))
        OR m.team2.teamColor = :color
          AND ((m.team2.offense.playerId = :playerAId AND m.team2.defense.playerId = :playerBId)
            OR (m.team2.offense.playerId = :playerBId AND m.team2.defense.playerId = :playerAId))
    """)
    fun countMatchesWithColor(
        @Param("playerAId") playerAId: Long,
        @Param("playerBId") playerBId: Long,
        @Param("color") color: String
    ): Long

    @Query("SELECT t FROM Team t WHERE LOWER(t.offense.name) LIKE LOWER(CONCAT('%',:search,'%')) OR LOWER(t.defense.name) LIKE LOWER(CONCAT('%',:search,'%'))")
    fun searchByPlayerName(@Param("search") search: String, pageable: Pageable): Page<Team>
}
