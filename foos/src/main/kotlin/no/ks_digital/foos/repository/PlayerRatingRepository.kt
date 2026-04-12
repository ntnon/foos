package no.ks_digital.foos.repository

import no.ks_digital.foos.entity.PlayerRating
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface PlayerRatingRepository : JpaRepository<PlayerRating, Long> {
    fun findByPlayerMatchPlayerMatchId(playerMatchId: Long): PlayerRating?
    fun findByPlayerMatchPlayerPlayerId(playerId: Long): List<PlayerRating>

    @Query("SELECT r FROM PlayerRating r WHERE r.playerMatch.player.playerId = :playerId ORDER BY r.createdAt ASC")
    fun findAllByPlayerIdOrderedByDate(@Param("playerId") playerId: Long): List<PlayerRating>
}
