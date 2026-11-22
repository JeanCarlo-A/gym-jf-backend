package com.gymjf.backend.shared.infrastructure.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.gymjf.backend.modules.auth.domain.Rol;

/**
 * Acceso al usuario autenticado de la solicitud actual. Los servicios lo usan
 * para filtrar por dueño y para rechazar (403) el acceso a datos de otros usuarios.
 */
@Component
public class CurrentUser {

    public AuthUser get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthUser user) {
            return user;
        }
        throw new AccessDeniedException("No hay un usuario autenticado");
    }

    public Integer id() {
        return get().getId();
    }

    public boolean isAdmin() {
        return get().getRol() == Rol.ADMIN;
    }

    /** Permite el acceso al administrador o al propio dueño del dato; cualquier otro recibe 403. */
    public void requireOwnerOrAdmin(Integer ownerId) {
        if (!isAdmin() && !id().equals(ownerId)) {
            throw new AccessDeniedException("No tienes permisos para acceder a datos de otro usuario");
        }
    }
}
