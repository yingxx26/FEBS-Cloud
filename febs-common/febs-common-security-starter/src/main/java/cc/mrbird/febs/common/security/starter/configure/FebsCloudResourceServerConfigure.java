package cc.mrbird.febs.common.security.starter.configure;

import cc.mrbird.febs.auth.authofmytest.mobile.SmsCodeAuthenticationFilter;
import cc.mrbird.febs.auth.authofmytest.mobile.SmsCodeAuthenticationProvider;
import cc.mrbird.febs.auth.authofmytest.mobile.SmsCodeAuthenticationSecurityConfig;
import cc.mrbird.febs.common.core.entity.constant.EndpointConstant;
import cc.mrbird.febs.common.core.entity.constant.StringConstant;
import cc.mrbird.febs.common.security.starter.handler.FebsAccessDeniedHandler;
import cc.mrbird.febs.common.security.starter.handler.FebsAuthExceptionEntryPoint;
import cc.mrbird.febs.common.security.starter.properties.FebsCloudSecurityProperties;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.UUID;

/**
 * @author MrBird
 */
@EnableResourceServer
@EnableAutoConfiguration(exclude = UserDetailsServiceAutoConfiguration.class)
public class FebsCloudResourceServerConfigure extends ResourceServerConfigurerAdapter {

    private FebsCloudSecurityProperties properties;
    private FebsAccessDeniedHandler accessDeniedHandler;
    private FebsAuthExceptionEntryPoint exceptionEntryPoint;
    ////////////方案二/////////////////

    private UserDetailsService userDetailsService;

    @Autowired(required = false)
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }
    /////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////短信验证码（方案一）/////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /*private SmsCodeAuthenticationSecurityConfig smsCodeAuthenticationSecurityConfig;

    @Autowired(required = false)
    public void setSmsCodeAuthenticationSecurityConfig(SmsCodeAuthenticationSecurityConfig smsCodeAuthenticationSecurityConfig) {
        this.smsCodeAuthenticationSecurityConfig = smsCodeAuthenticationSecurityConfig;
    }*/
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    @Autowired(required = false)
    public void setProperties(FebsCloudSecurityProperties properties) {
        this.properties = properties;
    }

    @Autowired(required = false)
    public void setAccessDeniedHandler(FebsAccessDeniedHandler accessDeniedHandler) {
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Autowired(required = false)
    public void setExceptionEntryPoint(FebsAuthExceptionEntryPoint exceptionEntryPoint) {
        this.exceptionEntryPoint = exceptionEntryPoint;
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        ////////////方案二/////////////////
        SmsCodeAuthenticationFilter smsCodeAuthenticationFilter = new SmsCodeAuthenticationFilter();
        smsCodeAuthenticationFilter.setAuthenticationManager(http.getSharedObject(AuthenticationManager.class));
        String key = UUID.randomUUID().toString();

        SmsCodeAuthenticationProvider smsCodeAuthenticationProvider = new SmsCodeAuthenticationProvider();
        smsCodeAuthenticationProvider.setUserDetailsService(userDetailsService);

        http.authenticationProvider(smsCodeAuthenticationProvider)
                .addFilterAfter(smsCodeAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        /////////////////////////////

        if (properties == null) {
            premitAll(http);
            return;
        }
        String[] anonUrls = StringUtils.splitByWholeSeparatorPreserveAllTokens(properties.getAnonUris(), StringConstant.COMMA);
        if (ArrayUtils.isEmpty(anonUrls)) {
            anonUrls = new String[]{};
        }
        if (ArrayUtils.contains(anonUrls, EndpointConstant.ALL)) {
            premitAll(http);
            return;
        }
        http.csrf().disable()
                .requestMatchers().antMatchers(properties.getAuthUri()) // getAuthUri的值是 /**
                .and()
                .authorizeRequests()
                .antMatchers(anonUrls).permitAll()  // anonUrls的值是 /actuator/**,/captcha,/social/**,/v2/api-docs,/v2/api-docs-ext,/login,/resource/**,/test/**
                .antMatchers(properties.getAuthUri()).authenticated()
                .and()
                ////////////方案一/////////////////
                //.apply(smsCodeAuthenticationSecurityConfig)
                //.and()
                /////////////////////////////
                .httpBasic();
    }

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        if (exceptionEntryPoint != null) {
            resources.authenticationEntryPoint(exceptionEntryPoint);
        }
        if (accessDeniedHandler != null) {
            resources.accessDeniedHandler(accessDeniedHandler);
        }
    }

    private void premitAll(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.authorizeRequests().anyRequest().permitAll();
    }
}
