package com.hato.infrastructure.persistence.entity;

import com.hato.domain.model.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "memberships", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "farm_id"})
})
@Getter
@Setter
@NoArgsConstructor
public class MembershipEntity {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(name = "farm_id", nullable = false, length = 64)
    private String farmId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role = Role.OWNER;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();
}
