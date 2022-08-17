package io.basquiat.common.exception

import java.lang.RuntimeException

/**
 * ObjectEmptyException 관련 에러 처리 exception
 * created by basquiat
 */
class ObjectEmptyException(message: String? = "Object가 존재하지 않습니다.") : RuntimeException(message)