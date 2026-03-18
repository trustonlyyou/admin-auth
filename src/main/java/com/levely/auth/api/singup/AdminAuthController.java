package com.levely.auth.api.singup;

import com.levely.auth.api.singup.request.SingUpInitRequest;
import com.levely.auth.api.singup.response.SingUpInitResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/admin/auth/signup")
public class AdminAuthController {

    @GetMapping(value = "/init")
    public ResponseEntity<SingUpInitResponse> singUpInit(SingUpInitRequest request) {
        return null;
    }
}
