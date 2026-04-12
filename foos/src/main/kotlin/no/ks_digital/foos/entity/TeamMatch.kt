package no.ks_digital.foos.entity

import jakarta.persistence.*

/**
 * Junction table: records a team's participation in a specific match.
 * Used for many-to-many between Team and Match, and as the anchor for TeamRating.
 */
@Entity
@Table(name = "team_match")
data class TeamMatch(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val teamMatchId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    val team: Team,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    val match: MatchEntity,

    @Column(nullable = false)
    val won: Boolean,

    @Column(nullable = false)
    val gameScore: Int
)

