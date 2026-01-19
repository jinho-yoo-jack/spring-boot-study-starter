package com.study.myspringstudydiary.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserRequest {

    @NotNull(message = "User name must not be null")
    @Size(min = 2, max = 100, message = "User name must be between 2 and 100 characters")
    @JsonProperty("user_name")
    private String userName;

    @Email(message = "Email must be valid")
    private String email;
}