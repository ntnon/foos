package no.ks_digital.foos.repository

import no.ks_digital.foos.entity.Matchup
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface MatchupRepository : JpaRepository<Matchup, Long> {

    // Find matchup by two player pairs, regardless of which is pair1/pair2
    // Within each pair, players are stored with lower ID first
    @Query("""
        SELECT m FROM Matchup m
        WHERE (
            (m.player1a.playerId = :p1a AND m.player1b.playerId = :p1b AND m.player2a.playerId = :p2a AND m.player2b.playerId = :p2b)
         OR (m.player1a.playerId = :p2a AND m.player1b.playerId = :p2b AND m.player2a.playerId = :p1a AND m.player2b.playerId = :p1b)
        )
    """)
    fun findMatchup(
        @Param("p1a") p1a: Long,
        @Param("p1b") p1b: Long,
        @Param("p2a") p2a: Long,
        @Param("p2b") p2b: Long
    ): Optional<Matchup>

    @Query("""
        SELECT m FROM Matchup m
        WHERE m.player1a.playerId = :playerId OR m.player1b.playerId = :playerId
           OR m.player2a.playerId = :playerId OR m.player2b.playerId = :playerId
    """)
    fun findByPlayer(@Param("playerId") playerId: Long): List<Matchup>
}
