package UserService.helpers;

import UserService.repositories.ScopesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class ScopesHelper {

    @Autowired
    private ScopesRepository scopesRepository;

    public String[] getScopesForUser(int userId){
        return scopesRepository.getAllScopesForUser(userId);
    }

    public static boolean hasRequiredScopes(String[] availableScopes, String[] requiredScopes) {
        if (availableScopes == null || requiredScopes == null) return false;
        else{
            // Convert arrays to sets for efficient lookup
            Set<String> availableScopeSet = new HashSet<>(Arrays.asList(availableScopes));
            for (String requiredScope : requiredScopes) {
                if (availableScopeSet.contains(requiredScope)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String[] parseScopes(String scopes) {
        if (scopes == null || scopes.isEmpty()) return new String[0];
        return scopes.split(",");
    }
}

