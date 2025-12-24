CREATE OR REPLACE FUNCTION score(row2_id INTEGER)
RETURNS INTEGER AS $$
DECLARE
    total_score INTEGER := 0;
    result_jsonb JSONB;
BEGIN
    SELECT result
    INTO result_jsonb
    FROM checks
    WHERE id = row2_id;
    SELECT COALESCE(SUM((value->>'score')::INTEGER), 0)
    INTO total_score
    FROM jsonb_each(result_jsonb);
    RETURN total_score;
END;
$$ LANGUAGE plpgsql;