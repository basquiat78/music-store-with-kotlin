package io.basquiat.musicstore.musician.service

import io.basquiat.common.extensions.findByIdOrThrow
import io.basquiat.musicstore.musician.domain.code.GenreCode
import io.basquiat.musicstore.musician.domain.dto.MusicianDto
import io.basquiat.musicstore.musician.repository.MusicianRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 뮤지션의 정보를 다루는 서비스 객체
 * created by basquiat
 */
@Service
class MusicianService(
    private val musicianRepository: MusicianRepository
) {

    /**
     * 뮤지션의 정보를 가져온다.
     * @return List<MusicianDto> 뮤지션 리스트
     */
    @Transactional(readOnly = true)
    fun fetchMusicians() = musicianRepository.findAll()
                                             .map(MusicianDto::create)

    /**
     * 뮤지션 아이디로 해당 뮤지션 정보를 가져온다.
     * @return MusicianDto
     */
    @Transactional(readOnly = true)
    fun fetchMusicianById(id: Long, message: String? = null) = musicianRepository.findByIdOrThrow(id, message)
                                                                                 .let {MusicianDto.create(it)}

    /**
     * 뮤지션 이름으로 뮤지션 정보를 가져온다.
     * @return MusicianDto?
     */
    @Transactional(readOnly = true)
    fun fetchMusicianByName(name: String) = musicianRepository.findByName(name)?.let { MusicianDto.create(it) }

    /**
     * 장르로 이에 해당하는 뮤지션 리스트를 가져온다.
     * @return List<MusicianDto>
     */
    @Transactional(readOnly = true)
    fun fetchMusiciansByGenre(genre: GenreCode) = musicianRepository.findByGenre(genre)
                                                                    .map(MusicianDto::create)

}