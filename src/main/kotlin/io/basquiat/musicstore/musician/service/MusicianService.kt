package io.basquiat.musicstore.musician.service

import io.basquiat.musicstore.common.extensions.findByIdOrThrow
import io.basquiat.musicstore.common.util.mandatoryParam
import io.basquiat.musicstore.musician.domain.code.GenreCode
import io.basquiat.musicstore.musician.domain.dto.MusicianDto
import io.basquiat.musicstore.musician.domain.entity.Musician
import io.basquiat.musicstore.musician.domain.vo.MusicianRequest
import io.basquiat.musicstore.musician.repository.MusicianRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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
     *
     * 뮤지션 아이디로 해당 뮤지션 정보를 가져온다.
     * @return MusicianDto
     */
    @Transactional(readOnly = true)
    fun findMusicianById(id: Long, message: String? = null): Musician = musicianRepository.findByIdOrThrow(id, message)

    /**
     * 뮤지션의 정보를 가져온다.
     * @return Page<MusicianDto> 뮤지션 리스트
     */
    @Transactional(readOnly = true)
    fun fetchMusicians(pageable: Pageable): Page<MusicianDto> = musicianRepository.findAll(pageable)
                                                                                  .map(MusicianDto::create)

    /**
     *
     * 뮤지션 아이디로 해당 뮤지션 정보를 가져온다.
     * @return MusicianDto
     */
    @Transactional(readOnly = true)
    fun fetchMusicianById(id: Long, message: String? = null) = findMusicianById(id, message).let {MusicianDto.create(it)}

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

    /**
     * 뮤지션 정보를 db에 생성한다.
     * @return MusicianDto
     */
    @Transactional
    fun createMusician(request: MusicianRequest): MusicianDto {
        val createMusician = with(request){Musician(name = name, genre = genre)}
        return MusicianDto.create(musicianRepository.save(createMusician))
    }

    /**
     * 뮤지션 정보를 변경한다.
     */
    @Transactional
    fun updateMusician(request: MusicianRequest): MusicianDto {
        val musicianId = request.id ?: mandatoryParam("변경할 뮤지션의 id는 필수 입니다.");
        val selectMusician = musicianRepository.findByIdOrThrow(musicianId)
        selectMusician.name = request.name
        return MusicianDto.create(selectMusician)
    }

}