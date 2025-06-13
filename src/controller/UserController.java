package controller;

import model.entities.User;
import model.service.UserServiceImpl;

public class UserController {
    private final UserServiceImpl userService = new UserServiceImpl();

    public boolean register(String username, String email, String password) {
        User user = User.builder()
                .userName(username)
                .email(email)
                .password(password)
                .build();
        return userService.register(user);
    }

    public boolean login(String username, String password) {
        return userService.login(username, password);
    }
}