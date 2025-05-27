package com.sofrecom.backend.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @Test
    @DisplayName("Should have correct number of roles defined")
    void testRoleEnumValues() {
        // Act
        Role[] roles = Role.values();

        // Assert
        assertEquals(2, roles.length);
        assertEquals(Role.Administrateur, roles[0]);
        assertEquals(Role.Collaborateur, roles[1]);
    }

    @Test
    @DisplayName("Should return correct enum value from string")
    void testRoleValueOf() {
        // Act & Assert
        assertEquals(Role.Administrateur, Role.valueOf("Administrateur"));
        assertEquals(Role.Collaborateur, Role.valueOf("Collaborateur"));
    }

    @Test
    @DisplayName("Should throw exception for invalid role name")
    void testRoleValueOfWithInvalidName() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> Role.valueOf("InvalidRole"));
        assertThrows(IllegalArgumentException.class, () -> Role.valueOf("ADMIN"));
        assertThrows(IllegalArgumentException.class, () -> Role.valueOf("USER"));
    }

    @Test
    @DisplayName("Should return correct permissions for Administrateur role")
    void testAdministrateurPermissions() {
        // Act
        Set<Permission> permissions = Role.Administrateur.getPermissions();

        // Assert
        assertNotNull(permissions);
        assertEquals(4, permissions.size());
        assertTrue(permissions.contains(Permission.ADMIN_READ));
        assertTrue(permissions.contains(Permission.ADMIN_WRITE));
        assertTrue(permissions.contains(Permission.ADMIN_UPDATE));
        assertTrue(permissions.contains(Permission.ADMIN_DELETE));
    }

    @Test
    @DisplayName("Should return empty permissions for Collaborateur role")
    void testCollaborateurPermissions() {
        // Act
        Set<Permission> permissions = Role.Collaborateur.getPermissions();

        // Assert
        assertNotNull(permissions);
        assertTrue(permissions.isEmpty());
        assertEquals(0, permissions.size());
    }

    @Test
    @DisplayName("Should return correct authorities for Administrateur role")
    void testAdministrateurAuthorities() {
        // Act
        List<SimpleGrantedAuthority> authorities = Role.Administrateur.getAuthorities();

        // Assert
        assertNotNull(authorities);
        assertEquals(5, authorities.size()); // 4 permissions + 1 role

        // Check if all permission authorities are present
        assertTrue(authorities.contains(new SimpleGrantedAuthority(Permission.ADMIN_READ.getPermission())));
        assertTrue(authorities.contains(new SimpleGrantedAuthority(Permission.ADMIN_WRITE.getPermission())));
        assertTrue(authorities.contains(new SimpleGrantedAuthority(Permission.ADMIN_UPDATE.getPermission())));
        assertTrue(authorities.contains(new SimpleGrantedAuthority(Permission.ADMIN_DELETE.getPermission())));

        // Check if role authority is present
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_Administrateur")));
    }

    @Test
    @DisplayName("Should return correct authorities for Collaborateur role")
    void testCollaborateurAuthorities() {
        // Act
        List<SimpleGrantedAuthority> authorities = Role.Collaborateur.getAuthorities();

        // Assert
        assertNotNull(authorities);
        assertEquals(1, authorities.size()); // Only role authority, no permissions

        // Check if role authority is present
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_Collaborateur")));
    }

    @Test
    @DisplayName("Should maintain immutability of permissions")
    void testPermissionsImmutability() {
        // Act
        Set<Permission> adminPermissions = Role.Administrateur.getPermissions();
        Set<Permission> collaborateurPermissions = Role.Collaborateur.getPermissions();

        // Assert - Attempting to modify should not affect the original
        int originalAdminSize = adminPermissions.size();
        int originalCollaborateurSize = collaborateurPermissions.size();

        // Try to modify (should throw exception or not affect original)
        assertThrows(UnsupportedOperationException.class, () ->
                adminPermissions.add(Permission.ADMIN_READ));
        assertThrows(UnsupportedOperationException.class, () ->
                collaborateurPermissions.add(Permission.ADMIN_READ));

        // Verify sizes remain the same
        assertEquals(originalAdminSize, Role.Administrateur.getPermissions().size());
        assertEquals(originalCollaborateurSize, Role.Collaborateur.getPermissions().size());
    }

    @Test
    @DisplayName("Should return new list instance each time getAuthorities is called")
    void testAuthoritiesNewInstance() {
        // Act
        List<SimpleGrantedAuthority> authorities1 = Role.Administrateur.getAuthorities();
        List<SimpleGrantedAuthority> authorities2 = Role.Administrateur.getAuthorities();

        // Assert
        assertNotSame(authorities1, authorities2); // Different instances
        assertEquals(authorities1, authorities2); // But same content
    }

    @Test
    @DisplayName("Should allow modification of returned authorities list")
    void testAuthoritiesListModifiable() {
        // Act
        List<SimpleGrantedAuthority> authorities = Role.Collaborateur.getAuthorities();
        int originalSize = authorities.size();

        // Should be able to modify the returned list without affecting the enum
        authorities.add(new SimpleGrantedAuthority("TEST_AUTHORITY"));

        // Assert
        assertEquals(originalSize + 1, authorities.size());

        // Verify original enum is not affected
        List<SimpleGrantedAuthority> freshAuthorities = Role.Collaborateur.getAuthorities();
        assertEquals(originalSize, freshAuthorities.size());
    }

    @Test
    @DisplayName("Should have correct role name in authority")
    void testRoleNameInAuthority() {
        // Act & Assert
        List<SimpleGrantedAuthority> adminAuthorities = Role.Administrateur.getAuthorities();
        List<SimpleGrantedAuthority> collaborateurAuthorities = Role.Collaborateur.getAuthorities();

        // Check exact role names
        assertTrue(adminAuthorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_Administrateur")));
        assertTrue(collaborateurAuthorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_Collaborateur")));
    }
    public Role getUserRole(String email) {
        return email.equals("admin@example.com") ? Role.Administrateur : Role.Collaborateur;
    }


    @Test
    @DisplayName("Should handle enum comparison correctly")
    void testRoleComparison() {
        // Act
        Role actualAdminRole = getUserRole("admin@example.com");
        Role actualCollabRole = getUserRole("user@example.com");

        // Assert
        assertEquals(Role.Administrateur, actualAdminRole);
        assertEquals(Role.Collaborateur, actualCollabRole);
        assertNotEquals(actualAdminRole, actualCollabRole);
        assertNotEquals(actualCollabRole, actualAdminRole);
    }


    @Test
    @DisplayName("Should have correct string representation")
    void testRoleToString() {
        // Act & Assert
        assertEquals("Administrateur", Role.Administrateur.toString());
        assertEquals("Collaborateur", Role.Collaborateur.toString());
        assertEquals("Administrateur", Role.Administrateur.name());
        assertEquals("Collaborateur", Role.Collaborateur.name());
    }

    @Test
    @DisplayName("Should handle ordinal values correctly")
    void testRoleOrdinal() {
        // Act & Assert
        assertEquals(0, Role.Administrateur.ordinal());
        assertEquals(1, Role.Collaborateur.ordinal());
    }
}

// Supporting Permission enum for testing (if not already present in your test classpath)
enum Permission {
    ADMIN_READ("admin:read"),
    ADMIN_WRITE("admin:write"),
    ADMIN_UPDATE("admin:update"),
    ADMIN_DELETE("admin:delete");

    private final String permission;

    Permission(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }
}