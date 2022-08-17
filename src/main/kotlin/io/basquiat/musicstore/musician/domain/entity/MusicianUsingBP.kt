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
class MusicianUsingBP(

    private var _name: String?,
    var genre: GenreCode?,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long?,

) {

    init {
        if(_name == null) {
            throw MandatoryArgumentException("뮤지션 이름은 필수 입니다.")
        }
    }

    val name get() = this._name

    fun changeName(name: String) {
        this._name = name
    }

    data class Builder(
        var name: String? = null,
        var genre: GenreCode? = GenreCode.ETC,
        var id: Long? = null
    ) {
        fun name(name: String) = apply { this.name = name }
        fun genre(genre: GenreCode) = apply { this.genre = genre }
        fun id(id: Long) = apply { this.id = id }
        fun build() = MusicianUsingBP(name, genre, id)
    }

}