package no.ks_digital.foos.dto

import no.ks_digital.foos.entity.TeamColor

data class TeamRequest(
    val offense: Long,
    val defense: Long,
    val teamColor: TeamColor
)