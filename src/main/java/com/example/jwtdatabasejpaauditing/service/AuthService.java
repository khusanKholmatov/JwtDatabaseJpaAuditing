package com.example.jwtdatabasejpaauditing.service;

import com.example.jwtdatabasejpaauditing.entity.User;
import com.example.jwtdatabasejpaauditing.entity.enums.RoleName;
import com.example.jwtdatabasejpaauditing.payload.ApiResponse;
import com.example.jwtdatabasejpaauditing.payload.LoginDto;
import com.example.jwtdatabasejpaauditing.payload.UserRegisterDto;
import com.example.jwtdatabasejpaauditing.repository.RoleRepository;
import com.example.jwtdatabasejpaauditing.repository.UserRepository;
import com.example.jwtdatabasejpaauditing.security.JwtProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    JavaMailSender javaMailSender;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    JwtProvider jwtProvider;

    public ApiResponse registerUser(UserRegisterDto registerDto){
        if (!userRepository.existsByEmail(registerDto.getEmail())) {
            User user = new User();
            user.setFirstName(registerDto.getFirstName());
            user.setLastName(registerDto.getLastName());
            user.setEmail(registerDto.getEmail());
            user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
            user.setRoles(Collections.singleton(roleRepository.findByRoleName(RoleName.ROLE_USER)));
            user.setEmailCode(UUID.randomUUID().toString());
            if (sendEmail(user.getEmail(), user.getEmailCode())){
                userRepository.save(user);
                return new ApiResponse("Sent code to email successfully", true);
            }else {
                return new ApiResponse("Some kinda error occured", false);
            }
        }
        return new ApiResponse("Email already exists", false);
    }

    public boolean sendEmail(String sendingEmail, String emailCode) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom("JwtProject@aaa.com");
            mailMessage.setTo(sendingEmail);
            mailMessage.setSubject("Verifying account");
            mailMessage.setText("<a href='http://localhost:8080/api/auth/verifyEmail?emailCode=" + emailCode + "&email=" + sendingEmail + ">Confirm Email, Click the link</a>");
            javaMailSender.send(mailMessage);
            return true;
        }catch (MailException exception){
            exception.printStackTrace();
            return false;
        }
    }

    public ApiResponse verifyEmail(String emailCode, String email) {
        Optional<User> optionalUser = userRepository.findByEmailAndEmailCode(email, emailCode);

        if (optionalUser.isPresent()){
            User user = optionalUser.get();
            user.setAccountEnabled(true);
            user.setEmailCode(null);
            userRepository.save(user);
            return new ApiResponse("Account verified", true);
        }
        return new ApiResponse("Account already exists", false);
    }

    public ApiResponse login(LoginDto loginDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginDto.getUsername(),
                    loginDto.getPassword()
            ));
            User user = (User) authentication.getPrincipal();
            String generateToken = jwtProvider.generateToken(loginDto.getUsername(), user.getRoles());
            return new ApiResponse("Token created", true, generateToken);
        }catch (BadCredentialsException e) {
            return new ApiResponse("something went wrong", false);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
