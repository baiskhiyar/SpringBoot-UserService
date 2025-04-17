package UserService.repositories;

import UserService.models.UserRoles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRolesRepository extends JpaRepository<UserRoles, Long> {

    @Query("SELECT ur.roleId FROM UserRoles ur WHERE ur.userId = :userId")
    int[] fetchRoleIdsForUser (@Param("userId") int userId);
    List<UserRoles> findByUserId(int userId);
}

