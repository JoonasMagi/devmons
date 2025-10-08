import { useState, useMemo, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  DndContext,
  DragOverlay,
  closestCorners,
  PointerSensor,
  useSensor,
  useSensors,
} from '@dnd-kit/core';
import type { DragStartEvent, DragEndEvent } from '@dnd-kit/core';
import { ArrowLeft, Settings, Search, Filter, Loader2, X } from 'lucide-react';
import toast, { Toaster } from 'react-hot-toast';
import { issueService } from '../services/issueService';
import { projectService } from '../services/projectService';
import { BoardColumn } from '../components/BoardColumn';
import { IssueCard } from '../components/IssueCard';
import { IssueDetailModal } from '../components/IssueDetailModal';
import { CreateIssueModal } from '../components/CreateIssueModal';
import type { Issue, WorkflowState, Priority } from '../types/issue';

export function Board() {
  const { projectId } = useParams<{ projectId: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [activeIssue, setActiveIssue] = useState<Issue | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [showFilters, setShowFilters] = useState(false);
  const [selectedAssignees, setSelectedAssignees] = useState<number[]>([]);
  const [selectedLabels, setSelectedLabels] = useState<number[]>([]);
  const [selectedTypes, setSelectedTypes] = useState<string[]>([]);
  const [selectedPriorities, setSelectedPriorities] = useState<Priority[]>([]);
  const [selectedIssueId, setSelectedIssueId] = useState<number | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [createIssueWorkflowStateId, setCreateIssueWorkflowStateId] = useState<number | undefined>();
  const [focusedCardIndex, setFocusedCardIndex] = useState<number>(-1);
  const [scrollPosition, setScrollPosition] = useState<number>(0);

  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: {
        distance: 8,
      },
    })
  );

  // Fetch project
  const { data: project } = useQuery({
    queryKey: ['project', projectId],
    queryFn: () => projectService.getProject(Number(projectId)),
    enabled: !!projectId,
  });

  // Fetch issues
  const { data: issues = [], isLoading, error } = useQuery({
    queryKey: ['issues', projectId],
    queryFn: () => issueService.getProjectIssues(Number(projectId)),
    enabled: !!projectId,
  });

  // Fetch workflow states
  const { data: workflowStates = [] } = useQuery({
    queryKey: ['workflow-states', projectId],
    queryFn: () => projectService.getWorkflowStates(Number(projectId)),
    enabled: !!projectId,
  });

  // Update issue mutation
  const updateIssueMutation = useMutation({
    mutationFn: ({ id, workflowStateId }: { id: number; workflowStateId: number }) =>
      issueService.updateIssue(id, { workflowStateId }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['issues', projectId] });
      toast.success('Issue updated successfully');
    },
    onError: () => {
      queryClient.invalidateQueries({ queryKey: ['issues', projectId] });
      toast.error('Failed to update issue');
    },
  });

  // Group issues by workflow state
  const columns = useMemo(() => {
    const issuesByState = new Map<number, Issue[]>();

    // Group issues by workflow state
    issues.forEach((issue) => {
      if (!issuesByState.has(issue.workflowState.id)) {
        issuesByState.set(issue.workflowState.id, []);
      }
      issuesByState.get(issue.workflowState.id)!.push(issue);
    });

    // Use workflow states from API (already sorted by order)
    return workflowStates.map((state) => ({
      workflowState: state,
      issues: issuesByState.get(state.id) || [],
    }));
  }, [issues, workflowStates]);

  // Get unique assignees, labels, and types for filters
  const filterOptions = useMemo(() => {
    const assignees = new Map();
    const labels = new Map();
    const types = new Set<string>();

    issues.forEach((issue) => {
      if (issue.assignee) {
        assignees.set(issue.assignee.id, issue.assignee);
      }
      issue.labels.forEach((label) => labels.set(label.id, label));
      types.add(issue.issueType.name);
    });

    return {
      assignees: Array.from(assignees.values()),
      labels: Array.from(labels.values()),
      types: Array.from(types),
    };
  }, [issues]);

  // Filter issues by search query and filters
  const filteredColumns = useMemo(() => {
    return columns.map((column) => ({
      ...column,
      issues: column.issues.filter((issue) => {
        // Search filter
        if (searchQuery.trim()) {
          const query = searchQuery.toLowerCase();
          const matchesSearch =
            issue.title.toLowerCase().includes(query) ||
            issue.key.toLowerCase().includes(query);
          if (!matchesSearch) return false;
        }

        // Assignee filter
        if (selectedAssignees.length > 0) {
          if (!issue.assignee || !selectedAssignees.includes(issue.assignee.id)) {
            return false;
          }
        }

        // Label filter
        if (selectedLabels.length > 0) {
          const hasLabel = issue.labels.some((label) =>
            selectedLabels.includes(label.id)
          );
          if (!hasLabel) return false;
        }

        // Type filter
        if (selectedTypes.length > 0) {
          if (!selectedTypes.includes(issue.issueType.name)) {
            return false;
          }
        }

        // Priority filter
        if (selectedPriorities.length > 0) {
          if (!selectedPriorities.includes(issue.priority)) {
            return false;
          }
        }

        return true;
      }),
    }));
  }, [columns, searchQuery, selectedAssignees, selectedLabels, selectedTypes, selectedPriorities]);

  const handleDragStart = (event: DragStartEvent) => {
    const issue = issues.find((i) => i.id === event.active.id);
    setActiveIssue(issue || null);
  };

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;
    setActiveIssue(null);

    if (!over) return;

    const issueId = active.id as number;
    const newWorkflowStateId = over.id as number;

    const issue = issues.find((i) => i.id === issueId);
    if (!issue || issue.workflowState.id === newWorkflowStateId) return;

    // Optimistic update
    updateIssueMutation.mutate({ id: issueId, workflowStateId: newWorkflowStateId });
  };

  const handleIssueClick = (issue: Issue) => {
    // Save scroll position before opening modal
    setScrollPosition(window.scrollY);
    setSelectedIssueId(issue.id);
    setIsModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setSelectedIssueId(null);
    // Restore scroll position after closing modal
    setTimeout(() => {
      window.scrollTo(0, scrollPosition);
    }, 0);
  };

  const handleCreateIssue = (workflowStateId: number) => {
    setCreateIssueWorkflowStateId(workflowStateId);
    setIsCreateModalOpen(true);
  };

  const clearFilters = () => {
    setSearchQuery('');
    setSelectedAssignees([]);
    setSelectedLabels([]);
    setSelectedTypes([]);
    setSelectedPriorities([]);
  };

  const hasActiveFilters =
    searchQuery.trim() ||
    selectedAssignees.length > 0 ||
    selectedLabels.length > 0 ||
    selectedTypes.length > 0 ||
    selectedPriorities.length > 0;

  // Get all visible issues in order (left to right, top to bottom)
  const allVisibleIssues = useMemo(() => {
    const issues: Issue[] = [];
    filteredColumns.forEach((column) => {
      issues.push(...column.issues);
    });
    return issues;
  }, [filteredColumns]);

  // Keyboard navigation
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      const activeElement = document.activeElement;
      const isInputFocused = activeElement?.tagName === 'INPUT' || activeElement?.tagName === 'TEXTAREA';

      // / to focus search
      if (e.key === '/' && !e.ctrlKey && !e.metaKey) {
        e.preventDefault();
        document.getElementById('board-search')?.focus();
        return;
      }

      // Skip keyboard navigation if input is focused
      if (isInputFocused) return;

      // j to navigate down (next card)
      if (e.key === 'j') {
        e.preventDefault();
        const nextIndex = Math.min(focusedCardIndex + 1, allVisibleIssues.length - 1);
        setFocusedCardIndex(nextIndex);
        // Scroll to focused card
        const cardElement = document.querySelector(`[data-issue-id="${allVisibleIssues[nextIndex]?.id}"]`);
        cardElement?.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
      }

      // k to navigate up (previous card)
      if (e.key === 'k') {
        e.preventDefault();
        const prevIndex = Math.max(focusedCardIndex - 1, 0);
        setFocusedCardIndex(prevIndex);
        // Scroll to focused card
        const cardElement = document.querySelector(`[data-issue-id="${allVisibleIssues[prevIndex]?.id}"]`);
        cardElement?.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
      }

      // Enter to open focused card
      if (e.key === 'Enter' && focusedCardIndex >= 0 && focusedCardIndex < allVisibleIssues.length) {
        e.preventDefault();
        const focusedIssue = allVisibleIssues[focusedCardIndex];
        if (focusedIssue) {
          handleIssueClick(focusedIssue);
        }
      }

      // c to create new issue
      if (e.key === 'c' && !e.ctrlKey && !e.metaKey) {
        e.preventDefault();
        // Create in first column
        if (columns.length > 0) {
          handleCreateIssue(columns[0].workflowState.id);
        }
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [columns, focusedCardIndex, allVisibleIssues]);

  return (
    <div className="min-h-screen bg-gray-50">
      <Toaster position="top-right" />

      {/* Top Navigation */}
      <nav className="bg-white border-b border-gray-200 sticky top-0 z-30">
        <div className="px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center gap-4">
              <button
                onClick={() => navigate('/dashboard')}
                className="p-2 text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-lg transition"
              >
                <ArrowLeft className="w-5 h-5" />
              </button>
              <div>
                <h1 className="text-xl font-bold text-gray-900">
                  {project?.name || 'Project Board'}
                </h1>
                <p className="text-sm text-gray-500">{project?.key}</p>
              </div>
            </div>

            <div className="flex items-center gap-3">
              {/* Search */}
              <div className="relative">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
                <input
                  id="board-search"
                  type="text"
                  placeholder="Search issues... (/ to focus, j/k to navigate, Enter to open)"
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="pl-9 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent transition w-80"
                />
              </div>

              {/* Filter Button */}
              <button
                onClick={() => setShowFilters(!showFilters)}
                className={`p-2 rounded-lg transition ${
                  showFilters || hasActiveFilters
                    ? 'bg-primary-100 text-primary-700'
                    : 'text-gray-600 hover:text-gray-900 hover:bg-gray-100'
                }`}
              >
                <Filter className="w-5 h-5" />
              </button>

              {/* Clear Filters */}
              {hasActiveFilters && (
                <button
                  onClick={clearFilters}
                  className="flex items-center gap-2 px-3 py-2 text-sm text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-lg transition"
                >
                  <X className="w-4 h-4" />
                  <span className="hidden sm:inline">Clear filters</span>
                </button>
              )}

              {/* Settings */}
              <button
                onClick={() => navigate(`/projects/${projectId}/settings`)}
                className="flex items-center gap-2 px-4 py-2 text-gray-700 hover:text-gray-900 hover:bg-gray-100 rounded-lg transition"
              >
                <Settings className="w-5 h-5" />
                <span className="hidden sm:inline">Settings</span>
              </button>
            </div>
          </div>
        </div>
      </nav>

      {/* Filter Panel */}
      {showFilters && (
        <div className="bg-white border-b border-gray-200 px-4 sm:px-6 lg:px-8 py-4">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            {/* Assignee Filter */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Assignee
              </label>
              <select
                multiple
                value={selectedAssignees.map(String)}
                onChange={(e) =>
                  setSelectedAssignees(
                    Array.from(e.target.selectedOptions, (option) => Number(option.value))
                  )
                }
                className="w-full border border-gray-300 rounded-lg p-2 text-sm"
                size={3}
              >
                {filterOptions.assignees.map((assignee) => (
                  <option key={assignee.id} value={assignee.id}>
                    {assignee.fullName}
                  </option>
                ))}
              </select>
            </div>

            {/* Label Filter */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Labels
              </label>
              <select
                multiple
                value={selectedLabels.map(String)}
                onChange={(e) =>
                  setSelectedLabels(
                    Array.from(e.target.selectedOptions, (option) => Number(option.value))
                  )
                }
                className="w-full border border-gray-300 rounded-lg p-2 text-sm"
                size={3}
              >
                {filterOptions.labels.map((label) => (
                  <option key={label.id} value={label.id}>
                    {label.name}
                  </option>
                ))}
              </select>
            </div>

            {/* Type Filter */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Issue Type
              </label>
              <div className="space-y-2">
                {filterOptions.types.map((type) => (
                  <label key={type} className="flex items-center gap-2">
                    <input
                      type="checkbox"
                      checked={selectedTypes.includes(type)}
                      onChange={(e) => {
                        if (e.target.checked) {
                          setSelectedTypes([...selectedTypes, type]);
                        } else {
                          setSelectedTypes(selectedTypes.filter((t) => t !== type));
                        }
                      }}
                      className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                    />
                    <span className="text-sm text-gray-700">{type}</span>
                  </label>
                ))}
              </div>
            </div>

            {/* Priority Filter */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Priority
              </label>
              <div className="space-y-2">
                {(['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'] as Priority[]).map((priority) => (
                  <label key={priority} className="flex items-center gap-2">
                    <input
                      type="checkbox"
                      checked={selectedPriorities.includes(priority)}
                      onChange={(e) => {
                        if (e.target.checked) {
                          setSelectedPriorities([...selectedPriorities, priority]);
                        } else {
                          setSelectedPriorities(
                            selectedPriorities.filter((p) => p !== priority)
                          );
                        }
                      }}
                      className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                    />
                    <span className="text-sm text-gray-700">{priority}</span>
                  </label>
                ))}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Main Content */}
      <main className="p-6">
        {isLoading && (
          <div className="flex items-center justify-center py-12">
            <Loader2 className="w-8 h-8 text-primary-600 animate-spin" />
          </div>
        )}

        {error && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-4 text-red-700">
            Failed to load board. Please try again.
          </div>
        )}

        {!isLoading && !error && (
          <DndContext
            sensors={sensors}
            collisionDetection={closestCorners}
            onDragStart={handleDragStart}
            onDragEnd={handleDragEnd}
          >
            <div className="flex gap-4 overflow-x-auto pb-4">
              {filteredColumns.map((column) => (
                <BoardColumn
                  key={column.workflowState.id}
                  workflowState={column.workflowState}
                  issues={column.issues}
                  onIssueClick={handleIssueClick}
                  onCreateIssue={() => handleCreateIssue(column.workflowState.id)}
                  focusedIssueId={focusedCardIndex >= 0 ? allVisibleIssues[focusedCardIndex]?.id : undefined}
                />
              ))}
            </div>

            <DragOverlay>
              {activeIssue && (
                <div className="rotate-3">
                  <IssueCard issue={activeIssue} onClick={() => {}} />
                </div>
              )}
            </DragOverlay>
          </DndContext>
        )}
      </main>

      {/* Issue Detail Modal */}
      <IssueDetailModal
        issueId={selectedIssueId}
        isOpen={isModalOpen}
        onClose={handleCloseModal}
      />

      {/* Create Issue Modal */}
      <CreateIssueModal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        projectId={Number(projectId)}
        workflowStateId={createIssueWorkflowStateId}
      />
    </div>
  );
}

