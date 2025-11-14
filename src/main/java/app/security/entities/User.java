package app.security.entities;

import app.entities.Contributor;
import app.security.hashing.PasswordHasher;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder

/**
 * Purpose: To handle security in the API
 * Author: Thomas Hartmann
 */
public class User implements ISecurityUser {
    @Id
    @EqualsAndHashCode.Include
    private String username;


    @Column(nullable = false)
    @ToString.Exclude
    private String password;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "username"),
            inverseJoinColumns = @JoinColumn(name = "role_name", referencedColumnName = "name")
    )
    @ToString.Exclude
    @Builder.Default
    private Set<Role> roles = new HashSet<>();


    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval =
            true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @Setter(AccessLevel.NONE)
    private Contributor contributor;

    public void attachContributor(Contributor contributor) {
        this.contributor = contributor;
        if (contributor != null && contributor.getUser() != this) {
            contributor.attachUser(this);
        }
    }


    public User(String userName, String userPass) {
        this.username = userName;
        this.password = PasswordHasher.hashPassFirstTime(userPass);
    }

    public User(Contributor contributor, Set<Role> roles, String username) {
        this.contributor = contributor;
        this.roles = roles;
        this.username = username;
    }

    public User(String userName, Set<Role> roleEntityList) {
        this.username = userName;
        this.roles = roleEntityList;
    }


    public Set<String> getRolesAsStrings() {
        if (roles.isEmpty()) {
            return null;
        }
        Set<String> rolesAsStrings = new HashSet<>();
        roles.forEach((r) -> rolesAsStrings.add(r.getName()));
        return rolesAsStrings;
    }


    @Override
    public boolean verifyPass(String plainPassword) {
        return PasswordHasher.checkPw(plainPassword, this.password);
    }

    @Override
    public void addRole(Role role) {
        if (role == null) {
            return;
        }
        if(roles == null){
            roles = new HashSet<>();
        }
        roles.add(role);
        role.getUsers().add(this);
    }

    public void removeRole(String userRole) {
        roles.stream()
                .filter((r) ->
                {
                    return r.getName().equalsIgnoreCase(userRole);
                })
                .findFirst()
                .ifPresent(role -> {
                    roles.remove(role);
                    role.getUsers().remove(this);
                });
    }

}
