package io.basquiat.common.util

import io.basquiat.common.exception.ObjectEmptyException

/**
 * 메세지가 없는 경우
 */
fun objectEmpty(): Nothing {
    throw ObjectEmptyException()
}

/**
 * 메세지가 있는 경우
 */
fun objectEmpty(message: String?): Nothing {
    if(message == null) {
        objectEmpty()
    } else {
        throw ObjectEmptyException(message)
    }
}
