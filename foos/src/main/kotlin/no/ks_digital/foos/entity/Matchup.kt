package no.ks_digital.foos.entity

import jakarta.persistence.*

@Entity
@Table(name = "matchup", uniqueConstraints = [UniqueConstraint(columnNames = ["player1a_id", "player1b_id", "player2a_id", "player2b_id"])])
data class Matchup(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val matchupId: Long? = null,

    // Pair 1 — players are stored with lower ID first to ensure consistent ordering
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player1a_id", nullable = false)
    val player1a: Player = Player(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player1b_id", nullable = false)
    val player1b: Player = Player(),

    // Pair 2
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player2a_id", nullable = false)
    val player2a: Player = Player(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player2b_id", nullable = false)
    val player2b: Player = Player(),

    @Column(nullable = false)
    val pair1Wins: Int = 0,

    @Column(nullable = false)
    val pair2Wins: Int = 0,

    @Column(nullable = false)
    val draws: Int = 0
)
