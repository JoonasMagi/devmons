import { useDroppable } from '@dnd-kit/core';
import { SortableContext, verticalListSortingStrategy } from '@dnd-kit/sortable';
import { Plus } from 'lucide-react';
import { IssueCard } from './IssueCard';
import type { WorkflowState, Issue } from '../types/issue';

interface BoardColumnProps {
  workflowState: WorkflowState;
  issues: Issue[];
  onIssueClick: (issue: Issue) => void;
  onCreateIssue: () => void;
  focusedIssueId?: number;
}

export function BoardColumn({ workflowState, issues, onIssueClick, onCreateIssue, focusedIssueId }: BoardColumnProps) {
  const { setNodeRef, isOver } = useDroppable({
    id: workflowState.id,
  });

  return (
    <div className="flex-shrink-0 w-80 bg-gray-50 rounded-lg">
      {/* Column Header */}
      <div className="p-4 border-b border-gray-200">
        <div className="flex items-center justify-between mb-2">
          <h3 className="font-semibold text-gray-900">{workflowState.name}</h3>
          <span className="px-2 py-1 bg-gray-200 text-gray-700 text-xs font-medium rounded-full">
            {issues.length}
          </span>
        </div>
        <button
          onClick={onCreateIssue}
          className="w-full flex items-center justify-center gap-2 px-3 py-2 text-sm text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-lg transition"
        >
          <Plus className="w-4 h-4" />
          <span>New Issue</span>
        </button>
      </div>

      {/* Column Content */}
      <div
        ref={setNodeRef}
        className={`p-4 space-y-3 min-h-[200px] max-h-[calc(100vh-300px)] overflow-y-auto ${
          isOver ? 'bg-primary-50' : ''
        }`}
      >
        <SortableContext items={issues.map(i => i.id)} strategy={verticalListSortingStrategy}>
          {issues.map((issue) => (
            <IssueCard
              key={issue.id}
              issue={issue}
              onClick={() => onIssueClick(issue)}
              isFocused={focusedIssueId === issue.id}
            />
          ))}
        </SortableContext>

        {issues.length === 0 && (
          <div className="text-center py-8 text-gray-400 text-sm">
            No issues
          </div>
        )}
      </div>
    </div>
  );
}

