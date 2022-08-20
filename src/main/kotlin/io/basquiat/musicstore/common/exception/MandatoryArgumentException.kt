package io.basquiat.musicstore.common.exception

import java.lang.RuntimeException

/**
 * NotFoundException 관련 에러 처리 exception
 * created by basquiat
 */
class MandatoryArgumentException(message: String? = "필수 정보가 누락되었습니다.") : RuntimeException(message)