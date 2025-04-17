package UserService.repositories;


import UserService.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> { // Users class, long -> type of users primary key.
    Optional<Users> findByUsername(String username);
    Users findById(int userId);
}


