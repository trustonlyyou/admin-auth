package com.levely.auth.domain.entity;


import com.levely.auth.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ADMIN_USER")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUser extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_NO")
    private Long userNo;


}
