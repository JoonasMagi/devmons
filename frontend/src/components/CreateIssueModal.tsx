import { Fragment, useState } from 'react';
import { Dialog, Transition } from '@headlessui/react';
import { X, Plus } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { issueService } from '../services/issueService';
import { projectService } from '../services/projectService';
import type { CreateIssueRequest } from '../types/issue';

interface CreateIssueModalProps {
  isOpen: boolean;
  onClose: () => void;
  projectId: number;
  workflowStateId?: number;
  onSuccess?: () => void;
}

export function CreateIssueModal({ isOpen, onClose, projectId, onSuccess }: CreateIssueModalProps) {
  const queryClient = useQueryClient();
  const [showPreview, setShowPreview] = useState(false);

  // Fetch issue types
  const { data: issueTypes = [] } = useQuery({
    queryKey: ['project-issue-types', projectId],
    queryFn: () => projectService.getIssueTypes(projectId),
    enabled: !!projectId && isOpen,
  });

  // Fetch project members
  const { data: members = [] } = useQuery({
    queryKey: ['project-members', projectId],
    queryFn: () => projectService.getProjectMembers(projectId),
    enabled: !!projectId && isOpen,
  });

  // Fetch project labels
  const { data: labels = [] } = useQuery({
    queryKey: ['project-labels', projectId],
    queryFn: () => projectService.getProjectLabels(projectId),
    enabled: !!projectId && isOpen,
  });

  const { register, handleSubmit, formState: { errors }, reset, watch } = useForm<CreateIssueRequest>({
    defaultValues: {
      priority: 'MEDIUM',
      labelIds: [],
    },
  });

  const description = watch('description');

  const createMutation = useMutation({
    mutationFn: (data: CreateIssueRequest) => issueService.createIssue(projectId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['project-issues', projectId] });
      queryClient.invalidateQueries({ queryKey: ['board-issues', projectId] });
      queryClient.invalidateQueries({ queryKey: ['backlog', projectId] });
      toast.success('Issue created successfully');
      reset();
      onClose();
      onSuccess?.();
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to create issue');
    },
  });

  const onSubmit = (data: CreateIssueRequest) => {
    // Convert labelIds from string[] to number[]
    const formattedData = {
      ...data,
      labelIds: data.labelIds ? data.labelIds.map(Number) : [],
      assigneeId: data.assigneeId ? Number(data.assigneeId) : undefined,
      issueTypeId: Number(data.issueTypeId),
      storyPoints: data.storyPoints ? Number(data.storyPoints) : undefined,
    };
    createMutation.mutate(formattedData);
  };

  const handleClose = () => {
    reset();
    setShowPreview(false);
    onClose();
  };

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
              <Dialog.Panel className="w-full max-w-2xl transform overflow-hidden rounded-2xl bg-white p-6 text-left align-middle shadow-xl transition-all">
                {/* Header */}
                <div className="flex items-center justify-between mb-6">
                  <Dialog.Title as="h3" className="text-lg font-semibold text-gray-900 flex items-center gap-2">
                    <Plus className="w-5 h-5 text-primary-600" />
                    Create Issue
                  </Dialog.Title>
                  <button
                    onClick={handleClose}
                    className="text-gray-400 hover:text-gray-500 transition-colors"
                  >
                    <X className="w-5 h-5" />
                  </button>
                </div>

                {/* Form */}
                <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                  {/* Title */}
                  <div>
                    <label htmlFor="title" className="block text-sm font-medium text-gray-700 mb-1">
                      Title <span className="text-red-500">*</span>
                    </label>
                    <input
                      {...register('title', { required: 'Title is required' })}
                      type="text"
                      id="title"
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                      placeholder="Enter issue title"
                    />
                    {errors.title && (
                      <p className="mt-1 text-sm text-red-600">{errors.title.message}</p>
                    )}
                  </div>

                  {/* Issue Type and Priority */}
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label htmlFor="issueTypeId" className="block text-sm font-medium text-gray-700 mb-1">
                        Type <span className="text-red-500">*</span>
                      </label>
                      <select
                        {...register('issueTypeId', { required: 'Issue type is required' })}
                        id="issueTypeId"
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                      >
                        <option value="">Select type</option>
                        {issueTypes.map((type) => (
                          <option key={type.id} value={type.id}>
                            {type.icon} {type.name}
                          </option>
                        ))}
                      </select>
                      {errors.issueTypeId && (
                        <p className="mt-1 text-sm text-red-600">{errors.issueTypeId.message}</p>
                      )}
                    </div>

                    <div>
                      <label htmlFor="priority" className="block text-sm font-medium text-gray-700 mb-1">
                        Priority
                      </label>
                      <select
                        {...register('priority')}
                        id="priority"
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                      >
                        <option value="LOW">Low</option>
                        <option value="MEDIUM">Medium</option>
                        <option value="HIGH">High</option>
                        <option value="CRITICAL">Critical</option>
                      </select>
                    </div>
                  </div>

                  {/* Assignee and Story Points */}
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label htmlFor="assigneeId" className="block text-sm font-medium text-gray-700 mb-1">
                        Assignee
                      </label>
                      <select
                        {...register('assigneeId')}
                        id="assigneeId"
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                      >
                        <option value="">Unassigned</option>
                        {members.map((member) => (
                          <option key={member.userId} value={member.userId}>
                            {member.fullName} (@{member.username})
                          </option>
                        ))}
                      </select>
                    </div>

                    <div>
                      <label htmlFor="storyPoints" className="block text-sm font-medium text-gray-700 mb-1">
                        Story Points
                      </label>
                      <input
                        {...register('storyPoints')}
                        type="number"
                        id="storyPoints"
                        min="1"
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                        placeholder="e.g., 5"
                      />
                    </div>
                  </div>

                  {/* Due Date and Labels */}
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label htmlFor="dueDate" className="block text-sm font-medium text-gray-700 mb-1">
                        Due Date
                      </label>
                      <input
                        {...register('dueDate')}
                        type="date"
                        id="dueDate"
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                      />
                    </div>

                    <div>
                      <label htmlFor="labelIds" className="block text-sm font-medium text-gray-700 mb-1">
                        Labels
                      </label>
                      <select
                        {...register('labelIds')}
                        id="labelIds"
                        multiple
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                        size={3}
                      >
                        {labels.map((label) => (
                          <option key={label.id} value={label.id}>
                            {label.name}
                          </option>
                        ))}
                      </select>
                      <p className="mt-1 text-xs text-gray-500">Hold Ctrl/Cmd to select multiple</p>
                    </div>
                  </div>

                  {/* Description */}
                  <div>
                    <div className="flex items-center justify-between mb-1">
                      <label htmlFor="description" className="block text-sm font-medium text-gray-700">
                        Description
                      </label>
                      <button
                        type="button"
                        onClick={() => setShowPreview(!showPreview)}
                        className="text-xs text-primary-600 hover:text-primary-700"
                      >
                        {showPreview ? 'Edit' : 'Preview'}
                      </button>
                    </div>
                    {showPreview ? (
                      <div className="w-full min-h-[120px] px-3 py-2 border border-gray-300 rounded-lg bg-gray-50 prose prose-sm max-w-none">
                        {description || <span className="text-gray-400">No description</span>}
                      </div>
                    ) : (
                      <textarea
                        {...register('description')}
                        id="description"
                        rows={5}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent font-mono text-sm"
                        placeholder="Describe the issue (Markdown supported)"
                      />
                    )}
                  </div>

                  {/* Actions */}
                  <div className="flex gap-3 pt-4">
                    <button
                      type="button"
                      onClick={handleClose}
                      className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
                    >
                      Cancel
                    </button>
                    <button
                      type="submit"
                      disabled={createMutation.isPending}
                      className="flex-1 px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      {createMutation.isPending ? 'Creating...' : 'Create Issue'}
                    </button>
                  </div>
                </form>
              </Dialog.Panel>
            </Transition.Child>
          </div>
        </div>
      </Dialog>
    </Transition>
  );
}

