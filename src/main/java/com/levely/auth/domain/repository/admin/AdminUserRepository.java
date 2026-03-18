package com.levely.auth.domain.repository.admin;

import com.levely.auth.domain.entity.admin.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {

    boolean isUserByEmailOrPhone(String email, String phone);
}
