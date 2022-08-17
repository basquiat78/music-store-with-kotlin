package io.basquiat.musicstore.musician.service

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
     * @return List<Musician> 뮤지션 리스트
     */
    @Transactional(readOnly = true)
    fun fetchMusicians(): List<MusicianDto> {
        return musicianRepository.findAll()
                                 .map {entity -> MusicianDto.create(entity)}
                                 //.map(MusicianDto.Companion::create)

    }

}