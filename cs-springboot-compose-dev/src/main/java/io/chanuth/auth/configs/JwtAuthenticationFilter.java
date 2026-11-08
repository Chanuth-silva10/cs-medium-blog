package io.chanuth.auth.configs;

import io.chanuth.auth.services.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Runs once per request. If the request carries "Authorization: Bearer <token>" and the token is
 * valid, the user is put into the security context. If the token is bad, the request simply stays
 * anonymous, and the reason is kept so the 401 answer can explain it.
 *
 * This class is deliberately NOT a @Component: Spring Boot would also register every Filter bean
 * as a plain servlet filter, running it a second time outside the security chain.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** Request attribute the entry point reads to explain why authentication failed. */
    public static final String AUTH_ERROR = "devcraft.auth.error";

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(BEARER_PREFIX.length()).trim();
        try {
            String email = jwtService.extractUsername(token);
            // Load the user on every request: a deleted user's token stops working at once,
            // and a role change applies to the very next request.
            UserDetails user = userDetailsService.loadUserByUsername(email);

            var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (ExpiredJwtException e) {
            request.setAttribute(AUTH_ERROR, "The token has expired");
        } catch (JwtException | IllegalArgumentException e) {
            request.setAttribute(AUTH_ERROR, "The token is invalid");
        } catch (UsernameNotFoundException e) {
            request.setAttribute(AUTH_ERROR, "The account behind this token no longer exists");
        }

        chain.doFilter(request, response);
    }
}
