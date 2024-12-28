package com.ecomerce.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.ecomerce.model.User;

import jakarta.servlet.http.HttpSession;

@ControllerAdvice
public class GlobalController {

    @ModelAttribute("loggedInUser")
    public User addLoggedInUserToModel(HttpSession session) {
        return (User) session.getAttribute("loggedInUser");
    }
}
