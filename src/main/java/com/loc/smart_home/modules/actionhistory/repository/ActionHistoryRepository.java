package com.loc.smart_home.modules.actionhistory.repository;

import com.loc.smart_home.modules.actionhistory.entity.ActionHistory;
import com.loc.smart_home.modules.actionhistory.enums.Action;
import com.loc.smart_home.modules.actionhistory.enums.ActionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ActionHistoryRepository extends JpaRepository<ActionHistory,Long> {
    @EntityGraph(attributePaths = {"device","user"})
    @Query(value = ActionHistoryQueries.SEARCH,
    countQuery = ActionHistoryQueries.COUNT_SEARCH)
    Page<ActionHistory> search(
            @Param("time") String time,
            @Param("device") String device,
            @Param("action") Action action,
            @Param("actionStatus") ActionStatus actionStatus,
            Pageable pageable
    );

    boolean existsByDevice_IdAndStatus(
            Integer deviceId,
            ActionStatus status
    );
}
