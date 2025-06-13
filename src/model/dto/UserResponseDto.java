package model.dto;
public record UserResponseDto(
        String userUuid,
        String userName,
        String email,
        String message
) {}
