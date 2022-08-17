package io.basquiat

import io.basquiat.musicstore.musician.domain.code.GenreCode
import io.basquiat.musicstore.musician.domain.entity.Musician
import io.basquiat.musicstore.musician.domain.entity.MusicianUsingBP
import io.basquiat.musicstore.musician.domain.entity.MusicianUsingCustom
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * getter/setter 테스트
 */
class SetterTest {

    @Test
    @DisplayName("빌드 패턴 적용")
    fun builderPattern_TEST() {
        val m = Musician.Builder()
                        .name("Charlie Parker")
                        .genre(GenreCode.JAZZ)
                        .build()
        assertThat(m.name).isEqualTo("Charlie Parker")
    }

    @Test
    @DisplayName("custom setter")
    fun entityCustomSetter_TEST() {
        val m = MusicianUsingCustom.Builder()
                                   .name("Charlie Pker")
                                   .genre(GenreCode.JAZZ)
                                   .build()

        assertThat(m.name).isEqualTo("Charlie Pker")
        // m.name = "test" 불가능
        m.changeName("Charlie Parker")
        assertThat(m.name).isEqualTo("Charlie Pker")
        assertThat(m.name).isEqualTo("Charlie Parker")
    }

    @Test
    @DisplayName("backing property")
    fun entityBackingProperty_TEST() {
        val m = MusicianUsingBP.Builder()
                               .name("Charlie Pker")
                               .genre(GenreCode.JAZZ)
                               .build()

        assertThat(m.name).isEqualTo("Charlie Pker")
        // m.name = "test" 불가능
        m.changeName("Charlie Parker")
        assertThat(m.name).isEqualTo("Charlie Pker")
        assertThat(m.name).isEqualTo("Charlie Parker")
    }

}