package tech.sergeyev.compmechlabauth.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.kerberos.authentication.KerberosServiceAuthenticationProvider;
import org.springframework.security.kerberos.authentication.sun.SunJaasKerberosTicketValidator;
import org.springframework.security.kerberos.client.config.SunJaasKrb5LoginConfig;
import org.springframework.security.kerberos.client.ldap.KerberosLdapContextSource;
import org.springframework.security.kerberos.web.authentication.SpnegoAuthenticationProcessingFilter;
import org.springframework.security.kerberos.web.authentication.SpnegoEntryPoint;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
import org.springframework.security.ldap.userdetails.LdapUserDetailsService;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import tech.sergeyev.compmechlabauth.ActiveDirectoryLdapAuthoritiesPopulator;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSecurityConfig.class);

    @Value("${app.ad-domain}")
    private String adDomain;

    @Value("${app.ad-server}")
    private String adServer;

    @Value("${app.service-principal}")
    private String servicePrincipal;

    @Value("${app.keytab-location}")
    private String keytabLocation;

    @Value("${app.ldap-search-base}")
    private String ldapSearchBase;

    @Value("${app.ldap-search-filter}")
    private String ldapSearchFilter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .exceptionHandling()
                    .authenticationEntryPoint(spnegoEntryPoint())
                    .and()
                .authorizeRequests()
                    .anyRequest().fullyAuthenticated()
                    .and()
                .formLogin()
                    .and()
                .addFilterBefore(
                        spnegoAuthenticationProcessingFilter(),
                        BasicAuthenticationFilter.class
                )
                .csrf().disable();
    }
// Рабочая версия.
// По-экспериментировал? Не работает?
// Удали всю написанную хуиту и раскомментируй эту реализацию
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .authenticationProvider(kerberosServiceAuthenticationProvider())
                .authenticationProvider(activeDirectoryLdapAuthenticationProvider());
    }

    @Bean
    public ActiveDirectoryLdapAuthenticationProvider activeDirectoryLdapAuthenticationProvider() {
        return new ActiveDirectoryLdapAuthenticationProvider(adDomain, adServer);
    }

    @Bean
    public SpnegoEntryPoint spnegoEntryPoint() {
        return new SpnegoEntryPoint("/");
    }

    @Bean
    public SpnegoAuthenticationProcessingFilter spnegoAuthenticationProcessingFilter() {
        SpnegoAuthenticationProcessingFilter filter =
                new SpnegoAuthenticationProcessingFilter();
        try {
            filter.setAuthenticationManager(authenticationManagerBean());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filter;
    }

    @Bean
    public KerberosServiceAuthenticationProvider kerberosServiceAuthenticationProvider() throws Exception {
        KerberosServiceAuthenticationProvider provider =
                new KerberosServiceAuthenticationProvider();
        provider.setTicketValidator(sunJaasKerberosTicketValidator());
        provider.setUserDetailsService(ldapUserDetailsService());
        return provider;
    }

    @Bean
    public SunJaasKerberosTicketValidator sunJaasKerberosTicketValidator() {
        SunJaasKerberosTicketValidator validator =
                new SunJaasKerberosTicketValidator();
        validator.setServicePrincipal(servicePrincipal);
        FileSystemResource resource = new FileSystemResource(keytabLocation);
        validator.setKeyTabLocation(resource);
        LOGGER.info(
                "Initializing Kerberos KEYTAB file path:{} for principal:{}, file exists:{}",
                resource.getFilename(), servicePrincipal, resource.exists()
        );
        validator.setDebug(true);
        return validator;
    }

    @Bean
    public KerberosLdapContextSource kerberosLdapContextSource() throws Exception {
        KerberosLdapContextSource source = new KerberosLdapContextSource(adServer);
        source.setLoginConfig(loginConfig());
        return source;
    }

    public SunJaasKrb5LoginConfig loginConfig() throws Exception {
        SunJaasKrb5LoginConfig config = new SunJaasKrb5LoginConfig();
        config.setKeyTabLocation(new FileSystemResource(keytabLocation));
        config.setServicePrincipal(servicePrincipal);
        config.setDebug(true);
        config.setIsInitiator(true);
        config.afterPropertiesSet();
        return config;
    }

    @Bean
    public LdapUserDetailsService ldapUserDetailsService() throws Exception {
        LOGGER.info("\n\nldapUserDetailsService() run");
        FilterBasedLdapUserSearch search = new FilterBasedLdapUserSearch(
                ldapSearchBase,
                ldapSearchFilter,
                kerberosLdapContextSource()
        );
        LdapUserDetailsService service = new LdapUserDetailsService(
                search,
                new ActiveDirectoryLdapAuthoritiesPopulator()
        );
        service.setUserDetailsMapper(new LdapUserDetailsMapper());
        return service;
    }
}
