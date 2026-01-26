package com.study.myspringstudydiary.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserRequest {

    @JsonProperty("user_name")
    private String userName;

    private String email;
}