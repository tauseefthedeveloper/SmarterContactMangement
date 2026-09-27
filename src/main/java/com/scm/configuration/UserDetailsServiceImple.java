package com.scm.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.scm.dao.UserRepository;
import com.scm.entities.User;

@Service
public class UserDetailsServiceImple implements UserDetailsService {

    @Autowired
    private UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        User user = this.userRepo.getUserByUserName(username);

        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        return new CustomeUserDetails(user);
    }
}
