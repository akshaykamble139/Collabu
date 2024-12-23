package com.akshay.Collabu.security;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.akshay.Collabu.services.CustomUserDetailsService;
import com.akshay.Collabu.utils.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        String authHeader = request.getHeader("Authorization");

        try {
	        if (authHeader != null && authHeader.startsWith("Bearer ")) {
	            String token = authHeader.substring(7);
	            String username = jwtUtils.extractUsername(token);
	
	            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
	                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
	
	                if (jwtUtils.validateToken(token, userDetails.getUsername())) {
	                    UsernamePasswordAuthenticationToken authToken =
	                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
	                    SecurityContextHolder.getContext().setAuthentication(authToken);
	                }
	            }
	        }
	
	        filterChain.doFilter(request, response);
        } catch (SignatureException ex) {
            handleJwtException(response, "Invalid token", "The token signature is invalid. Please login again.");
        } catch (ExpiredJwtException ex) {
            handleJwtException(response, "Token expired", "Your session has expired. Please login again.");
        }
        
    }
    
    private void handleJwtException(HttpServletResponse response, String error, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("error", error);
        errorDetails.put("message", message);

        response.getWriter().write(new ObjectMapper().writeValueAsString(errorDetails));
    }
}
