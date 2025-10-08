import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { MessageSquare, Send, Edit2, Trash2, X } from 'lucide-react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import toast from 'react-hot-toast';
import { commentService } from '../services/commentService';
import type { Comment } from '../types/comment';

interface CommentSectionProps {
  issueId: number;
}

export function CommentSection({ issueId }: CommentSectionProps) {
  const queryClient = useQueryClient();
  const [newComment, setNewComment] = useState('');
  const [editingCommentId, setEditingCommentId] = useState<number | null>(null);
  const [editedContent, setEditedContent] = useState('');
  const [isPreview, setIsPreview] = useState(false);

  // Fetch comments
  const { data: comments = [], isLoading } = useQuery({
    queryKey: ['comments', issueId],
    queryFn: () => commentService.getComments(issueId),
    enabled: !!issueId,
  });

  // Create comment mutation
  const createCommentMutation = useMutation({
    mutationFn: (content: string) => commentService.createComment(issueId, { content }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['comments', issueId] });
      setNewComment('');
      setIsPreview(false);
      toast.success('Comment added');
    },
    onError: () => {
      toast.error('Failed to add comment');
    },
  });

  // Update comment mutation
  const updateCommentMutation = useMutation({
    mutationFn: ({ commentId, content }: { commentId: number; content: string }) =>
      commentService.updateComment(commentId, { content }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['comments', issueId] });
      setEditingCommentId(null);
      setEditedContent('');
      toast.success('Comment updated');
    },
    onError: () => {
      toast.error('Failed to update comment');
    },
  });

  // Delete comment mutation
  const deleteCommentMutation = useMutation({
    mutationFn: (commentId: number) => commentService.deleteComment(commentId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['comments', issueId] });
      toast.success('Comment deleted');
    },
    onError: () => {
      toast.error('Failed to delete comment');
    },
  });

  const handleSubmitComment = () => {
    if (!newComment.trim()) {
      toast.error('Comment cannot be empty');
      return;
    }
    createCommentMutation.mutate(newComment);
  };

  const handleStartEdit = (comment: Comment) => {
    setEditingCommentId(comment.id);
    setEditedContent(comment.content);
  };

  const handleCancelEdit = () => {
    setEditingCommentId(null);
    setEditedContent('');
  };

  const handleSaveEdit = (commentId: number) => {
    if (!editedContent.trim()) {
      toast.error('Comment cannot be empty');
      return;
    }
    updateCommentMutation.mutate({ commentId, content: editedContent });
  };

  const handleDelete = (commentId: number) => {
    if (window.confirm('Are you sure you want to delete this comment?')) {
      deleteCommentMutation.mutate(commentId);
    }
  };

  const formatTimestamp = (timestamp: string) => {
    const date = new Date(timestamp);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return 'just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    if (diffHours < 24) return `${diffHours}h ago`;
    if (diffDays < 7) return `${diffDays}d ago`;
    return date.toLocaleDateString();
  };

  return (
    <div>
      <h3 className="text-sm font-medium text-gray-700 mb-3 flex items-center gap-2">
        <MessageSquare className="w-4 h-4" />
        Comments ({comments.length})
      </h3>

      {/* Comment List */}
      <div className="space-y-4 mb-4">
        {isLoading ? (
          <p className="text-sm text-gray-400 italic">Loading comments...</p>
        ) : comments.length === 0 ? (
          <p className="text-sm text-gray-400 italic">No comments yet. Be the first to comment!</p>
        ) : (
          comments.map((comment) => (
            <div key={comment.id} className="flex gap-3">
              {/* Avatar */}
              <div className="w-8 h-8 bg-gradient-to-br from-primary-500 to-primary-600 rounded-full flex items-center justify-center flex-shrink-0">
                <span className="text-xs text-white font-medium">
                  {comment.author.fullName.charAt(0).toUpperCase()}
                </span>
              </div>

              {/* Comment Content */}
              <div className="flex-1">
                <div className="bg-gray-50 rounded-lg p-3 border border-gray-200">
                  <div className="flex items-start justify-between mb-2">
                    <div>
                      <span className="text-sm font-medium text-gray-900">
                        {comment.author.fullName}
                      </span>
                      <span className="text-xs text-gray-500 ml-2">
                        {formatTimestamp(comment.createdAt)}
                        {comment.isEdited && ' (edited)'}
                      </span>
                    </div>
                    <div className="flex gap-1">
                      <button
                        onClick={() => handleStartEdit(comment)}
                        className="p-1 text-gray-400 hover:text-gray-600 transition"
                        title="Edit comment"
                      >
                        <Edit2 className="w-3 h-3" />
                      </button>
                      <button
                        onClick={() => handleDelete(comment.id)}
                        className="p-1 text-gray-400 hover:text-red-600 transition"
                        title="Delete comment"
                      >
                        <Trash2 className="w-3 h-3" />
                      </button>
                    </div>
                  </div>

                  {editingCommentId === comment.id ? (
                    <div>
                      <textarea
                        value={editedContent}
                        onChange={(e) => setEditedContent(e.target.value)}
                        className="w-full h-24 p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent text-sm"
                        placeholder="Edit your comment..."
                      />
                      <div className="flex gap-2 mt-2">
                        <button
                          onClick={() => handleSaveEdit(comment.id)}
                          disabled={updateCommentMutation.isPending}
                          className="px-3 py-1 bg-primary-600 text-white text-sm rounded hover:bg-primary-700 transition disabled:opacity-50"
                        >
                          Save
                        </button>
                        <button
                          onClick={handleCancelEdit}
                          className="px-3 py-1 text-gray-700 text-sm hover:bg-gray-100 rounded transition"
                        >
                          Cancel
                        </button>
                      </div>
                    </div>
                  ) : (
                    <div className="prose prose-sm max-w-none">
                      <ReactMarkdown remarkPlugins={[remarkGfm]}>
                        {comment.content}
                      </ReactMarkdown>
                    </div>
                  )}
                </div>
              </div>
            </div>
          ))
        )}
      </div>

      {/* New Comment Form */}
      <div className="border-t border-gray-200 pt-4">
        <div className="flex gap-2 mb-2">
          <button
            onClick={() => setIsPreview(false)}
            className={`px-3 py-1 text-sm rounded transition ${
              !isPreview
                ? 'bg-primary-100 text-primary-700'
                : 'text-gray-600 hover:bg-gray-100'
            }`}
          >
            Write
          </button>
          <button
            onClick={() => setIsPreview(true)}
            className={`px-3 py-1 text-sm rounded transition ${
              isPreview
                ? 'bg-primary-100 text-primary-700'
                : 'text-gray-600 hover:bg-gray-100'
            }`}
          >
            Preview
          </button>
        </div>

        {isPreview ? (
          <div className="min-h-[100px] p-3 border border-gray-200 rounded-lg bg-gray-50 prose prose-sm max-w-none">
            {newComment ? (
              <ReactMarkdown remarkPlugins={[remarkGfm]}>{newComment}</ReactMarkdown>
            ) : (
              <p className="text-gray-400 italic">Nothing to preview</p>
            )}
          </div>
        ) : (
          <textarea
            value={newComment}
            onChange={(e) => setNewComment(e.target.value)}
            className="w-full h-24 p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent text-sm"
            placeholder="Add a comment... (Markdown supported)"
            onKeyDown={(e) => {
              if (e.key === 'Enter' && (e.ctrlKey || e.metaKey)) {
                handleSubmitComment();
              }
            }}
          />
        )}

        <div className="flex justify-between items-center mt-2">
          <p className="text-xs text-gray-500">
            Tip: Use Markdown for formatting. Press Ctrl+Enter to submit.
          </p>
          <button
            onClick={handleSubmitComment}
            disabled={createCommentMutation.isPending || !newComment.trim()}
            className="flex items-center gap-2 px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <Send className="w-4 h-4" />
            Comment
          </button>
        </div>
      </div>
    </div>
  );
}

