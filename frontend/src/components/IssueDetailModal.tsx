import { Fragment, useState, useEffect } from 'react';
import { Dialog, Transition } from '@headlessui/react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate, useParams } from 'react-router-dom';
import {
  X,
  CheckSquare,
  Bug,
  ListTodo,
  Layers,
  User,
  Calendar,
  Tag,
  Clock,
  Link as LinkIcon,
  Trash2,
} from 'lucide-react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import DatePicker from 'react-datepicker';
import 'react-datepicker/dist/react-datepicker.css';
import toast from 'react-hot-toast';
import { issueService } from '../services/issueService';
import type { Issue, Priority, UpdateIssueRequest } from '../types/issue';

interface IssueDetailModalProps {
  issueId: number | null;
  isOpen: boolean;
  onClose: () => void;
}

const issueTypeIcons: Record<string, React.ReactNode> = {
  Story: <Layers className="w-5 h-5" />,
  Bug: <Bug className="w-5 h-5" />,
  Task: <CheckSquare className="w-5 h-5" />,
  Epic: <ListTodo className="w-5 h-5" />,
};

const priorityColors: Record<Priority, string> = {
  LOW: 'bg-gray-100 text-gray-700',
  MEDIUM: 'bg-yellow-100 text-yellow-700',
  HIGH: 'bg-orange-100 text-orange-700',
  CRITICAL: 'bg-red-100 text-red-700',
};

export function IssueDetailModal({ issueId, isOpen, onClose }: IssueDetailModalProps) {
  const { projectId } = useParams<{ projectId: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [isEditingDescription, setIsEditingDescription] = useState(false);
  const [editedTitle, setEditedTitle] = useState('');
  const [editedDescription, setEditedDescription] = useState('');
  const [hasUnsavedChanges, setHasUnsavedChanges] = useState(false);

  // Fetch issue data
  const { data: issue, isLoading } = useQuery({
    queryKey: ['issue', issueId],
    queryFn: () => issueService.getIssue(issueId!),
    enabled: !!issueId && isOpen,
  });

  // Fetch issue history
  const { data: history = [] } = useQuery({
    queryKey: ['issue-history', issueId],
    queryFn: () => issueService.getIssueHistory(issueId!),
    enabled: !!issueId && isOpen,
  });

  // Fetch workflow states
  const { data: workflowStates = [] } = useQuery({
    queryKey: ['workflow-states', projectId],
    queryFn: () => issueService.getWorkflowStates(Number(projectId)),
    enabled: !!projectId && isOpen,
  });

  // Update issue mutation
  const updateIssueMutation = useMutation({
    mutationFn: (data: UpdateIssueRequest) => issueService.updateIssue(issueId!, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['issue', issueId] });
      queryClient.invalidateQueries({ queryKey: ['issues', projectId] });
      queryClient.invalidateQueries({ queryKey: ['issue-history', issueId] });
      toast.success('Issue updated successfully');
      setHasUnsavedChanges(false);
    },
    onError: () => {
      toast.error('Failed to update issue');
    },
  });

  // Initialize edited values when issue loads
  useEffect(() => {
    if (issue) {
      setEditedTitle(issue.title);
      setEditedDescription(issue.description || '');
    }
  }, [issue]);

  // Update URL with issue key
  useEffect(() => {
    if (issue && isOpen) {
      navigate(`/projects/${projectId}/board?issue=${issue.key}`, { replace: true });
    }
  }, [issue, isOpen, projectId, navigate]);

  const handleClose = () => {
    if (hasUnsavedChanges) {
      if (!confirm('You have unsaved changes. Are you sure you want to close?')) {
        return;
      }
    }
    navigate(`/projects/${projectId}/board`, { replace: true });
    onClose();
  };

  const handleTitleBlur = () => {
    if (editedTitle !== issue?.title && editedTitle.trim()) {
      updateIssueMutation.mutate({ title: editedTitle });
    }
  };

  const handleDescriptionSave = () => {
    if (editedDescription !== issue?.description) {
      updateIssueMutation.mutate({ description: editedDescription });
    }
    setIsEditingDescription(false);
  };

  const handlePriorityChange = (priority: Priority) => {
    updateIssueMutation.mutate({ priority });
  };

  const handleDueDateChange = (date: Date | null) => {
    updateIssueMutation.mutate({ dueDate: date?.toISOString() || undefined });
  };

  const handleStoryPointsChange = (storyPoints: number | undefined) => {
    updateIssueMutation.mutate({ storyPoints });
  };

  const copyLink = () => {
    const url = `${window.location.origin}/projects/${projectId}/board?issue=${issue?.key}`;
    navigator.clipboard.writeText(url);
    toast.success('Link copied to clipboard');
  };

  const handleDelete = () => {
    if (!confirm(`Are you sure you want to delete ${issue?.key}? This action cannot be undone.`)) {
      return;
    }
    // TODO: Implement delete API call
    toast.error('Delete functionality not yet implemented');
  };

  // Keyboard shortcuts
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (!isOpen) return;

      if (e.key === 'Escape') {
        handleClose();
      } else if (e.key === 'e' && !e.ctrlKey && !e.metaKey) {
        const activeElement = document.activeElement;
        if (activeElement?.tagName !== 'INPUT' && activeElement?.tagName !== 'TEXTAREA') {
          e.preventDefault();
          setIsEditingDescription(true);
        }
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, hasUnsavedChanges]);

  if (!issueId) return null;

  return (
    <Transition appear show={isOpen} as={Fragment}>
      <Dialog as="div" className="relative z-50" onClose={handleClose}>
        <Transition.Child
          as={Fragment}
          enter="ease-out duration-300"
          enterFrom="opacity-0"
          enterTo="opacity-100"
          leave="ease-in duration-200"
          leaveFrom="opacity-100"
          leaveTo="opacity-0"
        >
          <div className="fixed inset-0 bg-black bg-opacity-25" />
        </Transition.Child>

        <div className="fixed inset-0 overflow-y-auto">
          <div className="flex min-h-full items-center justify-center p-4 text-center">
            <Transition.Child
              as={Fragment}
              enter="ease-out duration-300"
              enterFrom="opacity-0 scale-95"
              enterTo="opacity-100 scale-100"
              leave="ease-in duration-200"
              leaveFrom="opacity-100 scale-100"
              leaveTo="opacity-0 scale-95"
            >
              <Dialog.Panel className="w-full max-w-4xl transform overflow-hidden rounded-2xl bg-white text-left align-middle shadow-xl transition-all">
                {isLoading ? (
                  <div className="p-12 text-center">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto"></div>
                  </div>
                ) : issue ? (
                  <div className="flex flex-col max-h-[90vh]">
                    {/* Header */}
                    <div className="flex items-start justify-between p-6 border-b border-gray-200">
                      <div className="flex items-center gap-3 flex-1">
                        <span className="text-primary-600">{issueTypeIcons[issue.issueType.name]}</span>
                        <span className="font-mono text-sm text-gray-600">{issue.key}</span>
                        <input
                          type="text"
                          value={editedTitle}
                          onChange={(e) => {
                            setEditedTitle(e.target.value);
                            setHasUnsavedChanges(true);
                          }}
                          onBlur={handleTitleBlur}
                          className="flex-1 text-xl font-semibold text-gray-900 border-none focus:ring-2 focus:ring-primary-500 rounded px-2 py-1"
                        />
                      </div>
                      <button
                        onClick={handleClose}
                        className="p-2 text-gray-400 hover:text-gray-600 rounded-lg hover:bg-gray-100 transition"
                      >
                        <X className="w-5 h-5" />
                      </button>
                    </div>

                    {/* Body */}
                    <div className="flex-1 overflow-y-auto p-6">
                      <div className="grid grid-cols-3 gap-6">
                        {/* Main Content */}
                        <div className="col-span-2 space-y-6">
                          {/* Description */}
                          <div>
                            <div className="flex items-center justify-between mb-2">
                              <h3 className="text-sm font-medium text-gray-700">Description</h3>
                              <button
                                onClick={() => setIsEditingDescription(!isEditingDescription)}
                                className="text-sm text-primary-600 hover:text-primary-700"
                              >
                                {isEditingDescription ? 'Preview' : 'Edit'}
                              </button>
                            </div>
                            {isEditingDescription ? (
                              <div>
                                <textarea
                                  value={editedDescription}
                                  onChange={(e) => {
                                    setEditedDescription(e.target.value);
                                    setHasUnsavedChanges(true);
                                  }}
                                  className="w-full h-64 p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent font-mono text-sm"
                                  placeholder="Add a description..."
                                />
                                <div className="flex gap-2 mt-2">
                                  <button
                                    onClick={handleDescriptionSave}
                                    className="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition"
                                  >
                                    Save
                                  </button>
                                  <button
                                    onClick={() => {
                                      setEditedDescription(issue.description || '');
                                      setIsEditingDescription(false);
                                      setHasUnsavedChanges(false);
                                    }}
                                    className="px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-lg transition"
                                  >
                                    Cancel
                                  </button>
                                </div>
                              </div>
                            ) : (
                              <div className="prose prose-sm max-w-none p-3 border border-gray-200 rounded-lg min-h-[100px]">
                                {issue.description ? (
                                  <ReactMarkdown remarkPlugins={[remarkGfm]}>
                                    {issue.description}
                                  </ReactMarkdown>
                                ) : (
                                  <p className="text-gray-400 italic">No description provided</p>
                                )}
                              </div>
                            )}
                          </div>

                          {/* Activity Timeline */}
                          <div>
                            <h3 className="text-sm font-medium text-gray-700 mb-3">Activity</h3>
                            <div className="space-y-3">
                              {history.slice(0, 10).map((entry) => (
                                <div key={entry.id} className="flex gap-3">
                                  <div className="w-8 h-8 bg-gradient-to-br from-primary-500 to-primary-600 rounded-full flex items-center justify-center flex-shrink-0">
                                    <span className="text-xs text-white font-medium">
                                      {entry.changedBy.fullName.charAt(0).toUpperCase()}
                                    </span>
                                  </div>
                                  <div className="flex-1">
                                    <p className="text-sm text-gray-900">
                                      <span className="font-medium">{entry.changedBy.fullName}</span>
                                      {' '}changed{' '}
                                      <span className="font-medium">{entry.field}</span>
                                      {entry.oldValue && (
                                        <>
                                          {' '}from{' '}
                                          <span className="text-gray-600">{entry.oldValue}</span>
                                        </>
                                      )}
                                      {entry.newValue && (
                                        <>
                                          {' '}to{' '}
                                          <span className="text-gray-600">{entry.newValue}</span>
                                        </>
                                      )}
                                    </p>
                                    <p className="text-xs text-gray-500 mt-1">
                                      {new Date(entry.changedAt).toLocaleString()}
                                    </p>
                                  </div>
                                </div>
                              ))}
                              {history.length === 0 && (
                                <p className="text-sm text-gray-400 italic">No activity yet</p>
                              )}
                              {history.length > 10 && (
                                <button className="text-sm text-primary-600 hover:text-primary-700">
                                  Show more
                                </button>
                              )}
                            </div>
                          </div>
                        </div>

                        {/* Sidebar */}
                        <div className="space-y-4">
                          {/* Status */}
                          <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                              Status
                            </label>
                            <select
                              value={issue.workflowState.id}
                              onChange={(e) =>
                                updateIssueMutation.mutate({
                                  workflowStateId: Number(e.target.value),
                                })
                              }
                              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                            >
                              {workflowStates.map((state) => (
                                <option key={state.id} value={state.id}>
                                  {state.name}
                                </option>
                              ))}
                            </select>
                          </div>

                          {/* Priority */}
                          <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                              Priority
                            </label>
                            <select
                              value={issue.priority}
                              onChange={(e) => handlePriorityChange(e.target.value as Priority)}
                              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                            >
                              <option value="LOW">Low</option>
                              <option value="MEDIUM">Medium</option>
                              <option value="HIGH">High</option>
                              <option value="CRITICAL">Critical</option>
                            </select>
                          </div>

                          {/* Assignee */}
                          <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                              <User className="w-4 h-4 inline mr-1" />
                              Assignee
                            </label>
                            <div className="space-y-2">
                              {issue.assignee ? (
                                <div className="flex items-center gap-2 p-2 border border-gray-200 rounded-lg">
                                  <div className="w-8 h-8 bg-gradient-to-br from-primary-500 to-primary-600 rounded-full flex items-center justify-center">
                                    <span className="text-xs text-white font-medium">
                                      {issue.assignee.fullName.charAt(0).toUpperCase()}
                                    </span>
                                  </div>
                                  <div className="flex-1">
                                    <p className="text-sm font-medium text-gray-900">
                                      {issue.assignee.fullName}
                                    </p>
                                    <p className="text-xs text-gray-500">{issue.assignee.email}</p>
                                  </div>
                                  <button
                                    onClick={() => updateIssueMutation.mutate({ assigneeId: undefined })}
                                    className="text-xs text-red-600 hover:text-red-700"
                                  >
                                    Remove
                                  </button>
                                </div>
                              ) : (
                                <p className="text-sm text-gray-400 italic">Unassigned</p>
                              )}
                              <p className="text-xs text-gray-500">
                                Note: Assignee selector requires project members API
                              </p>
                            </div>
                          </div>

                          {/* Reporter */}
                          <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                              Reporter
                            </label>
                            <div className="flex items-center gap-2 p-2 border border-gray-200 rounded-lg bg-gray-50">
                              <div className="w-8 h-8 bg-gradient-to-br from-gray-500 to-gray-600 rounded-full flex items-center justify-center">
                                <span className="text-xs text-white font-medium">
                                  {issue.reporter.fullName.charAt(0).toUpperCase()}
                                </span>
                              </div>
                              <div className="flex-1">
                                <p className="text-sm font-medium text-gray-900">
                                  {issue.reporter.fullName}
                                </p>
                                <p className="text-xs text-gray-500">{issue.reporter.email}</p>
                              </div>
                            </div>
                          </div>

                          {/* Story Points */}
                          <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                              Story Points
                            </label>
                            <input
                              type="number"
                              value={issue.storyPoints || ''}
                              onChange={(e) =>
                                handleStoryPointsChange(
                                  e.target.value ? Number(e.target.value) : undefined
                                )
                              }
                              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                              placeholder="0"
                              min="0"
                            />
                          </div>

                          {/* Due Date */}
                          <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                              <Calendar className="w-4 h-4 inline mr-1" />
                              Due Date
                            </label>
                            <DatePicker
                              selected={issue.dueDate ? new Date(issue.dueDate) : null}
                              onChange={handleDueDateChange}
                              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                              placeholderText="Select date"
                              dateFormat="MMM d, yyyy"
                            />
                            {issue.dueDate && new Date(issue.dueDate) < new Date() && (
                              <p className="text-xs text-red-600 mt-1">Overdue</p>
                            )}
                          </div>

                          {/* Labels */}
                          <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                              <Tag className="w-4 h-4 inline mr-1" />
                              Labels
                            </label>
                            <div className="flex flex-wrap gap-1">
                              {issue.labels.map((label) => (
                                <span
                                  key={label.id}
                                  className="px-2 py-1 text-xs rounded-full"
                                  style={{
                                    backgroundColor: `${label.color}20`,
                                    color: label.color,
                                  }}
                                >
                                  {label.name}
                                </span>
                              ))}
                              {issue.labels.length === 0 && (
                                <p className="text-sm text-gray-400 italic">No labels</p>
                              )}
                            </div>
                          </div>

                          {/* Timestamps */}
                          <div className="pt-4 border-t border-gray-200">
                            <div className="flex items-center gap-2 text-xs text-gray-500 mb-1">
                              <Clock className="w-3 h-3" />
                              <span>Created {new Date(issue.createdAt).toLocaleString()}</span>
                            </div>
                            <div className="flex items-center gap-2 text-xs text-gray-500">
                              <Clock className="w-3 h-3" />
                              <span>Updated {new Date(issue.updatedAt).toLocaleString()}</span>
                            </div>
                          </div>

                          {/* Actions */}
                          <div className="pt-4 border-t border-gray-200 space-y-2">
                            <button
                              onClick={copyLink}
                              className="w-full flex items-center justify-center gap-2 px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 rounded-lg transition"
                            >
                              <LinkIcon className="w-4 h-4" />
                              Copy link
                            </button>
                            <button
                              onClick={handleDelete}
                              className="w-full flex items-center justify-center gap-2 px-4 py-2 text-sm text-red-600 hover:bg-red-50 rounded-lg transition"
                            >
                              <Trash2 className="w-4 h-4" />
                              Delete issue
                            </button>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                ) : null}
              </Dialog.Panel>
            </Transition.Child>
          </div>
        </div>
      </Dialog>
    </Transition>
  );
}

