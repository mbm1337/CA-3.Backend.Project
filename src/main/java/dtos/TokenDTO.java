package dtos;

import persistence.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TokenDTO {
    String token;
    String username;
    private Set<String> roles;

    public TokenDTO(String token, User user) {
        this.token = token;
        this.username = user.getEmail();
        this.roles = user.getRolesAsStrings(); // Assuming you have a method to get role names as strings
    }
}
