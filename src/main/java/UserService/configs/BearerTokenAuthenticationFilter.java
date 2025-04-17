package UserService.configs;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

// Authentication Filter
@Component
class BearerTokenAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private BearerTokenAuthenticationProvider authenticationProvider;

    private static final Logger logger = LoggerFactory.getLogger(BearerTokenAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
    ) throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            try {
                String bearerAuthToken = authorizationHeader.substring(7); // Removing "Bearer " prefix
                BearerTokenAuthentication authRequest = new BearerTokenAuthentication(bearerAuthToken);
                // This method call is responsible for getting the user from the auth token and adding scopes in the
                // scope authority which will be used to check required scopes vs available scopes on the api level.
                Authentication authenticationResult = authenticationProvider.authenticate(authRequest);
                // Now token is authenticated, Token, Users object and authorities that is available scopes is mapped.
                if (authenticationResult == null) {
                    logger.error("Authentication Failed");
                } else {
                    // Now it will check the intersection of required vs available scopes for the api getting called.
                    // Setting authUser and scopes in the context which will be available throughout the api request
                    // journey.
                    SecurityContextHolder.getContext().setAuthentication(authenticationResult);
                }
            } catch (AuthenticationException e) {
                SecurityContextHolder.clearContext();
                logger.error(e.getMessage());
                throw e;
            }
        } else {
            logger.warn("Authorization header is missing");
        }
        filterChain.doFilter(request, response);
    }
}
