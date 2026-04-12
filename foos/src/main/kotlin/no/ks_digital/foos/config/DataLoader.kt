package no.ks_digital.foos.config

import no.ks_digital.foos.dto.TeamRequest
import no.ks_digital.foos.entity.Player
import no.ks_digital.foos.entity.Team
import no.ks_digital.foos.entity.TeamColor
import no.ks_digital.foos.repository.PlayerRepository
import no.ks_digital.foos.repository.TeamRepository
import no.ks_digital.foos.repository.MatchRepository
import no.ks_digital.foos.service.MatchService
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Configuration
import java.time.LocalDate

@Configuration
class DataLoader(
    private val playerRepository: PlayerRepository,
    private val teamRepository: TeamRepository,
    private val matchRepository: MatchRepository,
    private val matchService: MatchService
) : CommandLineRunner {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun run(vararg args: String) {
        logger.info("DataLoader starting...")

        val playerCount = playerRepository.count()
        logger.info("Current player count: $playerCount")

        // Only load if database is empty
        if (playerCount > 0) {
            logger.info("Database already has data. Skipping data load.")
            return
        }

        logger.info("Loading initial data...")
        loadPlayers()
        loadTeams()
        loadMatches()
        logger.info("Data loading completed!")
    }

    private fun loadPlayers() {
        val players = listOf(
            "Nora", "Aki", "Anton", "Sondre FS", "Kolbein",
            "Håkon", "Andreas", "Stian", "Jakob", "Carl-Trygve",
            "Roar", "Såndræ F", "Ragnar", "Eivind", "Stian M",
            "Erlend", "Andreas MB", "Daniel"
        ).map { Player(name = it) }

        playerRepository.saveAll(players)
    }

    private fun loadTeams() {
        // Teams will be created as needed in loadMatches
    }

    private fun loadMatches() {
        val players = playerRepository.findAll().associateBy { it.name }

        // Helper function to get player by name
        fun getPlayer(name: String): Player = players[name] ?: error("Player $name not found")

        // Helper function to create/get or create team - respects color
        fun getOrCreateTeam(p1Name: String, p2Name: String, color: TeamColor = TeamColor.BLUE): Team {
            val p1 = getPlayer(p1Name)
            val p2 = getPlayer(p2Name)
            val existingTeam = teamRepository.findAll().find {
                it.teamColor == color &&
                ((it.offense.playerId == p1.playerId && it.defense.playerId == p2.playerId) ||
                 (it.offense.playerId == p2.playerId && it.defense.playerId == p1.playerId))
            }
            return existingTeam ?: teamRepository.save(
                Team(offense = p1, defense = p2, teamColor = color)
            )
        }

        // Helper function to parse game score from string like "10-5"
        fun parseGameScore(gameScoreStr: String?): Pair<Int?, Int?> {
            if (gameScoreStr == null || gameScoreStr == "FS") return Pair(null, null)
            val parts = gameScoreStr.split("-")
            return if (parts.size == 2) {
                Pair(parts[0].toIntOrNull(), parts[1].toIntOrNull())
            } else {
                Pair(null, null)
            }
        }

        val matches = listOf(

            // Mandag 09.feb - Nora & Aki 1 - 0 Anton & Sondre FS
            Pair(LocalDate.of(2026, 2, 9), Triple(getOrCreateTeam("Nora", "Aki", TeamColor.BLUE), getOrCreateTeam("Anton", "Sondre FS", TeamColor.RED), Pair(10, 8))),

            // Tirsdag 10.feb - Nora & Kolbein 1 - 0 Anton & Aki
            Pair(LocalDate.of(2026, 2, 10), Triple(getOrCreateTeam("Nora", "Kolbein", TeamColor.BLUE), getOrCreateTeam("Anton", "Aki", TeamColor.RED), Pair(10, 8))),
            Pair(LocalDate.of(2026, 2, 10), Triple(getOrCreateTeam("Håkon", "Kolbein", TeamColor.BLUE), getOrCreateTeam("Andreas", "Aki", TeamColor.RED), Pair(10, 8))),

            // Onsdag 11.feb
            Pair(LocalDate.of(2026, 2, 11), Triple(getOrCreateTeam("Kolbein", "Nora", TeamColor.BLUE), getOrCreateTeam("Stian", "Jakob", TeamColor.RED), Pair(10, 8))),
            Pair(LocalDate.of(2026, 2, 11), Triple(getOrCreateTeam("Kolbein", "Anton", TeamColor.BLUE), getOrCreateTeam("Carl-Trygve", "Roar", TeamColor.RED), Pair(10, 8))),
            Pair(LocalDate.of(2026, 2, 11), Triple(getOrCreateTeam("Kolbein", "Andreas", TeamColor.BLUE), getOrCreateTeam("Håkon", "Aki", TeamColor.RED), Pair(10, 8))),

            // Torsdag 12.feb
            Pair(LocalDate.of(2026, 2, 12), Triple(getOrCreateTeam("Kolbein", "Andreas", TeamColor.RED), getOrCreateTeam("Nora", "Aki", TeamColor.BLUE), Pair(10, 8))),
            Pair(LocalDate.of(2026, 2, 12), Triple(getOrCreateTeam("Kolbein", "Anton", TeamColor.RED), getOrCreateTeam("Nora", "Aki", TeamColor.BLUE), Pair(10, 8))),


            // Onsdag 18.feb
            Pair(LocalDate.of(2026, 2, 18), Triple(getOrCreateTeam("Såndræ F", "Roar", TeamColor.BLUE), getOrCreateTeam("Anton", "Nora", TeamColor.RED), Pair(10, 8))),
            Pair(LocalDate.of(2026, 2, 18), Triple(getOrCreateTeam("Kolbein", "Aki", TeamColor.BLUE), getOrCreateTeam("Anton", "Håkon", TeamColor.RED), Pair(10, 8))),

            Pair(LocalDate.of(2026, 2, 19), Triple(getOrCreateTeam("Aki", "Såndræ F", TeamColor.BLUE), getOrCreateTeam("Nora", "Stian M", TeamColor.RED), Pair(10, 8))),

            // Matches above given 10-8 scores

            // Fredag 13.feb
            Pair(LocalDate.of(2026, 2, 13), Triple(getOrCreateTeam("Kolbein", "Sondre FS", TeamColor.RED), getOrCreateTeam("Aki", "Anton", TeamColor.BLUE), Pair(10, 5))),
            Pair(LocalDate.of(2026, 2, 13), Triple(getOrCreateTeam("Stian M", "Erlend", TeamColor.RED), getOrCreateTeam("Nora", "Jakob", TeamColor.BLUE), Pair(10, 9))),
            Pair(LocalDate.of(2026, 2, 13), Triple(getOrCreateTeam("Kolbein", "Sondre FS", TeamColor.RED), getOrCreateTeam("Aki", "Anton", TeamColor.BLUE), Pair(10, 7))),

            // Mandag 16.feb
            Pair(LocalDate.of(2026, 2, 16), Triple(getOrCreateTeam("Sondre FS", "Carl-Trygve", TeamColor.RED), getOrCreateTeam("Anton", "Roar", TeamColor.BLUE), Pair(10, 2))),
            Pair(LocalDate.of(2026, 2, 16), Triple(getOrCreateTeam("Sondre FS", "Såndræ F", TeamColor.RED), getOrCreateTeam("Nora", "Aki", TeamColor.BLUE), Pair(10, 5))),

            // Tirsdag 17.feb - Andreas & Aki (B) 1 - 1 Kolbein & Håkon (10-8)
            Pair(LocalDate.of(2026, 2, 17), Triple(getOrCreateTeam("Andreas", "Aki", TeamColor.BLUE), getOrCreateTeam("Kolbein", "Håkon", TeamColor.RED), Pair(10, 8))),


            // Torsdag 19.feb
            Pair(LocalDate.of(2026, 2, 19), Triple(getOrCreateTeam("Daniel", "Såndræ F", TeamColor.RED), getOrCreateTeam("Carl-Trygve", "Andreas MB", TeamColor.BLUE), Pair(10, 2))),

            // Fredag 20.feb
            Pair(LocalDate.of(2026, 2, 20), Triple(getOrCreateTeam("Sondre FS", "Roar", TeamColor.BLUE), getOrCreateTeam("Nora", "Stian", TeamColor.RED), Pair(10, 8))),
            Pair(LocalDate.of(2026, 2, 20), Triple(getOrCreateTeam("Aki", "Eivind", TeamColor.RED), getOrCreateTeam("Nora", "Anton", TeamColor.BLUE), Pair(10, 7))),
            Pair(LocalDate.of(2026, 2, 20), Triple(getOrCreateTeam("Aki", "Anton", TeamColor.RED), getOrCreateTeam("Sondre FS", "Roar", TeamColor.BLUE), Pair(10, 2))),
            Pair(LocalDate.of(2026, 2, 20), Triple(getOrCreateTeam("Aki", "Sondre FS", TeamColor.RED), getOrCreateTeam("Anton", "Roar", TeamColor.BLUE), Pair(10, 3))),

            // Mandag 23.feb
            Pair(LocalDate.of(2026, 2, 23), Triple(getOrCreateTeam("Aki", "Sondre FS", TeamColor.RED), getOrCreateTeam("Anton", "Roar", TeamColor.BLUE), Pair(10, 8))),
        ).map { (matchDate, triple) ->
            val (team1, team2, gameScore) = triple
            val (g1, g2) = gameScore
            Triple(matchDate, Pair(team1, team2), Pair(g1, g2))
        }

        // Create matches using MatchService which will trigger stats updates
        for ((matchDate, teams, gameScore) in matches) {
            val (team1, team2) = teams
            val (g1, g2) = gameScore

            try {
                val teamRequest1 = no.ks_digital.foos.dto.TeamRequest(
                    offense = team1.offense.playerId!!,
                    defense = team1.defense.playerId!!,
                    teamColor = team1.teamColor
                )
                val teamRequest2 = no.ks_digital.foos.dto.TeamRequest(
                    offense = team2.offense.playerId!!,
                    defense = team2.defense.playerId!!,
                    teamColor = team2.teamColor
                )

                val matchRequest = no.ks_digital.foos.dto.MatchRequest(
                    team1 = teamRequest1,
                    team2 = teamRequest2,
                    team1GameScore = g1 ?: 0,
                    team2GameScore = g2 ?: 0
                )

                matchService.createMatch(matchRequest, matchDate)
            } catch (e: Exception) {
                logger.warn("Failed to create match: ${e.message}")
            }
        }
    }
}
