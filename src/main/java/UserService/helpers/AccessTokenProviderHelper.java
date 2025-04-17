package UserService.helpers;

import UserService.repositories.AccessTokenProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AccessTokenProviderHelper {
    @Autowired
    private AccessTokenProviderRepository accessTokenProviderRepository;
    @Transactional
    public void expireAccessToken(String accessToken){
        accessTokenProviderRepository.deleteByAccessToken(accessToken);
    }
}
