package io.basquiat.musicstore.musician.domain.vo

import io.basquiat.musicstore.musician.domain.code.GenreCode

/**
 * 뮤지션 생성 및 변경을 위한 request 객체
 */
data class MusicianRequest(
    val name: String,
    val genre: GenreCode? = GenreCode.ETC,
    val id: Long? = null
)