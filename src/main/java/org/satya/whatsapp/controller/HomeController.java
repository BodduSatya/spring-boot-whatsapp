package org.satya.whatsapp.controller;

import org.satya.whatsapp.modal.LoginForm;
import org.satya.whatsapp.service.MyUserDetailsService;
import org.satya.whatsapp.webtoken.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@Controller
public class HomeController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private MyUserDetailsService myUserDetailsService;

//    @RequestMapping(value = "/{path:[^\\.]*}", method = RequestMethod.GET)
//    public String forward() {
//        return "forward:/";
//    }

    @GetMapping("/home")
    public String welcomePage() {
        return "home";
    }

    @GetMapping("/admin/home")
    public String adminHomePage() {
        return "home_admin";
    }

    @GetMapping("/user/home")
    public String userHomePage() {
        return "home_user";
    }

    @GetMapping("/login")
    public String loginForm() {
        return "customLogin";
//        return "forward:/loginPage";
    }

    @PostMapping("/authenticate")
    public String authenticate(@RequestBody LoginForm loginForm) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginForm.username(),loginForm.password()
        ));

        if( authentication.isAuthenticated()){
            return jwtService.generateToken(myUserDetailsService.loadUserByUsername(loginForm.username()));
        }
        else
            throw  new UsernameNotFoundException("Invalid username or password");
    }

//    return ResponseEntity.ok(Collections.singletonMap("token", token));


}
