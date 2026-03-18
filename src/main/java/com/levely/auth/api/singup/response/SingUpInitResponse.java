package com.levely.auth.api.singup.response;

public record SingUpInitResponse(
        String adminNo,
        String email,
        String otpSecretIssued,
        String status
) {
}
