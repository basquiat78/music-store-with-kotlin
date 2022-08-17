package io.basquiat.musicstore.musician.domain.entity

import io.basquiat.common.exception.MandatoryArgumentException
import io.basquiat.musicstore.musician.domain.code.GenreCode
import org.hibernate.annotations.DynamicUpdate
import javax.persistence.*

/**
 * created by basquiat
 */
@Entity
@Table(name = "musician")
class Musician(

    var name: String,

    @Enumerated(EnumType.STRING)
    var genre: GenreCode? = GenreCode.ETC,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    ) {

    init {
        if(name.isBlank()) {
            throw MandatoryArgumentException("뮤지션 이름은 필수 입니다.")
        }
    }

}