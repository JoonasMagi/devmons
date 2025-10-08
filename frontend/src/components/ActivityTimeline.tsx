import { useState, useEffect, useRef } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { MessageSquare, Edit2, Trash2 } from 'lucide-react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import toast from 'react-hot-toast';
import { commentService } from '../services/commentService';
import { CommentInput } from './CommentInput';
import { mentionComponents } from '../utils/mentions';
import { websocketService, type WebSocketMessage } from '../services/websocketService';
import type { Comment } from '../types/comment';
import type { IssueHistory } from '../types/issue';

interface ActivityTimelineProps {
  projectId: number;
  issueId: number;
  history: IssueHistory[];
}

type TimelineItem =
  | { type: 'comment'; data: Comment }
  | { type: 'history'; data: IssueHistory };

type FilterType = 'all' | 'comments';

export function ActivityTimeline({ projectId, issueId, history }: ActivityTimelineProps) {
  const queryClient = useQueryClient();
  const timelineEndRef = useRef<HTMLDivElement>(null);
  const [filter, setFilter] = useState<FilterType>('all');
  const [editingCommentId, setEditingCommentId] = useState<number | null>(null);
  const [editContent, setEditContent] = useState('');
  const [isPreview, setIsPreview] = useState(false);
  const [newComment, setNewComment] = useState('');
  const [isNewCommentPreview, setIsNewCommentPreview] = useState(false);

  // Fetch comments
  const { data: comments = [] } = useQuery({
    queryKey: ['comments', issueId],
    queryFn: () => commentService.getComments(issueId),
  });

  // Create comment mutation
  const createCommentMutation = useMutation({
    mutationFn: (content: string) =>
      commentService.createComment(issueId, { content }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['comments', issueId] });
      setNewComment('');
      setIsNewCommentPreview(false);
      toast.success('Comment added');
    },
    onError: () => {
      toast.error('Failed to add comment');
    },
  });

  // Update comment mutation
  const updateCommentMutation = useMutation({
    mutationFn: ({ id, content }: { id: number; content: string }) =>
      commentService.updateComment(id, { content }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['comments', issueId] });
      setEditingCommentId(null);
      setEditContent('');
      toast.success('Comment updated');
    },
    onError: () => {
      toast.error('Failed to update comment');
    },
  });

  // Delete comment mutation
  const deleteCommentMutation = useMutation({
    mutationFn: (id: number) => commentService.deleteComment(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['comments', issueId] });
      toast.success('Comment deleted');
    },
    onError: () => {
      toast.error('Failed to delete comment');
    },
  });

  // Combine and sort timeline items
  const timelineItems: TimelineItem[] = [
    ...comments.map((comment): TimelineItem => ({ type: 'comment', data: comment })),
    ...history.map((entry): TimelineItem => ({ type: 'history', data: entry })),
  ].sort((a, b) => {
    const aTime = a.type === 'comment' ? a.data.createdAt : a.data.changedAt;
    const bTime = b.type === 'comment' ? b.data.createdAt : b.data.changedAt;
    return new Date(aTime).getTime() - new Date(bTime).getTime();
  });

  // Filter timeline items
  const filteredItems = filter === 'comments'
    ? timelineItems.filter(item => item.type === 'comment')
    : timelineItems;

  // Auto-scroll to latest activity
  useEffect(() => {
    if (filteredItems.length > 0) {
      timelineEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }
  }, [filteredItems.length]);

  // WebSocket subscription for real-time updates
  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) return;

    // Connect to WebSocket if not already connected
    if (!websocketService.isConnected()) {
      websocketService.connect(token).catch(error => {
        console.error('Failed to connect to WebSocket:', error);
      });
    }

    // Subscribe to issue updates
    const unsubscribe = websocketService.subscribeToIssue(issueId, (message: WebSocketMessage) => {
      console.log('Received WebSocket message:', message);

      switch (message.type) {
        case 'COMMENT_ADDED':
        case 'COMMENT_UPDATED':
          // Invalidate comments query to refetch
          queryClient.invalidateQueries({ queryKey: ['comments', issueId] });
          break;

        case 'COMMENT_DELETED':
          // Invalidate comments query to refetch
          queryClient.invalidateQueries({ queryKey: ['comments', issueId] });
          break;

        case 'ISSUE_UPDATED':
          // Invalidate issue query to refetch
          queryClient.invalidateQueries({ queryKey: ['issue', issueId] });
          break;
      }
    });

    // Cleanup on unmount
    return () => {
      unsubscribe();
    };
  }, [issueId, queryClient]);

  const handleEdit = (comment: Comment) => {
    setEditingCommentId(comment.id);
    setEditContent(comment.content);
    setIsPreview(false);
  };

  const handleCancelEdit = () => {
    setEditingCommentId(null);
    setEditContent('');
    setIsPreview(false);
  };

  const handleSaveEdit = () => {
    if (!editContent.trim() || !editingCommentId) return;
    updateCommentMutation.mutate({ id: editingCommentId, content: editContent });
  };

  const handleDelete = (id: number) => {
    if (confirm('Are you sure you want to delete this comment?')) {
      deleteCommentMutation.mutate(id);
    }
  };

  const handleSubmitComment = () => {
    if (!newComment.trim()) return;
    createCommentMutation.mutate(newComment);
  };

  const formatRelativeTime = (dateString: string) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMins / 60);
    const diffDays = Math.floor(diffHours / 24);

    if (diffMins < 1) return 'just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    if (diffHours < 24) return `${diffHours}h ago`;
    if (diffDays < 7) return `${diffDays}d ago`;
    return date.toLocaleDateString();
  };

  return (
    <div>
      {/* Header with filter */}
      <div className="flex items-center justify-between mb-3">
        <h3 className="text-sm font-medium text-gray-700">Activity</h3>
        <div className="flex gap-2">
          <button
            onClick={() => setFilter('all')}
            className={`px-3 py-1 text-xs rounded-md transition-colors ${
              filter === 'all'
                ? 'bg-primary-100 text-primary-700'
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            }`}
          >
            All
          </button>
          <button
            onClick={() => setFilter('comments')}
            className={`px-3 py-1 text-xs rounded-md transition-colors ${
              filter === 'comments'
                ? 'bg-primary-100 text-primary-700'
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            }`}
          >
            Comments only
          </button>
        </div>
      </div>

      {/* Add new comment */}
      <div className="mb-4 bg-white border border-gray-200 rounded-lg p-3">
        <CommentInput
          projectId={projectId}
          value={newComment}
          onChange={setNewComment}
          onSubmit={handleSubmitComment}
          placeholder="Add a comment (Markdown supported, @ to mention)"
          submitLabel={createCommentMutation.isPending ? 'Adding...' : 'Add Comment'}
          isPreview={isNewCommentPreview}
          onTogglePreview={() => setIsNewCommentPreview(!isNewCommentPreview)}
        />
      </div>

      {/* Timeline */}
      <div className="space-y-4">
        {filteredItems.map((item, index) => {
          if (item.type === 'comment') {
            const comment = item.data;
            const isEditing = editingCommentId === comment.id;

            return (
              <div key={`comment-${comment.id}`} className="flex gap-3">
                <div className="w-8 h-8 bg-gradient-to-br from-blue-500 to-blue-600 rounded-full flex items-center justify-center flex-shrink-0">
                  <MessageSquare className="w-4 h-4 text-white" />
                </div>
                <div className="flex-1 bg-white border border-gray-200 rounded-lg p-3">
                  <div className="flex items-start justify-between mb-2">
                    <div>
                      <span className="text-sm font-medium text-gray-900">
                        {comment.author.fullName}
                      </span>
                      <span className="text-xs text-gray-500 ml-2">
                        {formatRelativeTime(comment.createdAt)}
                        {comment.isEdited && ' (edited)'}
                      </span>
                    </div>
                    <div className="flex gap-1">
                      <button
                        onClick={() => handleEdit(comment)}
                        className="p-1 text-gray-400 hover:text-gray-600 transition-colors"
                        title="Edit comment"
                      >
                        <Edit2 className="w-4 h-4" />
                      </button>
                      <button
                        onClick={() => handleDelete(comment.id)}
                        className="p-1 text-gray-400 hover:text-red-600 transition-colors"
                        title="Delete comment"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                  </div>

                  {isEditing ? (
                    <div className="space-y-2">
                      <div className="flex gap-2 mb-2">
                        <button
                          onClick={() => setIsPreview(false)}
                          className={`px-3 py-1 text-xs rounded-md transition-colors ${
                            !isPreview
                              ? 'bg-primary-100 text-primary-700'
                              : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                          }`}
                        >
                          Write
                        </button>
                        <button
                          onClick={() => setIsPreview(true)}
                          className={`px-3 py-1 text-xs rounded-md transition-colors ${
                            isPreview
                              ? 'bg-primary-100 text-primary-700'
                              : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                          }`}
                        >
                          Preview
                        </button>
                      </div>

                      {isPreview ? (
                        <div className="prose prose-sm max-w-none p-3 bg-gray-50 rounded-md min-h-[100px]">
                          <ReactMarkdown remarkPlugins={[remarkGfm]}>
                            {editContent || '*Nothing to preview*'}
                          </ReactMarkdown>
                        </div>
                      ) : (
                        <textarea
                          value={editContent}
                          onChange={(e) => setEditContent(e.target.value)}
                          className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-primary-500 focus:border-transparent resize-none"
                          rows={4}
                          placeholder="Edit your comment (Markdown supported)"
                        />
                      )}

                      <div className="flex gap-2">
                        <button
                          onClick={handleSaveEdit}
                          disabled={!editContent.trim() || updateCommentMutation.isPending}
                          className="px-3 py-1.5 bg-primary-600 text-white text-sm rounded-md hover:bg-primary-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                        >
                          {updateCommentMutation.isPending ? 'Saving...' : 'Save'}
                        </button>
                        <button
                          onClick={handleCancelEdit}
                          className="px-3 py-1.5 bg-gray-100 text-gray-700 text-sm rounded-md hover:bg-gray-200 transition-colors"
                        >
                          Cancel
                        </button>
                      </div>
                    </div>
                  ) : (
                    <div className="prose prose-sm max-w-none">
                      <ReactMarkdown
                        remarkPlugins={[remarkGfm]}
                        components={mentionComponents}
                      >
                        {comment.content}
                      </ReactMarkdown>
                    </div>
                  )}
                </div>
              </div>
            );
          } else {
            const entry = item.data;
            return (
              <div key={`history-${entry.id}`} className="flex gap-3">
                <div className="w-8 h-8 bg-gradient-to-br from-primary-500 to-primary-600 rounded-full flex items-center justify-center flex-shrink-0">
                  <span className="text-xs text-white font-medium">
                    {entry.changedBy?.fullName?.charAt(0).toUpperCase() || '?'}
                  </span>
                </div>
                <div className="flex-1">
                  <p className="text-sm text-gray-900">
                    <span className="font-medium">{entry.changedBy?.fullName || 'Unknown'}</span>
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
                  <p className="text-xs text-gray-500 mt-0.5">
                    {formatRelativeTime(entry.changedAt)}
                  </p>
                </div>
              </div>
            );
          }
        })}

        {filteredItems.length === 0 && (
          <p className="text-sm text-gray-400 italic">No activity yet</p>
        )}

        {/* Auto-scroll anchor */}
        <div ref={timelineEndRef} />
      </div>
    </div>
  );
}
