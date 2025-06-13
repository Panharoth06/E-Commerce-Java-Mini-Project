package model.service;

import model.entities.User;
import model.repository.UserRepositoryImpl;

public class UserServiceImpl {
    private final UserRepositoryImpl userRepository = new UserRepositoryImpl();

    public boolean register(User user) {
        return userRepository.register(user);
    }

    public boolean login(String username, String password) {
        return userRepository.login(username, password) != null;
    }
}