

DELETE FROM action_type
WHERE ctid NOT IN (
    SELECT min(ctid)
    FROM action_type
    GROUP BY action_name
);

ALTER TABLE action_type ADD CONSTRAINT uk_action_name UNIQUE (action_name);