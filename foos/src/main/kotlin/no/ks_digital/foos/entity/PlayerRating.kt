package no.ks_digital.foos.entity

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * Elo rating snapshot for a player at the time of a specific match.
 * One record per player per match — gives full rating history traceable to individual matches.
 */
@Entity
@Table(name = "player_rating")
data class PlayerRating(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val playerRatingId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_match_id", nullable = false)
    val playerMatch: PlayerMatch,

    @Column(nullable = false)
    val ratingBefore: Double,

    @Column(nullable = false)
    val ratingAfter: Double,

    @Column(nullable = false)
    val ratingChange: Double,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
