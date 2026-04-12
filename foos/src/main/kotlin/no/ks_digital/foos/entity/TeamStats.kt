package no.ks_digital.foos.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "team_stats")
data class TeamStats(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val teamStatsId: Long = 0,
    @ManyToOne @JoinColumn(name = "player1_id", nullable = false)
    val player1: Player,
    @ManyToOne @JoinColumn(name = "player2_id", nullable = false)
    val player2: Player,
    val totalMatches: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val draws: Int = 0,
    val winRate: Double = 0.0,
    val preferredColor: String? = null,
    val currentWinStreak: Int = 0,
    val longestWinStreak: Int = 0,
    val averageScoreDifference: Double = 0.0,
    val totalPointsScored: Int = 0,
    val totalPointsAllowed: Int = 0,
    val avgPointsScoredPerMatch: Double = 0.0,
    val avgPointsAllowedPerMatch: Double = 0.0,
    val matchesAsRed: Int = 0,
    val winsAsRed: Int = 0,
    val matchesAsBlue: Int = 0,
    val winsAsBlue: Int = 0,
    val colorWinRateRed: Double = 0.0,
    val colorWinRateBlue: Double = 0.0,
    val avgMatchesPerWeek: Double = 0.0,
    // Current Elo — full history is in TeamRating
    val eloRating: Double = 1600.0,
    val lastPlayed: LocalDateTime = LocalDateTime.now(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

