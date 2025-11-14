package app.entities;

import app.security.entities.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "contributor")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Contributor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    private String githubProfile;

    private String screenName;

    private Integer contributions;

    @OneToMany(mappedBy = "contributor", fetch =FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    Set<Resource> resources = new HashSet<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_username", nullable = false, unique = true,
            referencedColumnName = "username")
    @ToString.Exclude
    @Setter(AccessLevel.NONE)
    private User user;

    public Contributor(String githubProfile, String screenName, Integer contributions) {
        this.githubProfile = githubProfile;
        this.screenName = screenName;
        this.contributions = contributions;
    }

    public Contributor(String githubProfile, String screenName, Integer contributions, Set<Resource> resources) {
        this.githubProfile = githubProfile;
        this.screenName = screenName;
        this.contributions = contributions;
        this.resources = resources;
    }

    //Helper methods for managing resources below
    public void addResource(Resource resource){
        if(resource == null){
            throw new IllegalArgumentException("You must enter a valid resource to add it!");
        }
        resources.add(resource);
        resource.setContributor(this);
    }


    public void removeResource(Resource resource){
        if(resource == null){
            throw new IllegalArgumentException("You must enter a valid resource to remove it!");
        }
        resources.remove(resource);
        resource.setContributor(null);
    }


    //Manages user <-> OneToOne relationship
    public void attachUser(User user) {
        this.user = user;
        if (user != null && user.getContributor() != this) {
            user.attachContributor(this);
        }
    }


}
