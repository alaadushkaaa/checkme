CREATE OR REPLACE FUNCTION best_solution(id_task UUID, id_user UUID)
RETURNS INTEGER AS $$
BEGIN
    RETURN(
        SELECT MAX(total_score(c.id))
        FROM checks c
        WHERE c.task_id = id_task AND c.user_id = id_user
    );
END;
$$ LANGUAGE plpgsql;