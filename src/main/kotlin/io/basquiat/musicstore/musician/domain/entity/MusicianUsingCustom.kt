package io.basquiat.musicstore.musician.domain.entity

import io.basquiat.common.exception.MandatoryArgumentException
import io.basquiat.musicstore.musician.domain.code.GenreCode
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

/**
 * Backing property를 사용한 테스트용 엔티티
 */
@Entity
class MusicianUsingCustom(

    name: String?,
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

    var name: String? = name
        protected set

    fun changeName(name: String) {
        this.name = name
    }

    data class Builder(
        var name: String? = null,
        var genre: GenreCode? = GenreCode.ETC,
        var id: Long? = null
    ) {
        fun name(name: String) = apply { this.name = name }
        fun genre(genre: GenreCode) = apply { this.genre = genre }
        fun id(id: Long) = apply { this.id = id }
        fun build() = MusicianUsingCustom(name, genre, id)
    }

}