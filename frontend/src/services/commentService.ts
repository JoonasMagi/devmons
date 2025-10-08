import { api } from '../lib/api';
import type { Comment, CreateCommentRequest, UpdateCommentRequest } from '../types/comment';

export const commentService = {
  // Get all comments for an issue
  getComments: async (issueId: number): Promise<Comment[]> => {
    const response = await api.get(`/issues/${issueId}/comments`);
    return response.data;
  },

  // Create a new comment
  createComment: async (issueId: number, data: CreateCommentRequest): Promise<Comment> => {
    const response = await api.post(`/issues/${issueId}/comments`, data);
    return response.data;
  },

  // Update a comment
  updateComment: async (commentId: number, data: UpdateCommentRequest): Promise<Comment> => {
    const response = await api.put(`/comments/${commentId}`, data);
    return response.data;
  },

  // Delete a comment
  deleteComment: async (commentId: number): Promise<void> => {
    await api.delete(`/comments/${commentId}`);
  },
};

