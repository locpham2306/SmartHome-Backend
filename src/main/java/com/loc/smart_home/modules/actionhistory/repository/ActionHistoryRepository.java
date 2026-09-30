package com.loc.smart_home.modules.actionhistory.repository;

import com.loc.smart_home.modules.actionhistory.entity.ActionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActionHistoryRepository extends JpaRepository<ActionHistory,Long> {
}
