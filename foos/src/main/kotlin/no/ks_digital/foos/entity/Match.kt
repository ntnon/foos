package no.ks_digital.foos.entity

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "match")
data class MatchEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val matchId: Long? = null,

    @Column(nullable = false)
    val matchDate: LocalDate = LocalDate.now(),

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team1_id", nullable = false)
    val team1: Team,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team2_id", nullable = false)
    val team2: Team,

    @Column(nullable = false)
    val team1GameScore: Int = 0,

    @Column(nullable = false)
    val team2GameScore: Int = 0
)
