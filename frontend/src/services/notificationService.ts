import { api } from '../lib/api';

export interface Notification {
  id: number;
  type: 'MENTION' | 'ASSIGNMENT' | 'STATUS_CHANGE' | 'COMMENT_ADDED' | 'ISSUE_UPDATED';
  message: string;
  link: string;
  relatedEntityId: number;
  relatedEntityType: string;
  isRead: boolean;
  createdAt: string;
  readAt?: string;
}

export const notificationService = {
  async getNotifications(): Promise<Notification[]> {
    const response = await api.get<Notification[]>('/notifications');
    return response.data;
  },

  async getUnreadNotifications(): Promise<Notification[]> {
    const response = await api.get<Notification[]>('/notifications/unread');
    return response.data;
  },

  async getUnreadCount(): Promise<number> {
    const response = await api.get<number>('/notifications/unread/count');
    return response.data;
  },

  async markAsRead(id: number): Promise<void> {
    await api.put(`/notifications/${id}/read`);
  },

  async markAllAsRead(): Promise<void> {
    await api.put('/notifications/read-all');
  },
};

