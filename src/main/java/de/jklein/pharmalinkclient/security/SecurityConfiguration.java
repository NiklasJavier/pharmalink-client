package de.jklein.pharmalinkclient.security;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import de.jklein.pharmalinkclient.views.login.LoginView;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration extends VaadinWebSecurity {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        // Stellt das Repository als Bean zur Verfügung, damit wir es injizieren können.
        return new HttpSessionSecurityContextRepository();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        // Wir definieren LoginView als das Ziel für unauthentifizierte Benutzer.
        // LoginSuccessView wird durch @AnonymousAllowed geschützt.
        setLoginView(http, LoginView.class);
    }
}