package no.ks_digital.foos.entity

import jakarta.persistence.*

@Entity
@Table(name = "team")
data class Team(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val teamId: Long? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "offense_id", nullable = false)
    val offense: Player = Player(),

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "defense_id", nullable = false)
    val defense: Player = Player(),

    @Column(length = 5)
    @Enumerated(EnumType.STRING)
    val teamColor: TeamColor
)
