package no.ks_digital.foos.entity

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * Elo rating snapshot for a pair (team) at the time of a specific match.
 * One record per team per match — gives full pair rating history.
 */
@Entity
@Table(name = "team_rating")
data class TeamRating(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val teamRatingId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_match_id", nullable = false)
    val teamMatch: TeamMatch,

    @Column(nullable = false)
    val ratingBefore: Double,

    @Column(nullable = false)
    val ratingAfter: Double,

    @Column(nullable = false)
    val ratingChange: Double,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

