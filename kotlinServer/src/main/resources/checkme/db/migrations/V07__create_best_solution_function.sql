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


CREATE OR REPLACE FUNCTION highest_score(id_task UUID)
RETURNS INTEGER AS $$
DECLARE
    highest_score INTEGER := 0;
    result_jsonb JSONB;
BEGIN
    SELECT criterions
    INTO result_jsonb
    FROM tasks
    WHERE id = id_task;
    SELECT COALESCE(SUM((value->>'score')::INTEGER), 0)
    INTO highest_score
    FROM jsonb_each(result_jsonb);
    RETURN highest_score;
END;
$$ LANGUAGE plpgsql;