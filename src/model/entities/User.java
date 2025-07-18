package model.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    Integer id;
    String userName;
    String email;
    String password;
    Boolean isDeleted;
    String userUuid;
    Date createAt;
}
