package com.bnp.invite;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InviteRepository extends JpaRepository<Invite, Long> {
    List<Invite> findByInviterIdOrderByCreatedAtDesc(Long inviterId);
    Optional<Invite> findByCode(String code);
    long countByInviterId(Long inviterId);
    boolean existsByInviterIdAndInviteeEmailIgnoreCase(Long inviterId, String inviteeEmail);
    void deleteByInviterId(Long inviterId);
}
