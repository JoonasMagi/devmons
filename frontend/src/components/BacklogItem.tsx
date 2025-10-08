import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { GripVertical, Calendar, User, ArrowUp, ArrowDown, Minus } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import type { Issue } from '../types/issue';

interface BacklogItemProps {
  issue: Issue;
  isDragging?: boolean;
}

const priorityConfig = {
  CRITICAL: { color: 'text-red-600', bg: 'bg-red-50', icon: ArrowUp, label: 'Critical' },
  HIGH: { color: 'text-orange-600', bg: 'bg-orange-50', icon: ArrowUp, label: 'High' },
  MEDIUM: { color: 'text-yellow-600', bg: 'bg-yellow-50', icon: Minus, label: 'Medium' },
  LOW: { color: 'text-green-600', bg: 'bg-green-50', icon: ArrowDown, label: 'Low' },
};

export function BacklogItem({ issue, isDragging = false }: BacklogItemProps) {
  const navigate = useNavigate();
  
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging: isSortableDragging,
  } = useSortable({ id: issue.id });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging || isSortableDragging ? 0.5 : 1,
  };

  const priorityInfo = priorityConfig[issue.priority];
  const PriorityIcon = priorityInfo.icon;

  const handleClick = () => {
    navigate(`/projects/${issue.projectId}/issues/${issue.id}`);
  };

  return (
    <div
      ref={setNodeRef}
      style={style}
      className={`p-4 hover:bg-gray-50 transition-colors ${
        isDragging ? 'shadow-lg bg-white border border-gray-300 rounded-lg' : ''
      }`}
    >
      <div className="flex items-center space-x-4">
        {/* Drag Handle */}
        <div
          {...attributes}
          {...listeners}
          className="flex-shrink-0 cursor-grab active:cursor-grabbing text-gray-400 hover:text-gray-600"
        >
          <GripVertical className="w-5 h-5" />
        </div>

        {/* Issue Type Icon */}
        <div className="flex-shrink-0">
          <span className="text-lg" title={issue.issueType.name}>
            {issue.issueType.icon}
          </span>
        </div>

        {/* Issue Key */}
        <div className="flex-shrink-0">
          <span className="text-sm font-mono text-gray-500">{issue.key}</span>
        </div>

        {/* Issue Content */}
        <div className="flex-1 min-w-0">
          <div className="flex items-start justify-between">
            <div className="flex-1 min-w-0">
              <h3
                className="text-sm font-medium text-gray-900 cursor-pointer hover:text-primary-600 truncate"
                onClick={handleClick}
                title={issue.title}
              >
                {issue.title}
              </h3>
              {issue.description && (
                <p className="text-sm text-gray-500 mt-1 line-clamp-2">
                  {issue.description}
                </p>
              )}
            </div>

            {/* Issue Metadata */}
            <div className="flex items-center space-x-4 ml-4 flex-shrink-0">
              {/* Priority */}
              <div className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${priorityInfo.bg} ${priorityInfo.color}`}>
                <PriorityIcon className="w-3 h-3 mr-1" />
                {priorityInfo.label}
              </div>

              {/* Story Points */}
              {issue.storyPoints && (
                <div className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-blue-50 text-blue-600">
                  {issue.storyPoints} pts
                </div>
              )}

              {/* Assignee */}
              {issue.assignee ? (
                <div className="flex items-center text-sm text-gray-600">
                  <User className="w-4 h-4 mr-1" />
                  <span className="truncate max-w-24" title={issue.assignee.fullName}>
                    {issue.assignee.fullName}
                  </span>
                </div>
              ) : (
                <div className="flex items-center text-sm text-gray-400">
                  <User className="w-4 h-4 mr-1" />
                  <span>Unassigned</span>
                </div>
              )}

              {/* Due Date */}
              {issue.dueDate && (
                <div className={`flex items-center text-sm ${
                  new Date(issue.dueDate) < new Date() ? 'text-red-600' : 'text-gray-600'
                }`}>
                  <Calendar className="w-4 h-4 mr-1" />
                  <span>
                    {new Date(issue.dueDate).toLocaleDateString()}
                  </span>
                </div>
              )}

              {/* Labels */}
              {issue.labels && issue.labels.length > 0 && (
                <div className="flex items-center space-x-1">
                  {issue.labels.slice(0, 3).map((label) => (
                    <span
                      key={label.id}
                      className="inline-block px-2 py-1 text-xs font-medium rounded-full text-white"
                      style={{ backgroundColor: label.color }}
                      title={label.name}
                    >
                      {label.name}
                    </span>
                  ))}
                  {issue.labels.length > 3 && (
                    <span className="text-xs text-gray-500">
                      +{issue.labels.length - 3}
                    </span>
                  )}
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
