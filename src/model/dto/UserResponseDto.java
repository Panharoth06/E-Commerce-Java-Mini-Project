package model.dto;

import lombok.Builder;

@Builder
public record UserResponseDto (
        String userName,
        String email,
        String userUuid,
        Boolean isDeleted
) { }
