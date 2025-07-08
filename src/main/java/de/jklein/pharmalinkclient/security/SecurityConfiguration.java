package de.jklein.pharmalinkclient.security;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration extends VaadinWebSecurity {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Erlaube öffentliche Ressourcen
        http.authorizeHttpRequests(auth ->
                auth.requestMatchers(
                        "/images/**",
                        "/line-awesome/**",
                        "/icons/**"
                ).permitAll()
        );

        // JWT ist stateless, daher keine CSRF-Protection für API-Routen (die hier nicht definiert sind)
        // Aber für Vaadin-Kommunikation wird es gebraucht.
        // Vaadin kümmert sich um den Rest.

        super.configure(http);

        // Leite alle nicht-authentifizierten Benutzer zur LoginView
        setLoginView(http, de.jklein.views.login.LoginView.class);
    }
}