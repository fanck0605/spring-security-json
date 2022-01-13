package cn.lixchk.spring.security

import cn.lixchk.spring.security.JsonAuthenticationFilter.LoginRequestParser
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher

class JsonLoginConfigurer :
    AbstractAuthenticationFilterConfigurer<HttpSecurity, JsonLoginConfigurer, JsonAuthenticationFilter>
        (JsonAuthenticationFilter(), null) {

    init {
        usernameParameter("username")
        passwordParameter("password")
    }

    fun loginRequestParser(loginRequestParser: LoginRequestParser): JsonLoginConfigurer {
        authenticationFilter.loginRequestParser = loginRequestParser
        return this
    }

    public override fun loginPage(loginPage: String): JsonLoginConfigurer {
        return super.loginPage(loginPage)
    }

    fun usernameParameter(usernameParameter: String): JsonLoginConfigurer {
        authenticationFilter.usernameParameter = usernameParameter
        return this
    }

    fun passwordParameter(passwordParameter: String): JsonLoginConfigurer {
        authenticationFilter.passwordParameter = passwordParameter
        return this
    }

    override fun createLoginProcessingUrlMatcher(loginProcessingUrl: String): RequestMatcher {
        return AntPathRequestMatcher(loginProcessingUrl, "POST")
    }
}
