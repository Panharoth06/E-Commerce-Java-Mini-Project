package model.mapper;

import model.dto.UserResponseDto;
import model.entities.User;

public class UserMapper {
    public static UserResponseDto fromUserToUserResponseDto(User user) {
        return UserResponseDto
                .builder()
                .userName(user.getUserName())
                .email(user.getEmail())
                .userUuid(user.getUserUuid())
                .isDeleted(user.getIsDeleted())
                .build();
    }
}
