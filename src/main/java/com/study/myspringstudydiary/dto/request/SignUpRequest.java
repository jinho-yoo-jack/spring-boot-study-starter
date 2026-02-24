package com.study.myspringstudydiary.dto.request;

import com.study.myspringstudydiary.validation.PasswordMatch;

@PasswordMatch(passwordField = "passwd", confirmPasswordField = "passwdConfirm")
public class SignUpRequest {
    String passwd;
    String passwdConfirm;
}
