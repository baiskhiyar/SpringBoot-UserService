package UserService.repositories;

import UserService.models.RoleScopes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleScopesRepository extends JpaRepository<RoleScopes, Integer> {
    List<RoleScopes> findByRoleId(int roleId);
}
