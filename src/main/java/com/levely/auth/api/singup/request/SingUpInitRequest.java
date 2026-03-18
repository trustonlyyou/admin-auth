package com.levely.auth.api.singup.request;

import jakarta.validation.constraints.NotBlank;

public record SingUpInitRequest(
        @NotBlank(message = "이메일은 필수 입니다.")
        String email,

        @NotBlank(message = "패스워드는 필수 입니다.")
        String password,

        @NotBlank(message = "이름은 필수 입니다.")
        String name,

        @NotBlank(message = "핸드폰번호는 필수 입니다.")
        String phone
) {
}
