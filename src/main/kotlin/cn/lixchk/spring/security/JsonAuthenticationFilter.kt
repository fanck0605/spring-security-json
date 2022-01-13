package cn.lixchk.spring.security

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectReader
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import java.io.Reader
import javax.servlet.http.HttpServletRequest

class JsonAuthenticationFilter : UsernamePasswordAuthenticationFilter {

    companion object {
        val JSON_LOGIN_REQUEST: String =
            "${JsonAuthenticationFilter::class.java.packageName}.JSON_LOGIN_REQUEST"
    }

    var loginRequestParser: LoginRequestParser = DefaultLoginRequestParser

    constructor() : super()

    constructor(authenticationManager: AuthenticationManager) : super(authenticationManager)

    override fun obtainUsername(request: HttpServletRequest): String? {
        return obtainJsonLoginRequest(request)[usernameParameter]
    }

    override fun obtainPassword(request: HttpServletRequest): String? {
        return obtainJsonLoginRequest(request)[passwordParameter]
    }

    private fun obtainJsonLoginRequest(request: HttpServletRequest): Map<String, String?> {
        @Suppress("UNCHECKED_CAST")
        val loginRequest = request.getAttribute(JSON_LOGIN_REQUEST) as Map<String, String?>?
        if (loginRequest != null) return loginRequest

        if (request.characterEncoding == null) // TODO 查询相关文档，确定用法
            request.characterEncoding = Charsets.UTF_8.name()

        return loginRequestParser(request.reader)
    }

    interface LoginRequestParser {
        operator fun invoke(requestReader: Reader): Map<String, String?>
    }

    object DefaultLoginRequestParser : LoginRequestParser {

        private val servletObjectReader: ObjectReader = jacksonObjectMapper().reader()
            .withoutFeatures(JsonParser.Feature.AUTO_CLOSE_SOURCE)

        override fun invoke(requestReader: Reader): Map<String, String?> {
            return try {
                servletObjectReader.readValue(requestReader)
            } catch (e: JsonProcessingException) {
                emptyMap()
            }
        }
    }
}
