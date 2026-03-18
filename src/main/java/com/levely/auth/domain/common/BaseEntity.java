package com.levely.auth.domain.common;


import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity {

    /* 생성일 */
    @CreatedDate
    @Column(name = "CREATE_TIME", nullable = false, updatable = false)
    private LocalDateTime createTime;

    /* 수정일 */
    @LastModifiedDate
    @Column(name = "UPDATE_TIME", nullable = false, updatable = true)
    private LocalDateTime updateTime;

    /* 생성자 */
    @Column(name = "CREATE_USER")
    private String createUser;

    /* 수정자 */
    @Column(name = "UPDATE_USER")
    private String updateUser;

}
