package UserService.repositories;

import UserService.models.AccessTokenProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AccessTokenProviderRepository extends JpaRepository<AccessTokenProvider, Long> {

    @Modifying
    @Query("update AccessTokenProvider set expiresAt = :expiresAt where accessToken = :accessToken")
    void expireToken(@Param("accessToken") String token, @Param("expiresAt") LocalDateTime expiresAt);
    AccessTokenProvider findByAccessToken(String accessToken);
    void deleteByAccessToken(String accessToken);
}


