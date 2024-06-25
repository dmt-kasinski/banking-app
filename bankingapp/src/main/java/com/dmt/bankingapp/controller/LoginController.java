package com.dmt.bankingapp.controller;

import com.dmt.bankingapp.service.implementation.RedirectLoggedClientImpl;
import com.dmt.bankingapp.service.interfaceClass.RedirectLoggedClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class LoginController {

    @Autowired
    private final RedirectLoggedClientImpl redirectLoggedClient;

    public LoginController(RedirectLoggedClientImpl redirectLoggedClient) {
        this.redirectLoggedClient = redirectLoggedClient;
    }

    @GetMapping("/login")
    public String viewLoginPage() {
        if(redirectLoggedClient.isAuthenticated()){
            return "redirect:/hello";
        }
        return "loginTemplates/login";
    }
}
