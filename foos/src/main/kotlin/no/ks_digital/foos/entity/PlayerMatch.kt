package no.ks_digital.foos.entity

import jakarta.persistence.*

/**
 * Junction table: records a player's participation in a specific match.
 * Used for many-to-many between Player and Match, and as the anchor for PlayerRating.
 */
@Entity
@Table(name = "player_match")
data class PlayerMatch(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val playerMatchId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    val player: Player,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    val match: MatchEntity,

    /** Which team this player was on in this match */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val teamColor: TeamColor,

    /** Which position this player occupied */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val position: Position,

    /** Did this player's team win? */
    @Column(nullable = false)
    val won: Boolean
)
