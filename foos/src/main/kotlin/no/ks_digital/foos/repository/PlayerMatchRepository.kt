package no.ks_digital.foos.repository

import no.ks_digital.foos.entity.PlayerMatch
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface PlayerMatchRepository : JpaRepository<PlayerMatch, Long> {
    fun findByMatchMatchId(matchId: Long): List<PlayerMatch>
    fun findByPlayerPlayerId(playerId: Long): List<PlayerMatch>

    /** All opponents (players on the other team) that this player has faced */
    @Query("""
        SELECT pm2 FROM PlayerMatch pm1
        JOIN PlayerMatch pm2 ON pm1.match.matchId = pm2.match.matchId
        WHERE pm1.player.playerId = :playerId
          AND pm2.player.playerId != :playerId
          AND pm1.teamColor != pm2.teamColor
    """)
    fun findOpponentsByPlayerId(@Param("playerId") playerId: Long): List<PlayerMatch>
}

