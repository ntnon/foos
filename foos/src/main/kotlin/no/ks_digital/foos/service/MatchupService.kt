package no.ks_digital.foos.service

import no.ks_digital.foos.dto.MatchupResponse
import no.ks_digital.foos.repository.MatchupRepository
import org.springframework.stereotype.Service

@Service
class MatchupService(
    private val matchupRepository: MatchupRepository
) {
    fun getAllMatchups(): List<MatchupResponse> =
        matchupRepository.findAll().map { MatchupResponse.from(it) }

    fun getMatchupsForPlayer(playerId: Long): List<MatchupResponse> =
        matchupRepository.findByPlayer(playerId).map { MatchupResponse.from(it) }
}
