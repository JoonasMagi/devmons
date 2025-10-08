-- Add board_position column to issues table
ALTER TABLE issues ADD COLUMN board_position INTEGER;

-- Add allowed_transitions column to workflow_states table
ALTER TABLE workflow_states ADD COLUMN allowed_transitions VARCHAR(500);

-- Create index on board_position for efficient ordering
CREATE INDEX idx_issues_board_position ON issues(workflow_state_id, board_position);

-- Set default board positions based on creation order within each workflow state
WITH ranked_issues AS (
    SELECT 
        id,
        ROW_NUMBER() OVER (PARTITION BY workflow_state_id ORDER BY created_at) as position
    FROM issues
)
UPDATE issues
SET board_position = ranked_issues.position
FROM ranked_issues
WHERE issues.id = ranked_issues.id;

