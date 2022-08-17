package io.basquiat

import io.basquiat.common.util.convertToObject
import io.basquiat.common.util.toJson
import io.basquiat.common.util.typeRef
import io.basquiat.musicstore.musician.domain.code.GenreCode
import io.basquiat.musicstore.musician.domain.entity.Musician
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

    @Test
    @DisplayName("objectMapper single")
    fun objectMapperSingle_TEST() {
        val obj = Musician(id = 1, name =  "test", genre = GenreCode.JAZZ)
        val json = toJson(obj)
        println(json)
        val convertObj = convertToObject(json, Musician::class.java)
        with(convertObj) {
            println(id)
            println(name)
            println(genre)
        }
    }

    @Test
    @DisplayName("objectMapper type")
    fun objectMapperList_TEST() {
        val list = listOf(Musician(id = 1, name =  "test111", genre = GenreCode.JAZZ), Musician(id = 2, name =  "test222", genre = GenreCode.JAZZ))
        val json = toJson(list)
        println(json)
        val convertToObj = convertToObject(json, typeRef(Musician::class.java))
        println(convertToObj)
    }

}