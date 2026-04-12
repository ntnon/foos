package no.ks_digital.foos.repository

import no.ks_digital.foos.entity.TeamMatch
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TeamMatchRepository : JpaRepository<TeamMatch, Long> {
    fun findByMatchMatchId(matchId: Long): List<TeamMatch>
    fun findByTeamTeamId(teamId: Long): List<TeamMatch>
}

