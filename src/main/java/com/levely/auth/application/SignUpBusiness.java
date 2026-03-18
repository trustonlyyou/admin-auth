package com.levely.auth.application;

import com.levely.auth.api.singup.request.SingUpInitRequest;
import com.levely.auth.api.singup.response.SingUpInitResponse;
import org.springframework.stereotype.Service;

@Service
public class SignUpBusiness {

    public SingUpInitResponse singUpInit(SingUpInitRequest request) {
        // 1. 이메일 / 휴대폰 중복 체크

        // 2. 비밀번호 암호화

        // 3. OTP Secret 생성

        // 4. 관리자 계정 임시 생성

        // 5. QR 등록 optAuth URL 생성

        // 6. 가입상태를 "PENDING" 으로 저장

        return null;
    }
}
