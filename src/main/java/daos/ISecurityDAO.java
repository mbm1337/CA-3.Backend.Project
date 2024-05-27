package daos;

import persistence.model.Role;
import persistence.model.User;

public interface ISecurityDAO {
    User getVerifiedUser(String username, String password);

    User createUser(String username, String password);

    Role createRole(String role);

    User addUserRole(String username, String role);

    User deleteUser(String username);
}
