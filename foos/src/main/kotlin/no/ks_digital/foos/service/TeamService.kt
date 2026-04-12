package no.ks_digital.foos.service

import no.ks_digital.foos.entity.Team
import no.ks_digital.foos.entity.TeamColor
import no.ks_digital.foos.repository.TeamRepository
import no.ks_digital.foos.repository.PlayerRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.util.*

@Service
class TeamService(
    private val teamRepository: TeamRepository,
    private val playerRepository: PlayerRepository
) {

    fun getAllTeams(): List<Team> = teamRepository.findAll()

    fun getTeams(search: String?, page: Int, size: Int): Page<Team> {
        val pageable = PageRequest.of(page, size, Sort.by("teamId"))
        return if (!search.isNullOrBlank())
            teamRepository.searchByPlayerName(search, pageable)
        else
            teamRepository.findAll(pageable)
    }

    fun getTeamById(id: Long): Optional<Team> = teamRepository.findById(id)

    fun createTeam(offense: Long, defense: Long, teamColor: TeamColor): Team {
        if (offense == defense) throw IllegalArgumentException("A team must have two different players")
        val offensePlayer = playerRepository.findById(offense)
            .orElseThrow { NoSuchElementException("Player not found with id: $offense") }
        val defensePlayer = playerRepository.findById(defense)
            .orElseThrow { NoSuchElementException("Player not found with id: $defense") }
        return teamRepository.save(Team(offense = offensePlayer, defense = defensePlayer, teamColor = teamColor))
    }

    fun updateTeam(id: Long, offense: Long?, defense: Long?, teamColor: TeamColor): Team {
        var team = teamRepository.findById(id)
            .orElseThrow { NoSuchElementException("Team not found with id: $id") }
        if (offense != null) {
            val p = playerRepository.findById(offense).orElseThrow { NoSuchElementException("Player not found: $offense") }
            team = team.copy(offense = p)
        }
        if (defense != null) {
            val p = playerRepository.findById(defense).orElseThrow { NoSuchElementException("Player not found: $defense") }
            team = team.copy(defense = p)
        }
        team = team.copy(teamColor = teamColor)
        if (team.offense.playerId == team.defense.playerId) throw IllegalArgumentException("A team must have two different players")
        return teamRepository.save(team)
    }

    fun deleteTeam(id: Long) {
        if (!teamRepository.existsById(id)) throw NoSuchElementException("Team not found with id: $id")
        teamRepository.deleteById(id)
    }
}
