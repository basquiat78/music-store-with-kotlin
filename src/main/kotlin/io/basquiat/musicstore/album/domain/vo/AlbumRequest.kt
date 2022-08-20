package io.basquiat.musicstore.musician.domain.vo

import io.basquiat.musicstore.common.domain.YesOrNo
import io.basquiat.musicstore.album.domain.code.AlbumType

/**
 * 앨범 생성 및 변경을 위한 request 객체
 */
data class AlbumRequest(

    val title: String,
    val rep: YesOrNo? = YesOrNo.N,
    val albumType: AlbumType? = AlbumType.FULL,
    val musicianId: Long? = null,
    val id: Long? = null,

)