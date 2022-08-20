package io.basquiat.musicstore.album.service

import io.basquiat.musicstore.album.domain.dto.AlbumDto
import io.basquiat.musicstore.album.repository.AlbumRepository
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
    fun fetchMusicians(pageable: Pageable): Page<AlbumDto> = albumRepository.fetchAllAlbum(pageable)
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
}