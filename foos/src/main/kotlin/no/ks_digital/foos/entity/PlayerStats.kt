package no.ks_digital.foos.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "player_stats")
data class PlayerStats(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val playerStatsId: Long = 0,
    @OneToOne @JoinColumn(name = "player_id", nullable = false, unique = true)
    val player: Player,
    val totalMatches: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val draws: Int = 0,
    val winRate: Double = 0.0,
    val bestPosition: String? = null,
    val mostWinningColor: String? = null,
    val currentWinStreak: Int = 0,
    val longestWinStreak: Int = 0,
    val matchesAsOffense: Int = 0,
    val winsAsOffense: Int = 0,
    val matchesAsDefense: Int = 0,
    val winsAsDefense: Int = 0,
    val positionWinRateOffense: Double = 0.0,
    val positionWinRateDefense: Double = 0.0,
    val matchesAsRed: Int = 0,
    val winsAsRed: Int = 0,
    val matchesAsBlue: Int = 0,
    val winsAsBlue: Int = 0,
    val colorWinRateRed: Double = 0.0,
    val colorWinRateBlue: Double = 0.0,
    // Current Elo — full history is in PlayerRating
    val eloRating: Double = 1600.0,
    val lastPlayed: LocalDateTime = LocalDateTime.now(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
