package UserService.services;

import UserService.helpers.CommonUtils;
import UserService.models.AccessTokenProvider;
import UserService.models.Users;
import UserService.helpers.AccessTokenProviderHelper;
import UserService.helpers.TimeUtility;
import UserService.repositories.AccessTokenProviderRepository;
import UserService.repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import UserService.helpers.ScopesHelper;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserService {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private AccessTokenProviderRepository accessTokenProviderRepository;

    @Autowired
    private ScopesHelper scopesHelper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private AccessTokenProviderHelper accessTokenProviderHelper;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public Users registerUser(Users user) {
        if (checkIfUsernameTaken(user.getUsername())){
            String errMsg = "Username is already taken";
            logger.error(errMsg);
            throw new RuntimeException(errMsg);
        }
        String hashedPassword = generatePasswordHash(user.getPassword());
        user.setPassword(hashedPassword);
        return usersRepository.save(user);
    }

    public Users findByUsername(String username) {

        String cacheKey = getUsernameCacheKey(username);
        Users cachedUser = redisService.get(cacheKey, Users.class);
        if (cachedUser == null) {
            Optional<Users> user = usersRepository.findByUsername(username);
            if (user.isPresent()) {
                cachedUser = user.get();
                redisService.save(cacheKey, cachedUser, 30, TimeUnit.MINUTES);
            }
        }
        return cachedUser;
    }

    public Users findById(int userId){
        String cacheKey = getUserByIdCacheKey(userId);
        Users cachedUser = redisService.get(cacheKey, Users.class);
        if (cachedUser == null){
            Users user = usersRepository.findById(userId);
            if (user != null){
                redisService.save(cacheKey, user, 30, TimeUnit.MINUTES);
                cachedUser = user;
            }
        }
        return cachedUser;
    }

    public Users updateUserById(int userId, Users userUpdateData) {
        if (userId == 0){
            String errMsg = "Missing userId in params!";
            logger.error(errMsg);
            throw new RuntimeException(errMsg);
        }
        Users existingUser = usersRepository.findById(userId);
        if (existingUser == null){
            throw new RuntimeException("User not found!");
        }
        String newUsername = userUpdateData.getUsername();
        if (!existingUser.getUsername().equals(newUsername) && checkIfUsernameTaken(newUsername)){
            throw new RuntimeException("Username already taken");
        }
        String hashedPassword = generatePasswordHash(userUpdateData.getPassword());
        userUpdateData.setPassword(hashedPassword);
        existingUser.setPassword(userUpdateData.getPassword());
        existingUser.setFirstName(userUpdateData.getFirstName());
        existingUser.setLastName(userUpdateData.getLastName());
        existingUser.setEmail(userUpdateData.getEmail());
        existingUser.setActive(userUpdateData.getActive());
        return usersRepository.save(existingUser);
    }

    public boolean checkIfUsernameTaken(String username) {
        return usersRepository.findByUsername(username).isPresent();
    }

    public String loginUser(String username, String password) {
        if(username == null || password == null){
            throw new RuntimeException("Username or password is missing!");
        }
        Users existingUser = usersRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found!"));
        if (!validatePassword(password, existingUser.getPassword())){
            throw new RuntimeException("Invalid username or password!");
        }
        if (!existingUser.getActive()){
            throw new RuntimeException("User is not active!");
        }
        return createAccessTokenForUser(existingUser);
    }

    public String createAccessTokenForUser(Users user){
        String[] userScopes = scopesHelper.getScopesForUser(user.getId());
        if (userScopes.length == 0){
            throw new RuntimeException("User has no scopes!");
        }
        String accessToken = UUID.randomUUID().toString();
        ZonedDateTime expiresAt = TimeUtility.addDeltaToTime(
                TimeUtility.getCurrentDateTime(), "month", 3
        );
        AccessTokenProvider accessTokenProvider = new AccessTokenProvider();
        accessTokenProvider.setUserId(user.getId());
        accessTokenProvider.setAccessToken(accessToken);
        accessTokenProvider.setAvailableScopes(String.join(",", userScopes));
//      Converting to localDateTimeZone and setting it to expiresAt
        accessTokenProvider.setExpiresAt(expiresAt.toLocalDateTime());
        accessTokenProviderRepository.save(accessTokenProvider);
        return accessToken;
    }

    public String logoutUser(String authToken){
        Users user = CommonUtils.getCurrentUser();
        if (user == null){
            throw new RuntimeException("User not found!");
        }
        accessTokenProviderHelper.expireAccessToken(authToken);
        clearUserCache(user);
        return "Logged out successfully";
    }

    public String generatePasswordHash(String password){
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(password);
    }

    public boolean validatePassword(String password, String hashedPassword){
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches(password, hashedPassword);
    }

    public String getUsernameCacheKey(String username){
        return "user_by_username_" + username;
    }

    public String getUserByIdCacheKey(int userId){
        return "user_by_id_" + userId;
    }

    public void clearUserCache(Users user){
        String[] cacheKeys = new String[]{getUsernameCacheKey(user.getUsername()), getUserByIdCacheKey(user.getId())};
        for (String cacheKey : cacheKeys){
            redisService.delete(cacheKey);
        }
    }

    public Users getUserDetailsForLogin(String username, String password){
        Users existingUser = findByUsername(username);
        if (existingUser == null){
            throw new RuntimeException("User not found!");
        }
        if (!validatePassword(password, existingUser.getPassword())){
            throw new RuntimeException("Invalid password!");
        }
        String[] availableScopes = scopesHelper.getScopesForUser(existingUser.getId());
        Users response = new Users();
        response.setId(existingUser.getId());
        response.setUsername(existingUser.getUsername());
        response.setAvailableScopes(availableScopes);
        return response;
    }
}

