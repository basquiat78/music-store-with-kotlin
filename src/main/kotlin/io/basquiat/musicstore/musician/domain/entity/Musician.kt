package io.basquiat.musicstore.musician.domain.entity

import io.basquiat.common.exception.MandatoryArgumentException
import io.basquiat.musicstore.musician.domain.code.GenreCode
import javax.persistence.*

/**
 * created by basquiat
 */
@Entity
@Table(name = "musician")
class Musician(

    var name: String? = null,

    @Enumerated(EnumType.STRING)
    var genre: GenreCode?,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long?,

) {

    init {
        if(name == null) {
            throw MandatoryArgumentException("뮤지션 이름은 필수 입니다.")
        }
    }

    data class Builder(
        var name: String? = null,
        var genre: GenreCode? = GenreCode.ETC,
        var id: Long? = null,
    ) {
        fun name(name: String) = apply { this.name = name }
        fun genre(genre: GenreCode) = apply { this.genre = genre }
        fun id(id: Long) = apply { this.id = id }
        fun build() = Musician(name, genre, id)
    }

}