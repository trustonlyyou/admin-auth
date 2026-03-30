package com.levely.auth.domain.service.admin;

import com.levely.auth.domain.repository.admin.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final AdminUserRepository adminUserRepository;

    /**
     * 사용자 중복 체크
     * @param email 사용자 이메일
     * @param phone 사용자 핸드폰
     * @return boolean
     */
    public boolean isUser(String email, String phone) {
        return adminUserRepository.isUserByEmailOrPhone(email, phone);
    }
}
