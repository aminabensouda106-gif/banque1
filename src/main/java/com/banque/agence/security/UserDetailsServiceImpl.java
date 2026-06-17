package com.banque.agence.security;

import com.banque.agence.repository.ClientRepository;
import com.banque.agence.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;

    public UserDetailsServiceImpl(UserRepository userRepository, ClientRepository clientRepository) {
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String login = username == null ? "" : username.trim();
        if (login.isEmpty()) {
            throw new UsernameNotFoundException("Identifiant vide.");
        }

        return userRepository.findByUsername(login)
                .map(SecurityUser::new)
                .map(UserDetails.class::cast)
                .or(() -> clientRepository.findActivePortalClientByLogin(login).map(SecurityClient::new))
                .orElseThrow(() -> new UsernameNotFoundException("Identifiant introuvable : " + login));
    }
}
