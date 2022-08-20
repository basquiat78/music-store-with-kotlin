package io.basquiat.musicstore.album.domain.entity

import io.basquiat.musicstore.common.domain.YesOrNo
import io.basquiat.musicstore.common.exception.MandatoryArgumentException
import io.basquiat.musicstore.album.domain.code.AlbumType
import io.basquiat.musicstore.musician.domain.entity.Musician
import org.hibernate.annotations.DynamicUpdate
import javax.persistence.*

/**
 * created by basquiat
 */
@Entity
@DynamicUpdate
@Table(name = "album")
class Album(

    @Convert(converter = YesOrNo.LowCaseConverter::class)
    var rep: YesOrNo? = YesOrNo.N,

    @Enumerated(EnumType.STRING)
    @Column(name = "album_type")
    var albumType: AlbumType? = AlbumType.FULL,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "musician_id")
    var musician: Musician,

    var title: String,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

) {
    init {
        if(title.isBlank()) {
            throw MandatoryArgumentException("앨범 명은 필수입니다.")
        }
    }
}