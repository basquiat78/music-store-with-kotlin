package io.basquiat.common.domain

/**
 * Rest API response 정보를 담은 객체
 */
data class ResponseResult<T>(
    private val _result: T?,
) {

    val result get() = this._result

    companion object {
        /**
         * ResponseResult를 생성하는 정적 메소드
         * @param result
         * @param <T>
         * @return ResponseResult<T>
         */
        fun <T> of(result: T): ResponseResult<T> {
            return ResponseResult(result)
        }
    }

}