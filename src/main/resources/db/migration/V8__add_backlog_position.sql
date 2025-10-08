-- Add backlog_position column to issues table for backlog prioritization
-- This is separate from board_position which is for ordering within workflow columns

-- Add backlog_position column
ALTER TABLE issues ADD COLUMN backlog_position INTEGER;

-- Create index for efficient backlog ordering
CREATE INDEX idx_issues_backlog_position ON issues(project_id, backlog_position);

-- Set initial backlog positions based on creation order
-- Issues created earlier get lower position numbers (higher priority)
UPDATE issues 
SET backlog_position = subquery.row_num * 1000
FROM (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY project_id ORDER BY created_at) as row_num
    FROM issues
) AS subquery
WHERE issues.id = subquery.id;
