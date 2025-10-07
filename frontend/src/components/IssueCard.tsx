import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { CheckSquare, Bug, ListTodo, Layers, User } from 'lucide-react';
import type { Issue } from '../types/issue';

interface IssueCardProps {
  issue: Issue;
  onClick: () => void;
}

const issueTypeIcons: Record<string, React.ReactNode> = {
  Story: <Layers className="w-4 h-4" />,
  Bug: <Bug className="w-4 h-4" />,
  Task: <CheckSquare className="w-4 h-4" />,
  Epic: <ListTodo className="w-4 h-4" />,
};

const priorityColors: Record<string, string> = {
  LOW: 'border-l-gray-400',
  MEDIUM: 'border-l-yellow-500',
  HIGH: 'border-l-orange-500',
  CRITICAL: 'border-l-red-600',
};

export function IssueCard({ issue, onClick }: IssueCardProps) {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id: issue.id });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.5 : 1,
  };

  return (
    <div
      ref={setNodeRef}
      style={style}
      {...attributes}
      {...listeners}
      onClick={onClick}
      className={`
        bg-white rounded-lg shadow-sm border-l-4 ${priorityColors[issue.priority]}
        border-r border-t border-b border-gray-200
        p-3 cursor-pointer
        hover:shadow-md transition-shadow duration-200
        ${isDragging ? 'cursor-grabbing' : 'cursor-grab'}
      `}
    >
      {/* Header */}
      <div className="flex items-start justify-between gap-2 mb-2">
        <div className="flex items-center gap-2 text-sm text-gray-600">
          <span className="text-primary-600">{issueTypeIcons[issue.issueType.name]}</span>
          <span className="font-mono font-medium">{issue.key}</span>
        </div>
        {issue.storyPoints && (
          <span className="px-2 py-0.5 bg-gray-100 text-gray-700 text-xs font-medium rounded">
            {issue.storyPoints}
          </span>
        )}
      </div>

      {/* Title */}
      <h4 className="text-sm font-medium text-gray-900 mb-2 line-clamp-2">
        {issue.title}
      </h4>

      {/* Labels */}
      {issue.labels.length > 0 && (
        <div className="flex flex-wrap gap-1 mb-2">
          {issue.labels.slice(0, 3).map((label) => (
            <span
              key={label.id}
              className="px-2 py-0.5 text-xs rounded-full"
              style={{
                backgroundColor: `${label.color}20`,
                color: label.color,
              }}
            >
              {label.name}
            </span>
          ))}
          {issue.labels.length > 3 && (
            <span className="px-2 py-0.5 text-xs text-gray-500">
              +{issue.labels.length - 3}
            </span>
          )}
        </div>
      )}

      {/* Footer */}
      <div className="flex items-center justify-between">
        {issue.assignee ? (
          <div className="flex items-center gap-2">
            <div className="w-6 h-6 bg-gradient-to-br from-primary-500 to-primary-600 rounded-full flex items-center justify-center">
              <span className="text-xs text-white font-medium">
                {issue.assignee.fullName.charAt(0).toUpperCase()}
              </span>
            </div>
            <span className="text-xs text-gray-600">{issue.assignee.fullName}</span>
          </div>
        ) : (
          <div className="flex items-center gap-1 text-xs text-gray-400">
            <User className="w-4 h-4" />
            <span>Unassigned</span>
          </div>
        )}
      </div>
    </div>
  );
}

