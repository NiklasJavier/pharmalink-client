package de.jklein.pharmalinkclient.security; // Notice the change here

import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import java.io.Serializable;

@Component
@Scope(value = "vaadin-session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserSession implements Serializable {
    // ... rest of the class is the same
    private String jwt;

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public boolean isLoggedIn() {
        return jwt != null && !jwt.isEmpty();
    }
}