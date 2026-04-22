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
            "Erlend", "Andreas MB", "Daniel", "Lars Mikal",
            "Martin", "Marie", "Son3", "Helge", "Lotte",
            "Hege", "Jarle", "Herrevold", "Øystein", "Dan-Eric"
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

            // Tirsdag 24.feb
            Pair(LocalDate.of(2026, 2, 24), Triple(getOrCreateTeam("Aki", "Sondre FS", TeamColor.BLUE), getOrCreateTeam("Nora", "Andreas", TeamColor.RED), Pair(10, 7))),
            Pair(LocalDate.of(2026, 2, 24), Triple(getOrCreateTeam("Sondre FS", "Såndræ F", TeamColor.BLUE), getOrCreateTeam("Håkon", "Anton", TeamColor.RED), Pair(10, 1))),
            Pair(LocalDate.of(2026, 2, 24), Triple(getOrCreateTeam("Daniel", "Andreas MB", TeamColor.RED), getOrCreateTeam("Såndræ F", "Roar", TeamColor.BLUE), Pair(10, 7))),

            // Onsdag 25.feb
            Pair(LocalDate.of(2026, 2, 25), Triple(getOrCreateTeam("Daniel", "Lars Mikal", TeamColor.BLUE), getOrCreateTeam("Sondre FS", "Roar", TeamColor.RED), Pair(10, 2))),
            Pair(LocalDate.of(2026, 2, 25), Triple(getOrCreateTeam("Kolbein", "Håkon", TeamColor.BLUE), getOrCreateTeam("Andreas", "Anton", TeamColor.RED), Pair(10, 8))),
            Pair(LocalDate.of(2026, 2, 25), Triple(getOrCreateTeam("Aki", "Såndræ F", TeamColor.RED), getOrCreateTeam("Kolbein", "Anton", TeamColor.BLUE), Pair(10, 6))),

            // Torsdag 26.feb
            Pair(LocalDate.of(2026, 2, 26), Triple(getOrCreateTeam("Kolbein", "Carl-Trygve", TeamColor.RED), getOrCreateTeam("Sondre FS", "Anton", TeamColor.BLUE), Pair(10, 5))),
            Pair(LocalDate.of(2026, 2, 26), Triple(getOrCreateTeam("Kolbein", "Erlend", TeamColor.RED), getOrCreateTeam("Aki", "Nora", TeamColor.BLUE), Pair(10, 7))),
            Pair(LocalDate.of(2026, 2, 26), Triple(getOrCreateTeam("Daniel", "Såndræ F", TeamColor.RED), getOrCreateTeam("Aki", "Nora", TeamColor.BLUE), Pair(10, 4))),
            Pair(LocalDate.of(2026, 2, 26), Triple(getOrCreateTeam("Stian M", "Erlend", TeamColor.RED), getOrCreateTeam("Anton", "Jakob", TeamColor.BLUE), Pair(10, 5))),
            Pair(LocalDate.of(2026, 2, 26), Triple(getOrCreateTeam("Kolbein", "Andreas", TeamColor.RED), getOrCreateTeam("Aki", "Martin", TeamColor.BLUE), Pair(10, 6))),

            // Fredag 27.feb
            Pair(LocalDate.of(2026, 2, 27), Triple(getOrCreateTeam("Daniel", "Roar", TeamColor.BLUE), getOrCreateTeam("Sondre FS", "Erlend", TeamColor.RED), Pair(10, 6))),
            Pair(LocalDate.of(2026, 2, 27), Triple(getOrCreateTeam("Aki", "Anton", TeamColor.RED), getOrCreateTeam("Nora", "Eivind", TeamColor.BLUE), Pair(10, 5))),
            Pair(LocalDate.of(2026, 2, 27), Triple(getOrCreateTeam("Aki", "Stian", TeamColor.RED), getOrCreateTeam("Nora", "Eivind", TeamColor.BLUE), Pair(10, 5))),
            Pair(LocalDate.of(2026, 2, 27), Triple(getOrCreateTeam("Daniel", "Jarle", TeamColor.BLUE), getOrCreateTeam("Nora", "Roar", TeamColor.RED), Pair(10, 3))),
            Pair(LocalDate.of(2026, 2, 27), Triple(getOrCreateTeam("Daniel", "Jarle", TeamColor.BLUE), getOrCreateTeam("Aki", "Eivind", TeamColor.RED), Pair(10, 7))),

            // Mandag 02.mar
            Pair(LocalDate.of(2026, 3, 2), Triple(getOrCreateTeam("Carl-Trygve", "Marie", TeamColor.RED), getOrCreateTeam("Sondre FS", "Roar", TeamColor.BLUE), Pair(10, 9))),
            Pair(LocalDate.of(2026, 3, 2), Triple(getOrCreateTeam("Aki", "Andreas", TeamColor.BLUE), getOrCreateTeam("Sondre FS", "Roar", TeamColor.RED), Pair(10, 9))),

            // Tirsdag 03.mar
            Pair(LocalDate.of(2026, 3, 3), Triple(getOrCreateTeam("Daniel", "Såndræ F", TeamColor.BLUE), getOrCreateTeam("Anton", "Roar", TeamColor.RED), Pair(10, 3))),
            Pair(LocalDate.of(2026, 3, 3), Triple(getOrCreateTeam("Stian", "Erlend", TeamColor.RED), getOrCreateTeam("Anton", "Nora", TeamColor.BLUE), Pair(10, 9))),
            Pair(LocalDate.of(2026, 3, 3), Triple(getOrCreateTeam("Kolbein", "Carl-Trygve", TeamColor.BLUE), getOrCreateTeam("Såndræ F", "Eivind", TeamColor.RED), Pair(10, 7))),
            Pair(LocalDate.of(2026, 3, 3), Triple(getOrCreateTeam("Aki", "Stian", TeamColor.RED), getOrCreateTeam("Andreas", "Håkon", TeamColor.BLUE), Pair(10, 4))),

            // Onsdag 04.mar
            Pair(LocalDate.of(2026, 3, 4), Triple(getOrCreateTeam("Nora", "Roar", TeamColor.BLUE), getOrCreateTeam("Ragnar", "Sondre FS", TeamColor.RED), Pair(10, 9))),
            Pair(LocalDate.of(2026, 3, 4), Triple(getOrCreateTeam("Aki", "Såndræ F", TeamColor.BLUE), getOrCreateTeam("Kolbein", "Anton", TeamColor.RED), Pair(10, 6))),
            Pair(LocalDate.of(2026, 3, 4), Triple(getOrCreateTeam("Daniel", "Kolbein", TeamColor.RED), getOrCreateTeam("Sondre FS", "Carl-Trygve", TeamColor.BLUE), Pair(10, 3))),
            Pair(LocalDate.of(2026, 3, 4), Triple(getOrCreateTeam("Daniel", "Aki", TeamColor.BLUE), getOrCreateTeam("Kolbein", "Sondre FS", TeamColor.RED), Pair(10, 4))),
            Pair(LocalDate.of(2026, 3, 4), Triple(getOrCreateTeam("Håkon", "Stian", TeamColor.BLUE), getOrCreateTeam("Andreas", "Nora", TeamColor.RED), Pair(10, 4))),

            // Torsdag 05.mar
            Pair(LocalDate.of(2026, 3, 5), Triple(getOrCreateTeam("Daniel", "Herrevold", TeamColor.RED), getOrCreateTeam("Aki", "Håkon", TeamColor.BLUE), Pair(10, 6))),
            Pair(LocalDate.of(2026, 3, 5), Triple(getOrCreateTeam("Aki", "Sondre FS", TeamColor.BLUE), getOrCreateTeam("Anton", "Erlend", TeamColor.RED), Pair(10, 3))),

            // Fredag 06.mar
            Pair(LocalDate.of(2026, 3, 6), Triple(getOrCreateTeam("Sondre FS", "Roar", TeamColor.RED), getOrCreateTeam("Anton", "Ragnar", TeamColor.BLUE), Pair(10, 3))),
            Pair(LocalDate.of(2026, 3, 6), Triple(getOrCreateTeam("Daniel", "Erlend", TeamColor.RED), getOrCreateTeam("Aki", "Såndræ F", TeamColor.BLUE), Pair(10, 5))),
            Pair(LocalDate.of(2026, 3, 6), Triple(getOrCreateTeam("Daniel", "Sondre FS", TeamColor.RED), getOrCreateTeam("Aki", "Anton", TeamColor.BLUE), Pair(10, 4))),

            // Mandag 09.mar
            Pair(LocalDate.of(2026, 3, 9), Triple(getOrCreateTeam("Aki", "Såndræ F", TeamColor.BLUE), getOrCreateTeam("Nora", "Son3", TeamColor.RED), Pair(10, 3))),
            Pair(LocalDate.of(2026, 3, 9), Triple(getOrCreateTeam("Sondre FS", "Anton", TeamColor.BLUE), getOrCreateTeam("Aki", "Roar", TeamColor.RED), Pair(10, 6))),

            // Tirsdag 10.mar
            Pair(LocalDate.of(2026, 3, 10), Triple(getOrCreateTeam("Aki", "Kolbein", TeamColor.RED), getOrCreateTeam("Eivind", "Roar", TeamColor.BLUE), Pair(10, 1))),
            Pair(LocalDate.of(2026, 3, 10), Triple(getOrCreateTeam("Anton", "Carl-Trygve", TeamColor.RED), getOrCreateTeam("Daniel", "Erlend", TeamColor.BLUE), Pair(10, 9))),
            Pair(LocalDate.of(2026, 3, 10), Triple(getOrCreateTeam("Aki", "Nora", TeamColor.BLUE), getOrCreateTeam("Andreas", "Eivind", TeamColor.RED), Pair(10, 5))),

            // Onsdag 11.mar
            Pair(LocalDate.of(2026, 3, 11), Triple(getOrCreateTeam("Kolbein", "Helge", TeamColor.BLUE), getOrCreateTeam("Andreas", "Håkon", TeamColor.RED), Pair(10, 2))),

            // Mandag 16.mar
            Pair(LocalDate.of(2026, 3, 16), Triple(getOrCreateTeam("Aki", "Andreas", TeamColor.BLUE), getOrCreateTeam("Sondre FS", "Helge", TeamColor.RED), Pair(10, 9))),

            // Tirsdag 17.mar
            Pair(LocalDate.of(2026, 3, 17), Triple(getOrCreateTeam("Stian", "Eivind", TeamColor.RED), getOrCreateTeam("Kolbein", "Aki", TeamColor.BLUE), Pair(10, 8))),
            Pair(LocalDate.of(2026, 3, 17), Triple(getOrCreateTeam("Aki", "Martin", TeamColor.BLUE), getOrCreateTeam("Sondre FS", "Carl-Trygve", TeamColor.RED), Pair(10, 6))),

            // Onsdag 18.mar
            Pair(LocalDate.of(2026, 3, 18), Triple(getOrCreateTeam("Såndræ F", "Stian", TeamColor.BLUE), getOrCreateTeam("Kolbein", "Håkon", TeamColor.RED), Pair(10, 9))),
            Pair(LocalDate.of(2026, 3, 18), Triple(getOrCreateTeam("Aki", "Eivind", TeamColor.BLUE), getOrCreateTeam("Stian", "Nora", TeamColor.RED), Pair(10, 5))),

            // Torsdag 19.mar
            Pair(LocalDate.of(2026, 3, 19), Triple(getOrCreateTeam("Kolbein", "Håkon", TeamColor.RED), getOrCreateTeam("Nora", "Andreas", TeamColor.BLUE), Pair(10, 7))),
            Pair(LocalDate.of(2026, 3, 19), Triple(getOrCreateTeam("Nora", "Sondre FS", TeamColor.BLUE), getOrCreateTeam("Erlend", "Stian", TeamColor.RED), Pair(10, 8))),
            Pair(LocalDate.of(2026, 3, 19), Triple(getOrCreateTeam("Kolbein", "Andreas", TeamColor.RED), getOrCreateTeam("Aki", "Håkon", TeamColor.BLUE), Pair(10, 6))),

            // Fredag 20.mar
            Pair(LocalDate.of(2026, 3, 20), Triple(getOrCreateTeam("Nora", "Andreas", TeamColor.BLUE), getOrCreateTeam("Aki", "Roar", TeamColor.RED), Pair(10, 9))),

            // Mandag 23.mar
            Pair(LocalDate.of(2026, 3, 23), Triple(getOrCreateTeam("Aki", "Eivind", TeamColor.RED), getOrCreateTeam("Anton", "Roar", TeamColor.BLUE), Pair(10, 9))),
            Pair(LocalDate.of(2026, 3, 23), Triple(getOrCreateTeam("Anton", "Helge", TeamColor.BLUE), getOrCreateTeam("Aki", "Roar", TeamColor.RED), Pair(10, 7))),

            // Tirsdag 24.mar
            Pair(LocalDate.of(2026, 3, 24), Triple(getOrCreateTeam("Såndræ F", "Stian", TeamColor.RED), getOrCreateTeam("Anton", "Håkon", TeamColor.BLUE), Pair(10, 4))),
            Pair(LocalDate.of(2026, 3, 24), Triple(getOrCreateTeam("Såndræ F", "Anton", TeamColor.RED), getOrCreateTeam("Aki", "Erlend", TeamColor.BLUE), Pair(10, 2))),

            // Onsdag 25.mar
            Pair(LocalDate.of(2026, 3, 25), Triple(getOrCreateTeam("Nora", "Anton", TeamColor.RED), getOrCreateTeam("Sondre FS", "Roar", TeamColor.BLUE), Pair(10, 8))),
            Pair(LocalDate.of(2026, 3, 25), Triple(getOrCreateTeam("Anton", "Stian", TeamColor.BLUE), getOrCreateTeam("Aki", "Nora", TeamColor.RED), Pair(10, 8))),
            Pair(LocalDate.of(2026, 3, 25), Triple(getOrCreateTeam("Aki", "Håkon", TeamColor.BLUE), getOrCreateTeam("Nora", "Eivind", TeamColor.RED), Pair(10, 6))),

            // Torsdag 26.mar
            Pair(LocalDate.of(2026, 3, 26), Triple(getOrCreateTeam("Nora", "Eivind", TeamColor.RED), getOrCreateTeam("Aki", "Anton", TeamColor.BLUE), Pair(10, 7))),
            Pair(LocalDate.of(2026, 3, 26), Triple(getOrCreateTeam("Aki", "Sondre FS", TeamColor.RED), getOrCreateTeam("Anton", "Roar", TeamColor.BLUE), Pair(10, 7))),

            // Fredag 27.mar
            Pair(LocalDate.of(2026, 3, 27), Triple(getOrCreateTeam("Anton", "Sondre FS", TeamColor.BLUE), getOrCreateTeam("Aki", "Nora", TeamColor.RED), Pair(10, 6))),
            Pair(LocalDate.of(2026, 3, 27), Triple(getOrCreateTeam("Nora", "Øystein", TeamColor.BLUE), getOrCreateTeam("Aki", "Erlend", TeamColor.RED), Pair(10, 6))),

            // Tirsdag 31.mar
            Pair(LocalDate.of(2026, 3, 31), Triple(getOrCreateTeam("Kolbein", "Håkon", TeamColor.RED), getOrCreateTeam("Aki", "Andreas", TeamColor.BLUE), Pair(10, 9))),
            Pair(LocalDate.of(2026, 3, 31), Triple(getOrCreateTeam("Kolbein", "Håkon", TeamColor.RED), getOrCreateTeam("Såndræ F", "Andreas", TeamColor.BLUE), Pair(10, 4))),
            Pair(LocalDate.of(2026, 3, 31), Triple(getOrCreateTeam("Daniel", "Håkon", TeamColor.RED), getOrCreateTeam("Sondre FS", "Andreas", TeamColor.BLUE), Pair(10, 2))),
            Pair(LocalDate.of(2026, 3, 31), Triple(getOrCreateTeam("Kolbein", "Aki", TeamColor.RED), getOrCreateTeam("Sondre FS", "Håkon", TeamColor.BLUE), Pair(10, 3))),

            // Tirsdag 07.apr
            Pair(LocalDate.of(2026, 4, 7), Triple(getOrCreateTeam("Kolbein", "Roar", TeamColor.RED), getOrCreateTeam("Såndræ F", "Håkon", TeamColor.BLUE), Pair(10, 9))),
            Pair(LocalDate.of(2026, 4, 7), Triple(getOrCreateTeam("Aki", "Håkon", TeamColor.RED), getOrCreateTeam("Roar", "Carl-Trygve", TeamColor.BLUE), Pair(10, 6))),

            // Onsdag 08.apr
            Pair(LocalDate.of(2026, 4, 8), Triple(getOrCreateTeam("Sondre FS", "Roar", TeamColor.BLUE), getOrCreateTeam("Kolbein", "Håkon", TeamColor.RED), Pair(10, 8))),
            Pair(LocalDate.of(2026, 4, 8), Triple(getOrCreateTeam("Aki", "Roar", TeamColor.RED), getOrCreateTeam("Kolbein", "Nora", TeamColor.BLUE), Pair(10, 8))),
            Pair(LocalDate.of(2026, 4, 8), Triple(getOrCreateTeam("Aki", "Stian", TeamColor.RED), getOrCreateTeam("Håkon", "Sondre FS", TeamColor.BLUE), Pair(10, 5))),

            // Torsdag 09.apr
            Pair(LocalDate.of(2026, 4, 9), Triple(getOrCreateTeam("Daniel", "Nora", TeamColor.BLUE), getOrCreateTeam("Aki", "Eivind", TeamColor.RED), Pair(10, 1))),
            Pair(LocalDate.of(2026, 4, 9), Triple(getOrCreateTeam("Aki", "Kolbein", TeamColor.RED), getOrCreateTeam("Håkon", "Sondre FS", TeamColor.BLUE), Pair(10, 5))),
            Pair(LocalDate.of(2026, 4, 9), Triple(getOrCreateTeam("Daniel", "Sondre FS", TeamColor.RED), getOrCreateTeam("Aki", "Kolbein", TeamColor.BLUE), Pair(10, 8))),

            // Fredag 10.apr
            Pair(LocalDate.of(2026, 4, 10), Triple(getOrCreateTeam("Kolbein", "Roar", TeamColor.RED), getOrCreateTeam("Stian", "Lotte", TeamColor.BLUE), Pair(10, 4))),
            Pair(LocalDate.of(2026, 4, 10), Triple(getOrCreateTeam("Kolbein", "Erlend", TeamColor.RED), getOrCreateTeam("Aki", "Sondre FS", TeamColor.BLUE), Pair(10, 7))),
            Pair(LocalDate.of(2026, 4, 10), Triple(getOrCreateTeam("Kolbein", "Aki", TeamColor.BLUE), getOrCreateTeam("Daniel", "Roar", TeamColor.RED), Pair(10, 7))),

            // Mandag 13.apr
            Pair(LocalDate.of(2026, 4, 13), Triple(getOrCreateTeam("Eivind", "Hege", TeamColor.BLUE), getOrCreateTeam("Aki", "Roar", TeamColor.RED), Pair(10, 9))),
            Pair(LocalDate.of(2026, 4, 13), Triple(getOrCreateTeam("Eivind", "Aki", TeamColor.RED), getOrCreateTeam("Sondre FS", "Roar", TeamColor.BLUE), Pair(10, 7))),

            // Tirsdag 14.apr
            Pair(LocalDate.of(2026, 4, 14), Triple(getOrCreateTeam("Kolbein", "Aki", TeamColor.BLUE), getOrCreateTeam("Såndræ F", "Eivind", TeamColor.RED), Pair(10, 8))),
            Pair(LocalDate.of(2026, 4, 14), Triple(getOrCreateTeam("Eivind", "Stian", TeamColor.RED), getOrCreateTeam("Aki", "Håkon", TeamColor.BLUE), Pair(10, 8))),

            // Onsdag 15.apr
            Pair(LocalDate.of(2026, 4, 15), Triple(getOrCreateTeam("Nora", "Stian", TeamColor.RED), getOrCreateTeam("Jarle", "Dan-Eric", TeamColor.BLUE), Pair(10, 5))),
            Pair(LocalDate.of(2026, 4, 15), Triple(getOrCreateTeam("Kolbein", "Håkon", TeamColor.RED), getOrCreateTeam("Aki", "Eivind", TeamColor.BLUE), Pair(10, 6))),
            Pair(LocalDate.of(2026, 4, 15), Triple(getOrCreateTeam("Daniel", "Carl-Trygve", TeamColor.RED), getOrCreateTeam("Sondre FS", "Roar", TeamColor.BLUE), Pair(10, 1))),

            // Torsdag 16.apr
            Pair(LocalDate.of(2026, 4, 16), Triple(getOrCreateTeam("Såndræ F", "Sondre FS", TeamColor.BLUE), getOrCreateTeam("Daniel", "Nora", TeamColor.RED), Pair(10, 7))),
            Pair(LocalDate.of(2026, 4, 16), Triple(getOrCreateTeam("Kolbein", "Eivind", TeamColor.BLUE), getOrCreateTeam("Aki", "Andreas", TeamColor.RED), Pair(10, 9))),
            Pair(LocalDate.of(2026, 4, 16), Triple(getOrCreateTeam("Kolbein", "Aki", TeamColor.BLUE), getOrCreateTeam("Nora", "Eivind", TeamColor.RED), Pair(10, 6))),

            // Fredag 17.apr
            Pair(LocalDate.of(2026, 4, 17), Triple(getOrCreateTeam("Roar", "Stian", TeamColor.BLUE), getOrCreateTeam("Aki", "Erlend", TeamColor.RED), Pair(10, 8))),
            Pair(LocalDate.of(2026, 4, 17), Triple(getOrCreateTeam("Roar", "Carl-Trygve", TeamColor.BLUE), getOrCreateTeam("Nora", "Eivind", TeamColor.RED), Pair(10, 9))),

            // Mandag 20.apr
            Pair(LocalDate.of(2026, 4, 20), Triple(getOrCreateTeam("Anton", "Eivind", TeamColor.RED), getOrCreateTeam("Nora", "Helge", TeamColor.BLUE), Pair(10, 8))),
            Pair(LocalDate.of(2026, 4, 20), Triple(getOrCreateTeam("Aki", "Eivind", TeamColor.RED), getOrCreateTeam("Nora", "Stian", TeamColor.BLUE), Pair(10, 4))),

            // Tirsdag 21.apr
            Pair(LocalDate.of(2026, 4, 21), Triple(getOrCreateTeam("Såndræ F", "Håkon", TeamColor.RED), getOrCreateTeam("Nora", "Eivind", TeamColor.BLUE), Pair(10, 6))),
            Pair(LocalDate.of(2026, 4, 21), Triple(getOrCreateTeam("Aki", "Anton", TeamColor.BLUE), getOrCreateTeam("Kolbein", "Roar", TeamColor.RED), Pair(10, 9))),

            // Onsdag 23.apr
            Pair(LocalDate.of(2026, 4, 23), Triple(getOrCreateTeam("Daniel", "Håkon", TeamColor.BLUE), getOrCreateTeam("Kolbein", "Aki", TeamColor.RED), Pair(10, 8))),
            Pair(LocalDate.of(2026, 4, 23), Triple(getOrCreateTeam("Kolbein", "Nora", TeamColor.RED), getOrCreateTeam("Eivind", "Aki", TeamColor.BLUE), Pair(10, 5))),
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
