package cn.lixchk.spring.security

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectReader
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import java.io.Reader
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JsonAuthenticationFilter : UsernamePasswordAuthenticationFilter {

    companion object {
        @JvmField
        val JSON_LOGIN_REQUEST: String =
            "${JsonAuthenticationFilter::class.java.packageName}.JSON_LOGIN_REQUEST"
    }

    var loginRequestParser: LoginRequestParser = DefaultLoginRequestParser()

    constructor() : super()

    constructor(authenticationManager: AuthenticationManager) : super(authenticationManager)

    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse): Authentication? {
        try {
            return super.attemptAuthentication(request, response)
        } finally {
            request.removeAttribute(JSON_LOGIN_REQUEST)
        }
    }

    override fun obtainUsername(request: HttpServletRequest): String? {
        return obtainLoginRequest(request)[usernameParameter]
    }

    override fun obtainPassword(request: HttpServletRequest): String? {
        return obtainLoginRequest(request)[passwordParameter]
    }

    private fun obtainLoginRequest(request: HttpServletRequest): Map<String, String?> {
        @Suppress("UNCHECKED_CAST")
        val loginRequest = request.getAttribute(JSON_LOGIN_REQUEST) as Map<String, String?>?
        if (loginRequest != null) return loginRequest

        if (request.characterEncoding == null) // TODO 查询相关文档，确定用法
            request.characterEncoding = Charsets.UTF_8.name()

        return loginRequestParser(request.reader)
            .also { request.setAttribute(JSON_LOGIN_REQUEST, it) }
    }

    interface LoginRequestParser {
        operator fun invoke(requestReader: Reader): Map<String, String?>
    }

    inner class DefaultLoginRequestParser : LoginRequestParser {

        private val servletObjectReader: ObjectReader = jacksonObjectMapper()
            .readerFor(object : TypeReference<Map<String, String?>>() {}) // TODO 查询相关文档，优化用法
            .withoutFeatures(JsonParser.Feature.AUTO_CLOSE_SOURCE)

        override fun invoke(requestReader: Reader): Map<String, String?> {
            return try {
                servletObjectReader.readValue(requestReader)
            } catch (e: JsonProcessingException) {
                logger.warn(e.message, e)
                emptyMap()
            }
        }
    }
}
