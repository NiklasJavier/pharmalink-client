package de.jklein.pharmalinkclient.views.login;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinServletResponse;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import de.jklein.pharmalinkclient.security.UserSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;

import java.util.Collections;

// **HIER DIE 1. ANPASSUNG:** Die Route enthält jetzt den :username Parameter
@Route("login/token/:username/:token")
@PageTitle("Login Process")
@AnonymousAllowed
public class LoginSuccessView extends VerticalLayout implements BeforeEnterObserver {

    private static final Logger log = LoggerFactory.getLogger(LoginSuccessView.class);
    private final SecurityContextRepository securityContextRepository;
    private final UserSession userSession;

    public LoginSuccessView(SecurityContextRepository securityContextRepository, UserSession userSession) {
        this.securityContextRepository = securityContextRepository;
        this.userSession = userSession;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // **HIER DIE 2. ANPASSUNG:** Benutzername und Token aus der URL auslesen
        final String username = event.getRouteParameters().get("username").orElse("");
        final String token = event.getRouteParameters().get("token").orElse("");

        if (token.isEmpty() || username.isEmpty()) {
            log.warn("LoginSuccessView ohne Token oder Benutzername aufgerufen. Leite zum Login weiter.");
            event.forwardTo(LoginView.class);
            return;
        }

        try {
            HttpServletRequest request = ((VaadinServletRequest) VaadinService.getCurrentRequest()).getHttpServletRequest();
            HttpServletResponse response = ((VaadinServletResponse) VaadinService.getCurrentResponse()).getHttpServletResponse();

            userSession.setJwt(token);

            // **HIER DIE 3. ANPASSUNG:** Den ausgelesenen Benutzernamen verwenden
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    username, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);

            securityContextRepository.saveContext(context, request, response);

            log.info("Sitzung für Benutzer '{}' erfolgreich erstellt. Leite zum Dashboard weiter.", username);
            event.forwardTo("");

        } catch (Exception e) {
            log.error("Fehler bei der Erstellung der Benutzersitzung für '{}'.", username, e);
            event.rerouteTo(LoginView.class);
        }
    }
}