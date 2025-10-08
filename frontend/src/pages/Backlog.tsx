import { useState, useMemo } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { DndContext, DragOverlay } from '@dnd-kit/core';
import type { DragEndEvent, DragStartEvent } from '@dnd-kit/core';
import { SortableContext, verticalListSortingStrategy } from '@dnd-kit/sortable';
import { Search, Plus, Archive, ArrowLeft, Kanban } from 'lucide-react';
import toast from 'react-hot-toast';
import { projectService } from '../services/projectService';
import { issueService } from '../services/issueService';
import { BacklogItem } from '../components/BacklogItem';
import { CreateIssueModal } from '../components/CreateIssueModal';
import type { Issue, Priority, UpdateIssueRequest } from '../types/issue';

type FilterType = 'all' | 'story' | 'bug' | 'task' | 'epic';
type SortType = 'priority' | 'created' | 'updated' | 'assignee';

export function Backlog() {
  const { projectId } = useParams<{ projectId: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  
  const [searchQuery, setSearchQuery] = useState('');
  const [filterType, setFilterType] = useState<FilterType>('all');
  const [sortBy, setSortBy] = useState<SortType>('priority');
  const [selectedAssignee, setSelectedAssignee] = useState<string>('all');
  const [selectedPriority, setSelectedPriority] = useState<Priority | 'all'>('all');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [activeIssue, setActiveIssue] = useState<Issue | null>(null);

  // Fetch backlog issues
  const { data: backlogIssues = [], isLoading } = useQuery({
    queryKey: ['backlog', projectId],
    queryFn: () => projectService.getBacklog(Number(projectId)),
    enabled: !!projectId,
  });

  // Fetch project
  const { data: project } = useQuery({
    queryKey: ['project', projectId],
    queryFn: () => projectService.getProject(Number(projectId)),
    enabled: !!projectId,
  });

  // Fetch project members for filtering
  const { data: members = [] } = useQuery({
    queryKey: ['projectMembers', projectId],
    queryFn: () => projectService.getProjectMembers(Number(projectId)),
    enabled: !!projectId,
  });

  // Update issue mutation
  const updateIssueMutation = useMutation({
    mutationFn: ({ id, ...data }: { id: number } & Partial<UpdateIssueRequest>) => {
      console.log('Updating issue:', id, data);
      return issueService.updateIssue(id, data);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['backlog', projectId] });
      toast.success('Issue position updated');
    },
    onError: (error: any) => {
      console.error('Failed to update issue:', error);
      toast.error(error.response?.data?.message || error.message || 'Failed to update issue');
    },
  });

  // Filter and sort issues
  const filteredAndSortedIssues = useMemo(() => {
    let filtered = backlogIssues.filter((issue) => {
      // Search filter
      if (searchQuery) {
        const query = searchQuery.toLowerCase();
        if (!issue.title.toLowerCase().includes(query) && 
            !issue.key.toLowerCase().includes(query) &&
            !issue.description?.toLowerCase().includes(query)) {
          return false;
        }
      }

      // Type filter
      if (filterType !== 'all' && issue.issueType.name.toLowerCase() !== filterType) {
        return false;
      }

      // Assignee filter
      if (selectedAssignee !== 'all' && issue.assignee?.username !== selectedAssignee) {
        return false;
      }

      // Priority filter
      if (selectedPriority !== 'all' && issue.priority !== selectedPriority) {
        return false;
      }

      return true;
    });

    // Sort issues
    filtered.sort((a, b) => {
      switch (sortBy) {
        case 'priority':
          // Sort by backlog position (priority order)
          if (a.backlogPosition !== undefined && b.backlogPosition !== undefined) {
            return a.backlogPosition - b.backlogPosition;
          }
          if (a.backlogPosition !== undefined) return -1;
          if (b.backlogPosition !== undefined) return 1;
          return new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime();
        
        case 'created':
          return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
        
        case 'updated':
          return new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime();
        
        case 'assignee':
          const aAssignee = a.assignee?.fullName || 'Unassigned';
          const bAssignee = b.assignee?.fullName || 'Unassigned';
          return aAssignee.localeCompare(bAssignee);
        
        default:
          return 0;
      }
    });

    return filtered;
  }, [backlogIssues, searchQuery, filterType, sortBy, selectedAssignee, selectedPriority]);

  // Drag and drop handlers
  const handleDragStart = (event: DragStartEvent) => {
    const issue = backlogIssues.find((i) => i.id === event.active.id);
    setActiveIssue(issue || null);
  };

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;
    setActiveIssue(null);

    if (!over || active.id === over.id) return;

    const activeIssue = backlogIssues.find((i) => i.id === active.id);
    const overIssue = backlogIssues.find((i) => i.id === over.id);

    if (!activeIssue || !overIssue) return;

    // Calculate new backlog position
    const overIndex = filteredAndSortedIssues.findIndex((i) => i.id === over.id);

    let newPosition: number;
    if (overIndex === 0) {
      // Moving to top
      const firstIssue = filteredAndSortedIssues[0];
      newPosition = (firstIssue.backlogPosition || 0) - 1000;
    } else if (overIndex === filteredAndSortedIssues.length - 1) {
      // Moving to bottom
      const lastIssue = filteredAndSortedIssues[filteredAndSortedIssues.length - 1];
      newPosition = (lastIssue.backlogPosition || 0) + 1000;
    } else {
      // Moving between two issues
      const prevIssue = filteredAndSortedIssues[overIndex - 1];
      const nextIssue = filteredAndSortedIssues[overIndex];
      newPosition = ((prevIssue.backlogPosition || 0) + (nextIssue.backlogPosition || 0)) / 2;
    }

    // Update backlog position
    updateIssueMutation.mutate({
      id: activeIssue.id,
      backlogPosition: Math.round(newPosition),
    });
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <button
            onClick={() => navigate('/dashboard')}
            className="p-2 text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-lg transition"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>
          <div>
            <h1 className="text-2xl font-bold text-gray-900">
              {project?.name || 'Project'} - Backlog
            </h1>
            <p className="text-gray-600">Prioritize and manage your project backlog</p>
          </div>
        </div>
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigate(`/projects/${projectId}/board`)}
            className="inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
          >
            <Kanban className="w-4 h-4 mr-2" />
            Board View
          </button>
          <button
            onClick={() => setShowCreateModal(true)}
            className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-primary-600 hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
          >
            <Plus className="w-4 h-4 mr-2" />
            Create Issue
          </button>
        </div>
      </div>

      {/* Filters and Search */}
      <div className="bg-white rounded-lg border border-gray-200 p-4">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4">
          {/* Search */}
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
            <input
              type="text"
              placeholder="Search issues..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-primary-500 focus:border-transparent"
            />
          </div>

          {/* Type Filter */}
          <select
            value={filterType}
            onChange={(e) => setFilterType(e.target.value as FilterType)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-primary-500 focus:border-transparent"
          >
            <option value="all">All Types</option>
            <option value="story">Story</option>
            <option value="bug">Bug</option>
            <option value="task">Task</option>
            <option value="epic">Epic</option>
          </select>

          {/* Assignee Filter */}
          <select
            value={selectedAssignee}
            onChange={(e) => setSelectedAssignee(e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-primary-500 focus:border-transparent"
          >
            <option value="all">All Assignees</option>
            <option value="">Unassigned</option>
            {members.map((member) => (
              <option key={member.id} value={member.username}>
                {member.fullName}
              </option>
            ))}
          </select>

          {/* Priority Filter */}
          <select
            value={selectedPriority}
            onChange={(e) => setSelectedPriority(e.target.value as Priority | 'all')}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-primary-500 focus:border-transparent"
          >
            <option value="all">All Priorities</option>
            <option value="LOW">Low</option>
            <option value="MEDIUM">Medium</option>
            <option value="HIGH">High</option>
            <option value="CRITICAL">Critical</option>
          </select>

          {/* Sort */}
          <select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value as SortType)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-primary-500 focus:border-transparent"
          >
            <option value="priority">Priority Order</option>
            <option value="created">Recently Created</option>
            <option value="updated">Recently Updated</option>
            <option value="assignee">Assignee</option>
          </select>
        </div>
      </div>

      {/* Backlog List */}
      <div className="bg-white rounded-lg border border-gray-200">
        <div className="p-4 border-b border-gray-200">
          <h2 className="text-lg font-medium text-gray-900">
            Issues ({filteredAndSortedIssues.length})
          </h2>
        </div>

        <DndContext onDragStart={handleDragStart} onDragEnd={handleDragEnd}>
          <SortableContext items={filteredAndSortedIssues.map(i => i.id)} strategy={verticalListSortingStrategy}>
            <div className="divide-y divide-gray-200">
              {filteredAndSortedIssues.map((issue) => (
                <BacklogItem key={issue.id} issue={issue} />
              ))}
            </div>
          </SortableContext>

          <DragOverlay>
            {activeIssue ? <BacklogItem issue={activeIssue} isDragging /> : null}
          </DragOverlay>
        </DndContext>

        {filteredAndSortedIssues.length === 0 && (
          <div className="p-8 text-center text-gray-500">
            <Archive className="w-12 h-12 mx-auto mb-4 text-gray-300" />
            <p>No issues found matching your filters.</p>
          </div>
        )}
      </div>

      {/* Create Issue Modal */}
      {showCreateModal && (
        <CreateIssueModal
          isOpen={showCreateModal}
          projectId={Number(projectId)}
          onClose={() => setShowCreateModal(false)}
          onSuccess={() => {
            setShowCreateModal(false);
            queryClient.invalidateQueries({ queryKey: ['backlog', projectId] });
          }}
        />
      )}
    </div>
  );
}
