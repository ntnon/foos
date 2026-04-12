package no.ks_digital.foos.entity

enum class TeamColor(val value: String) {
    RED("R"),
    BLUE("B");

    companion object {
        fun fromValue(value: String): TeamColor? = entries.find { it.value == value }
    }
}

