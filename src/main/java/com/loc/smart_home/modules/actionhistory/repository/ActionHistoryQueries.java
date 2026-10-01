package com.loc.smart_home.modules.actionhistory.repository;

public class ActionHistoryQueries {
    private static final String SEARCH_FROM_WHERE = """
                FROM ActionHistory ah
                JOIN ah.device d
                WHERE
                (:time IS NULL OR FORMAT(ah.createdAt AS 'yyyy-MM-dd HH:mm:ss')
                LIKE :time ESCAPE '!')
                AND (:device IS NULL OR d.name = :device)
                AND (:action IS NULL OR ah.action = :action)
                AND (:actionStatus IS NULL OR ah.status = :actionStatus)
            """;

    public static final String SEARCH =
            "SELECT ah " + SEARCH_FROM_WHERE;
    public static final String COUNT_SEARCH =
            "SELECT COUNT(ah) " + SEARCH_FROM_WHERE;

    private ActionHistoryQueries(){
    }
}
