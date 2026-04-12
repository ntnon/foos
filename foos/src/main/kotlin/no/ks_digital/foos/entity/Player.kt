package no.ks_digital.foos.entity

import jakarta.persistence.*

@Entity
@Table(name = "player")
data class Player(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val playerId: Long? = null,

    @Column(nullable = false, unique = true)
    val name: String = ""
)
