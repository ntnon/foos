package no.ks_digital.foos.repository

import no.ks_digital.foos.entity.MatchEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface MatchRepository : JpaRepository<MatchEntity, Long> {
    fun findByMatchDate(matchDate: LocalDate): List<MatchEntity>

    @Query("SELECT m FROM MatchEntity m WHERE m.team1.teamId = :teamId OR m.team2.teamId = :teamId")
    fun findByTeamId(@Param("teamId") teamId: Long): List<MatchEntity>
}

