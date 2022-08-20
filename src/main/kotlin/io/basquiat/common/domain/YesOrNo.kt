package io.basquiat.common.domain

/**
 * YesOrNo enum
 * created by basuqiat
 */
enum class YesOrNo : InterfaceGenericEnum<YesOrNo> {

    Y,
    N;

    /**
     * DB에서 어떤 이유로 null인 경우도 있기 때문에 null일 경우 기본적으로 넘겨줄 enum객체를 생성한다.
     * @return YesOrNo
     */
    override fun defaultIfNull() = N

    /**
     * Custom Converter for low case
     */
    class LowCaseConverter : LowCaseEnumConverter<YesOrNo>(YesOrNo::class.java)

}