package io.basquiat.musicstore.album.service

import io.basquiat.musicstore.common.extensions.findByIdOrThrow
import io.basquiat.musicstore.common.util.mandatoryParam
import io.basquiat.musicstore.album.domain.dto.AlbumDto
import io.basquiat.musicstore.album.domain.entity.Album
import io.basquiat.musicstore.album.repository.AlbumRepository
import io.basquiat.musicstore.musician.domain.dto.MusicianDto
import io.basquiat.musicstore.musician.domain.vo.AlbumRequest
import io.basquiat.musicstore.musician.service.MusicianService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 앨범 정보를 다루는 서비스 객체
 * created by basquiat
 */
@Service
class AlbumService(
    private val albumRepository: AlbumRepository,
    private val musicianService: MusicianService,
) {

    /**
     * 앨범 리스트를 가져온다.
     * @return Page<AlbumDto> 앨범 리스트
     */
    @Transactional(readOnly = true)
    fun fetchAlbums(pageable: Pageable): Page<AlbumDto> = albumRepository.fetchAllAlbum(pageable)
                                                                            .map(AlbumDto::create)

    /**
     * 특정 뮤지션의 앨범 리스트를 가져온다.
     * @return Page<AlbumDto> 앨범 리스트
     */
    @Transactional(readOnly = true)
    fun fetchAlbumsByMusicianId(musicianId: Long, pageable: Pageable): Page<AlbumDto> {
        val musician = musicianService.fetchMusicianById(musicianId)
        return albumRepository.findByMusicianId(musicianId, pageable)
                              .map { entity -> AlbumDto.createWithMusician(entity, musician) }
    }


    /**
     * 타이틀로 앨범 리스트를 가져온다.
     * 동명 타이틀의 음반이 많을 수 있기 때문에 리스트로 가져온다.
     * @return Page<AlbumDto> 앨범 리스트
     */
    @Transactional(readOnly = true)
    fun fetchAlbumsByTitle(title: String, pageable: Pageable): Page<AlbumDto> = albumRepository.findByTitle(title, pageable)
                                                                                               .map(AlbumDto::create)

    /**
     * 앨범 정보를 db에 생성한다.
     * @return AlbumDto
     */
    @Transactional
    fun createAlbum(request: AlbumRequest): AlbumDto {
        val musicianId = request.musicianId ?: mandatoryParam("뮤지션 아이디는 필수 입니다.")
        val targetMusician = musicianService.findMusicianById(musicianId)
        val createAlbum = with(request){Album(musician = targetMusician, title = title, albumType = albumType, rep = rep )}
        albumRepository.save(createAlbum)
        return AlbumDto.createWithMusician(createAlbum, MusicianDto.create(targetMusician))
    }

    /**
     * 앨범 정보를 변경한다.
     * @return AlbumDto
     */
    @Transactional
    fun updateAlbum(request: AlbumRequest): AlbumDto {
        val albumId = request.id ?: mandatoryParam("앨범 아이디는 필수 입니다.")
        val updateAlbum = albumRepository.findByIdOrThrow(albumId)
        updateAlbum.title = request.title
        return AlbumDto.create(updateAlbum)
    }

}