package com.gymjf.backend.shared.infrastructure.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.gymjf.backend.modules.auth.domain.Rol;
import com.gymjf.backend.modules.auth.domain.User;

import lombok.Getter;

/**
 * Usuario autenticado: además del correo y el rol lleva el id, para poder
 * comprobar que cada cliente solo accede a sus propios datos.
 */
@Getter
public class AuthUser implements UserDetails {

    private final Integer id;
    private final String email;
    private final String password;
    private final Rol rol;

    public AuthUser(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.rol = user.getRol();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }
}
