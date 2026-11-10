package com.hato.infrastructure.persistence.repository;

import com.hato.infrastructure.persistence.entity.MembershipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataMembershipRepository extends JpaRepository<MembershipEntity, String> {
    List<MembershipEntity> findByUserId(String userId);
    List<MembershipEntity> findByFarmId(String farmId);
}
