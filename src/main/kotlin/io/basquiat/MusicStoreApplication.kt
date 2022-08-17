package io.basquiat

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MusicStoreApplication

fun main(args: Array<String>) {
    runApplication<MusicStoreApplication>(*args)
}