package com.levely.auth.domain.service.admin;

import com.levely.auth.domain.repository.admin.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final AdminUserRepository adminUserRepository;

    public boolean isUser(String email, String phone) {
        return adminUserRepository.isUserByEmailOrPhone(email, phone);
    }
}
