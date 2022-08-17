package io.basquiat.musicstore.musician.domain.dto

import io.basquiat.musicstore.musician.domain.code.GenreCode
import io.basquiat.musicstore.musician.domain.entity.Musician

/**
 * musicianDTO
 * created by basquiat
 */
data class MusicianDto(
    val id: Long,
    val name: String,
    private var _genreCode: GenreCode,
) {
    /**
     * 클라이언트로 정보를 내려줄 때는 code값으로 보내주기 위해 backing property를 사용
     */
    val genre: String get() = this._genreCode.genre

    companion object {
        /**
         * @param musician
         * @return MusicianDto
         */
        fun create(musician: Musician): MusicianDto = with(musician) {
            MusicianDto(name =  name!!, id = id!!, _genreCode = genre!!)
        }
        /*
        fun create(musician: Musician): MusicianDto {
            return MusicianDto(musician.id!!, musician.name!!, musician.genre!!)
        }
        */
    }

}