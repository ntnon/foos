package no.ks_digital.foos.repository

import no.ks_digital.foos.entity.TeamRating
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface TeamRatingRepository : JpaRepository<TeamRating, Long> {
    fun findByTeamMatchTeamMatchId(teamMatchId: Long): TeamRating?

    @Query("""
        SELECT r FROM TeamRating r 
        WHERE (r.teamMatch.team.offense.playerId = :p1 AND r.teamMatch.team.defense.playerId = :p2)
           OR (r.teamMatch.team.offense.playerId = :p2 AND r.teamMatch.team.defense.playerId = :p1)
        ORDER BY r.createdAt ASC
    """)
    fun findAllByPairOrderedByDate(@Param("p1") p1: Long, @Param("p2") p2: Long): List<TeamRating>
}
