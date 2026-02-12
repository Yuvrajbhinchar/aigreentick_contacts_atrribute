package outfromapp.TestService;

import com.aigreentick.services.contacts.entity.Role;

import java.util.List;

public interface RoleService {

    Role createRole(Role role);

    Role getRoleById(Long id);

    List<Role> getAllRoles();

    Role updateRole(Long id, Role role);

    void deleteRole(Long id);
}
