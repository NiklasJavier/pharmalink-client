package de.jklein.pharmalinkclient.security;

import com.vaadin.flow.spring.security.AuthenticationContext;
import java.util.Optional;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedUser {

    private final AuthenticationContext authenticationContext;

    public AuthenticatedUser(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    public Optional<OidcUser> get() {
        return authenticationContext.getAuthenticatedUser(OidcUser.class);
    }

    public void logout() {
        authenticationContext.logout();
    }

}
