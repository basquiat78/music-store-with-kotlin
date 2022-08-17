package io.basquiat.musicstore.musician.service

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
    @DisplayName("뮤지션 정보를 가져오는 메소도를 테스트한다.")
    fun fetchMusicians_TEST() {
        val musicians = musicianService.fetchMusicians()
        assertThat(musicians).hasSize(2)
    }




}