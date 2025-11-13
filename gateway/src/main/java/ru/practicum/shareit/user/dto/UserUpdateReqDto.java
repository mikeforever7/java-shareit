package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateReqDto {

    private String name;

    @Email
    private String email;
}