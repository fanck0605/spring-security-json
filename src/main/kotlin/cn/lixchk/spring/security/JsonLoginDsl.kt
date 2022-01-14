package cn.lixchk.spring.security

import org.springframework.security.authentication.AuthenticationDetailsSource
import org.springframework.security.config.annotation.web.HttpSecurityBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.web.servlet.SecurityMarker
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import javax.servlet.http.HttpServletRequest

@SecurityMarker
class JsonLoginDsl {
    var loginRequestParser: JsonAuthenticationFilter.LoginRequestParser? = null
    var loginPage: String? = null
    var authenticationSuccessHandler: AuthenticationSuccessHandler? = null
    var authenticationFailureHandler: AuthenticationFailureHandler? = null
    var failureUrl: String? = null
    var loginProcessingUrl: String? = null
    var permitAll: Boolean? = null
    var authenticationDetailsSource: AuthenticationDetailsSource<HttpServletRequest, *>? = null

    private var defaultSuccessUrlOption: Pair<String, Boolean>? = null

    /**
     * Grants access to the urls for [failureUrl] as well as for the [HttpSecurityBuilder], the
     * [loginPage] and [loginProcessingUrl] for every user.
     */
    fun permitAll() {
        permitAll = true
    }

    /**
     * Specifies where users will be redirected after authenticating successfully if
     * they have not visited a secured page prior to authenticating or [alwaysUse]
     * is true.
     *
     * @param defaultSuccessUrl the default success url
     * @param alwaysUse true if the [defaultSuccessUrl] should be used after
     * authentication despite if a protected page had been previously visited
     */
    fun defaultSuccessUrl(defaultSuccessUrl: String, alwaysUse: Boolean) {
        defaultSuccessUrlOption = Pair(defaultSuccessUrl, alwaysUse)
    }

    internal fun get(): (JsonLoginConfigurer) -> Unit {
        return { login ->
            loginRequestParser?.also { login.loginRequestParser(it) }
            loginPage?.also { login.loginPage(it) }
            failureUrl?.also { login.failureUrl(it) }
            loginProcessingUrl?.also { login.loginProcessingUrl(it) }
            permitAll?.also { login.permitAll(it) }
            defaultSuccessUrlOption?.also { login.defaultSuccessUrl(it.first, it.second) }
            authenticationSuccessHandler?.also { login.successHandler(it) }
            authenticationFailureHandler?.also { login.failureHandler(it) }
            authenticationDetailsSource?.also { login.authenticationDetailsSource(it) }
        }
    }
}

fun HttpSecurity.jsonLogin(formLoginConfiguration: JsonLoginDsl.() -> Unit): HttpSecurity {
    val loginCustomizer = JsonLoginDsl().apply(formLoginConfiguration).get()
    val loginConfigurer = getConfigurer(JsonLoginConfigurer::class.java) ?: apply(JsonLoginConfigurer())
    loginCustomizer(loginConfigurer)
    return this
}
