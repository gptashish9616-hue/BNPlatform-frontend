package com.bnp.group;

import com.bnp.common.enums.Enums.GroupStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Long> {
    List<Group> findByStatus(GroupStatus status);
}
