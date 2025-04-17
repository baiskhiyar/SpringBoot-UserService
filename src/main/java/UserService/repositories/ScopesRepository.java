package UserService.repositories;


import UserService.models.Scopes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ScopesRepository extends JpaRepository<Scopes, Long> {

    @Query("select sc.name from UserRoles ur join RoleScopes rs on ur.roleId = rs.roleId join Scopes sc on rs.scopeId = sc.id where ur.userId = :userId")
    String[] getAllScopesForUser(@Param("userId") int userId);
}

