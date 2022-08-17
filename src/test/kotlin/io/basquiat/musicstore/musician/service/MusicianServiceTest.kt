package io.basquiat.musicstore.musician.service

import io.basquiat.musicstore.musician.domain.code.GenreCode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class MusicianServiceTest @Autowired constructor(
    private val musicianService: MusicianService,
) {

    @Test
    @DisplayName("뮤지션 정보를 가져오는 메소드를 테스트한다.")
    fun fetchMusicians_TEST() {
        val musicians = musicianService.fetchMusicians()
        assertThat(musicians).hasSize(2)
    }

    @Test
    @DisplayName("아이디로 뮤지션 정보를 가져오는 메소드를 테스트한다.")
    fun fetchMusicianById_TEST() {
        val musicianId = 1L
        val musician = musicianService.fetchMusicianById(musicianId)
        assertThat(musician.name).isEqualTo("Charlie Parker")

        val otherMusicianId = 1_111L
        val otherMusician = musicianService.fetchMusicianById(otherMusicianId, "어아다 ${otherMusicianId}로 조회된 뮤지션 정보가 없습니다. 아이디를 확인해 보세요.")
        assertThat(otherMusician.name).isEqualTo("Charlie Parker")

    }

    @Test
    @DisplayName("이름으로 뮤지션 리스트를 가져오는 메소드를 테스트한다.")
    fun fetchMusiciansByName_TEST() {
        val musicianName = "Charlie Parker"
        val musician = musicianService.fetchMusicianByName(musicianName)
        assertThat(musician?.name).isEqualTo("Charlie Parker")
    }

    @Test
    @DisplayName("장르로 해당 뮤지션 리스트를 가져오는 메소드를 테스트한다.")
    fun fetchMusiciansByGenre_TEST() {
        val genre = GenreCode.JAZZ
        val musicians = musicianService.fetchMusiciansByGenre(genre)
        assertThat(musicians).hasSize(2)
        assertThat(musicians[0].name).isEqualTo("Charlie Parker")
        assertThat(musicians[1].name).isEqualTo("Charlie Parker") // 의도적 에러
    }

}