import { api } from '../lib/api';

export interface ProjectMember {
  id: number;
  userId: number;
  username: string;
  email: string;
  fullName: string;
  role: 'OWNER' | 'MEMBER' | 'VIEWER';
  joinedAt: string;
}

export interface ProjectInvitation {
  id: number;
  projectId: number;
  projectName: string;
  email: string;
  role: 'OWNER' | 'MEMBER' | 'VIEWER';
  status: 'PENDING' | 'ACCEPTED' | 'DECLINED' | 'CANCELLED' | 'EXPIRED';
  invitedByUsername: string;
  createdAt: string;
  expiresAt: string;
  expired: boolean;
}

export interface InviteMemberRequest {
  email: string;
  role: 'OWNER' | 'MEMBER' | 'VIEWER';
}

export interface UpdateMemberRoleRequest {
  role: 'OWNER' | 'MEMBER' | 'VIEWER';
}

export const teamService = {
  /**
   * Invite a member to the project
   */
  inviteMember: async (projectId: number, request: InviteMemberRequest): Promise<ProjectInvitation> => {
    const response = await api.post(`/projects/${projectId}/members/invite`, request);
    return response.data;
  },

  /**
   * Get pending invitations for a project
   */
  getPendingInvitations: async (projectId: number): Promise<ProjectInvitation[]> => {
    const response = await api.get(`/projects/${projectId}/invitations`);
    return response.data;
  },

  /**
   * Cancel a pending invitation
   */
  cancelInvitation: async (invitationId: number): Promise<void> => {
    await api.delete(`/invitations/${invitationId}`);
  },

  /**
   * Accept an invitation
   */
  acceptInvitation: async (token: string): Promise<ProjectMember> => {
    const response = await api.post(`/invitations/accept?token=${token}`);
    return response.data;
  },

  /**
   * Get all members of a project
   */
  getProjectMembers: async (projectId: number): Promise<ProjectMember[]> => {
    const response = await api.get(`/projects/${projectId}/members`);
    return response.data;
  },

  /**
   * Update a member's role
   */
  updateMemberRole: async (
    projectId: number,
    memberId: number,
    request: UpdateMemberRoleRequest
  ): Promise<ProjectMember> => {
    const response = await api.put(`/projects/${projectId}/members/${memberId}/role`, request);
    return response.data;
  },

  /**
   * Remove a member from the project
   */
  removeMember: async (projectId: number, memberId: number): Promise<void> => {
    await api.delete(`/projects/${projectId}/members/${memberId}`);
  },
};

