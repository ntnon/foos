package no.ks_digital.foos

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FoosApplication

fun main(args: Array<String>) {
	runApplication<FoosApplication>(*args)
}
