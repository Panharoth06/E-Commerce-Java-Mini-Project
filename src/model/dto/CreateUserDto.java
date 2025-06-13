package model.dto;

public record CreateUserDto(
        String userName,
        String email,
        String password
) { }
