package it.epicode.eShop.auth;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class AppUserService {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    public AppUser registerUser(RegisterRequest registerRequest, Set<Role> roles) {
        if (appUserRepository.existsByUsername(registerRequest.getUsername())) {
            throw new EntityExistsException("Username già in uso");
        }


        AppUser appUser = new AppUser();
        appUser.setUsername(registerRequest.getUsername());
        appUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        appUser.setEmail(registerRequest.getEmail());
        appUser.setNome(registerRequest.getNome());
        appUser.setCognome(registerRequest.getCognome());
        appUser.setRoles(roles);


        return appUserRepository.save(appUser);
    }

    public Optional<AppUser> findByUsername(String username) {
        return appUserRepository.findByUsername(username);
    }

    public AuthResponse authenticateUser(String username, String password)  {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            AppUser appUser = loadUserByUsername(username);

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            AuthResponse authResponse = new AuthResponse();
            authResponse.setToken(jwtTokenUtil.generateToken(userDetails));

            authResponse.setUser(appUser);

            return authResponse;

        } catch (AuthenticationException e) {
            throw new SecurityException("Credenziali non valide", e);
        }
    }


    public AppUser loadUserByUsername(String username)  {
        AppUser appUser = appUserRepository.findByUsername(username)
            .orElseThrow(() -> new EntityNotFoundException("Utente non trovato con username: " + username));


        return appUser;
    }
}
