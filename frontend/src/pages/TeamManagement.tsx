import { useState } from 'react';
import { useParams } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Users, UserPlus, Mail, Trash2 } from 'lucide-react';
import toast from 'react-hot-toast';
import { teamService, type ProjectMember, type ProjectInvitation } from '../services/teamService';
import { InviteMemberModal } from '../components/InviteMemberModal';

/**
 * Team Management page for managing project members and invitations.
 */
export function TeamManagement() {
  const { projectId } = useParams<{ projectId: string }>();
  const queryClient = useQueryClient();
  const [isInviteModalOpen, setIsInviteModalOpen] = useState(false);

  // Fetch project members
  const { data: members = [], isLoading: membersLoading } = useQuery({
    queryKey: ['project-members', projectId],
    queryFn: () => teamService.getProjectMembers(Number(projectId)),
    enabled: !!projectId,
  });

  // Fetch pending invitations
  const { data: invitations = [], isLoading: invitationsLoading } = useQuery({
    queryKey: ['project-invitations', projectId],
    queryFn: () => teamService.getPendingInvitations(Number(projectId)),
    enabled: !!projectId,
  });

  // Update member role mutation
  const updateRoleMutation = useMutation({
    mutationFn: ({ memberId, role }: { memberId: number; role: 'OWNER' | 'MEMBER' | 'VIEWER' }) =>
      teamService.updateMemberRole(Number(projectId), memberId, { role }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['project-members', projectId] });
      toast.success('Member role updated');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to update role');
    },
  });

  // Remove member mutation
  const removeMemberMutation = useMutation({
    mutationFn: (memberId: number) => teamService.removeMember(Number(projectId), memberId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['project-members', projectId] });
      toast.success('Member removed from project');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to remove member');
    },
  });

  // Cancel invitation mutation
  const cancelInvitationMutation = useMutation({
    mutationFn: (invitationId: number) => teamService.cancelInvitation(invitationId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['project-invitations', projectId] });
      toast.success('Invitation cancelled');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to cancel invitation');
    },
  });

  const handleRoleChange = (memberId: number, newRole: string) => {
    if (window.confirm('Are you sure you want to change this member\'s role?')) {
      updateRoleMutation.mutate({ memberId, role: newRole as 'OWNER' | 'MEMBER' | 'VIEWER' });
    }
  };

  const handleRemoveMember = (member: ProjectMember) => {
    if (window.confirm(`Are you sure you want to remove ${member.fullName} from the project?`)) {
      removeMemberMutation.mutate(member.id);
    }
  };

  const handleCancelInvitation = (invitation: ProjectInvitation) => {
    if (window.confirm(`Cancel invitation for ${invitation.email}?`)) {
      cancelInvitationMutation.mutate(invitation.id);
    }
  };

  const getRoleBadgeColor = (role: string) => {
    switch (role) {
      case 'OWNER':
        return 'bg-purple-100 text-purple-800';
      case 'MEMBER':
        return 'bg-blue-100 text-blue-800';
      case 'VIEWER':
        return 'bg-gray-100 text-gray-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const getInitials = (fullName: string) => {
    return fullName
      .split(' ')
      .map(n => n[0])
      .join('')
      .toUpperCase()
      .slice(0, 2);
  };

  if (membersLoading || invitationsLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto p-6">
      {/* Header */}
      <div className="mb-8">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-900 flex items-center gap-3">
              <Users className="w-8 h-8 text-primary-600" />
              Team Management
            </h1>
            <p className="mt-2 text-gray-600">
              Manage project members and invitations
            </p>
          </div>
          <button
            onClick={() => setIsInviteModalOpen(true)}
            className="flex items-center gap-2 px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors"
          >
            <UserPlus className="w-5 h-5" />
            Invite Member
          </button>
        </div>
      </div>

      {/* Team Members */}
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 mb-8">
        <div className="px-6 py-4 border-b border-gray-200">
          <h2 className="text-lg font-semibold text-gray-900">
            Team Members ({members.length})
          </h2>
        </div>
        <div className="divide-y divide-gray-200">
          {members.map((member) => (
            <div key={member.id} className="px-6 py-4 flex items-center justify-between hover:bg-gray-50">
              <div className="flex items-center gap-4">
                {/* Avatar */}
                <div className="w-12 h-12 rounded-full bg-gradient-to-br from-primary-400 to-primary-600 flex items-center justify-center text-white font-semibold">
                  {getInitials(member.fullName)}
                </div>
                {/* Info */}
                <div>
                  <div className="font-medium text-gray-900">{member.fullName}</div>
                  <div className="text-sm text-gray-500">@{member.username} • {member.email}</div>
                  <div className="text-xs text-gray-400 mt-1">
                    Joined {new Date(member.joinedAt).toLocaleDateString()}
                  </div>
                </div>
              </div>
              {/* Actions */}
              <div className="flex items-center gap-3">
                {/* Role Selector */}
                <select
                  value={member.role}
                  onChange={(e) => handleRoleChange(member.id, e.target.value)}
                  className={`px-3 py-1.5 rounded-full text-sm font-medium border-0 ${getRoleBadgeColor(member.role)} cursor-pointer`}
                >
                  <option value="OWNER">Owner</option>
                  <option value="MEMBER">Member</option>
                  <option value="VIEWER">Viewer</option>
                </select>
                {/* Remove Button */}
                <button
                  onClick={() => handleRemoveMember(member)}
                  className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                  title="Remove member"
                >
                  <Trash2 className="w-5 h-5" />
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Pending Invitations */}
      {invitations.length > 0 && (
        <div className="bg-white rounded-lg shadow-sm border border-gray-200">
          <div className="px-6 py-4 border-b border-gray-200">
            <h2 className="text-lg font-semibold text-gray-900 flex items-center gap-2">
              <Mail className="w-5 h-5 text-gray-400" />
              Pending Invitations ({invitations.length})
            </h2>
          </div>
          <div className="divide-y divide-gray-200">
            {invitations.map((invitation) => (
              <div key={invitation.id} className="px-6 py-4 flex items-center justify-between hover:bg-gray-50">
                <div className="flex items-center gap-4">
                  <div className="w-12 h-12 rounded-full bg-gray-200 flex items-center justify-center">
                    <Mail className="w-6 h-6 text-gray-400" />
                  </div>
                  <div>
                    <div className="font-medium text-gray-900">{invitation.email}</div>
                    <div className="text-sm text-gray-500">
                      Invited by @{invitation.invitedByUsername}
                    </div>
                    <div className="text-xs text-gray-400 mt-1">
                      {invitation.expired ? (
                        <span className="text-red-600">Expired</span>
                      ) : (
                        `Expires ${new Date(invitation.expiresAt).toLocaleDateString()}`
                      )}
                    </div>
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  <span className={`px-3 py-1.5 rounded-full text-sm font-medium ${getRoleBadgeColor(invitation.role)}`}>
                    {invitation.role}
                  </span>
                  <button
                    onClick={() => handleCancelInvitation(invitation)}
                    className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                    title="Cancel invitation"
                  >
                    <Trash2 className="w-5 h-5" />
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Invite Member Modal */}
      <InviteMemberModal
        isOpen={isInviteModalOpen}
        onClose={() => setIsInviteModalOpen(false)}
        projectId={Number(projectId)}
      />
    </div>
  );
}

