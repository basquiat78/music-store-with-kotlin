package io.basquiat.musicstore.album.domain.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.basquiat.musicstore.common.domain.YesOrNo
import io.basquiat.musicstore.album.domain.code.AlbumType
import io.basquiat.musicstore.album.domain.entity.Album
import io.basquiat.musicstore.musician.domain.dto.MusicianDto

/**
 * AlbumDto
 * created by basquiat
 */
data class AlbumDto(

    val id: Long? = null,

    val musicianName: String,

    val title: String,

    @JsonProperty("albumType")
    private var _albumType: AlbumType,

    @JsonProperty("representative")
    private var _representative: YesOrNo,
) {
    /**
     * 클라이언트로 정보를 내려줄 때는 albumType의 설명을 보내주기 위해 backing property를 사용
     */
    val albumType: String get() = this._albumType.description

    val representative: String get() = this._representative.name

    companion object {
        /**
         * @param album
         * @param musician
         * @return AlbumDto
         */
        fun createWithMusician(album: Album, musician: MusicianDto) = with(album) {
            AlbumDto(title = title,
                     id = id,
                     _albumType = albumType?: AlbumType.FULL,
                     musicianName = musician.name,
                     _representative = rep?: YesOrNo.N,
            )
        }

        /**
         * @param album
         * @return AlbumDto
         */
        fun create(album: Album) = with(album) {
            AlbumDto(title = title,
                     id = id,
                     _albumType = albumType?: AlbumType.FULL,
                     _representative = rep?: YesOrNo.N,
                     musicianName = musician.name
            )
        }
    }

}