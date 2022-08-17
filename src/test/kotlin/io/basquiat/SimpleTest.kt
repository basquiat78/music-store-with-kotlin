package io.basquiat

import io.basquiat.musicstore.musician.domain.code.GenreCode
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class SimpleTest {

    @Test
    @DisplayName("enum test")
    fun enum_TEST() {
        //val genre = "one"
        val genre = "Jazz"
        println(GenreCode.of(genre))
    }

}