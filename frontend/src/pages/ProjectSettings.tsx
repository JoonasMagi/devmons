import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { ArrowLeft, Settings, Tag, Archive, RotateCcw, Save, X } from 'lucide-react';
import { useForm } from 'react-hook-form';
import toast from 'react-hot-toast';
import { projectService } from '../services/projectService';
import type { UpdateProjectRequest } from '../types/project';
import { CreateLabelModal } from '../components/CreateLabelModal';

export function ProjectSettings() {
  const { projectId } = useParams();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [isCreateLabelModalOpen, setIsCreateLabelModalOpen] = useState(false);

  // Fetch project
  const { data: project, isLoading: projectLoading } = useQuery({
    queryKey: ['project', projectId],
    queryFn: () => projectService.getProject(Number(projectId)),
    enabled: !!projectId,
  });

  // Fetch labels
  const { data: labels = [], isLoading: labelsLoading } = useQuery({
    queryKey: ['project-labels', projectId],
    queryFn: () => projectService.getProjectLabels(Number(projectId)),
    enabled: !!projectId,
  });

  // Form for project details
  const { register, handleSubmit, formState: { errors, isDirty } } = useForm<UpdateProjectRequest>({
    values: {
      name: project?.name || '',
      description: project?.description || '',
    },
  });

  // Update project mutation
  const updateMutation = useMutation({
    mutationFn: (data: UpdateProjectRequest) => projectService.updateProject(Number(projectId), data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['project', projectId] });
      queryClient.invalidateQueries({ queryKey: ['projects'] });
      toast.success('Project updated successfully');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to update project');
    },
  });

  // Archive project mutation
  const archiveMutation = useMutation({
    mutationFn: () => projectService.archiveProject(Number(projectId)),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['projects'] });
      toast.success('Project archived');
      navigate('/dashboard');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to archive project');
    },
  });

  // Restore project mutation
  const restoreMutation = useMutation({
    mutationFn: () => projectService.restoreProject(Number(projectId)),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['project', projectId] });
      queryClient.invalidateQueries({ queryKey: ['projects'] });
      toast.success('Project restored');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to restore project');
    },
  });

  // Delete label mutation
  const deleteLabelMutation = useMutation({
    mutationFn: (labelId: number) => projectService.deleteLabel(Number(projectId), labelId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['project-labels', projectId] });
      toast.success('Label deleted');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to delete label');
    },
  });

  const onSubmit = (data: UpdateProjectRequest) => {
    updateMutation.mutate(data);
  };

  const handleArchive = () => {
    if (window.confirm('Are you sure you want to archive this project? It will be hidden from the main view.')) {
      archiveMutation.mutate();
    }
  };

  const handleRestore = () => {
    if (window.confirm('Are you sure you want to restore this project?')) {
      restoreMutation.mutate();
    }
  };

  const handleDeleteLabel = (labelId: number, labelName: string) => {
    if (window.confirm(`Are you sure you want to delete the label "${labelName}"?`)) {
      deleteLabelMutation.mutate(labelId);
    }
  };

  if (projectLoading || labelsLoading) {
    return (
      <div className="flex items-center justify-center h-screen">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Top Navigation */}
      <nav className="bg-white border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center gap-4">
              <button
                onClick={() => navigate(`/projects/${projectId}/board`)}
                className="p-2 text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-lg transition"
              >
                <ArrowLeft className="w-5 h-5" />
              </button>
              <h1 className="text-xl font-bold text-gray-900 flex items-center gap-2">
                <Settings className="w-6 h-6 text-primary-600" />
                Project Settings
              </h1>
            </div>
          </div>
        </div>
      </nav>

      {/* Main Content */}
      <main className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
        {/* Project Details */}
        <div className="bg-white rounded-lg shadow-sm border border-gray-200">
          <div className="px-6 py-4 border-b border-gray-200">
            <h2 className="text-lg font-semibold text-gray-900">Project Details</h2>
          </div>
          <form onSubmit={handleSubmit(onSubmit)} className="p-6 space-y-4">
            {/* Project Name */}
            <div>
              <label htmlFor="name" className="block text-sm font-medium text-gray-700 mb-1">
                Project Name
              </label>
              <input
                {...register('name', { required: 'Project name is required' })}
                type="text"
                id="name"
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
              />
              {errors.name && (
                <p className="mt-1 text-sm text-red-600">{errors.name.message}</p>
              )}
            </div>

            {/* Project Key (Read-only) */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Project Key
              </label>
              <input
                type="text"
                value={project?.key || ''}
                disabled
                className="w-full px-3 py-2 border border-gray-300 rounded-lg bg-gray-50 text-gray-500 cursor-not-allowed"
              />
              <p className="mt-1 text-xs text-gray-500">Project key cannot be changed</p>
            </div>

            {/* Description */}
            <div>
              <label htmlFor="description" className="block text-sm font-medium text-gray-700 mb-1">
                Description
              </label>
              <textarea
                {...register('description')}
                id="description"
                rows={4}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
              />
            </div>

            {/* Save Button */}
            <div className="flex justify-end">
              <button
                type="submit"
                disabled={!isDirty || updateMutation.isPending}
                className="flex items-center gap-2 px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <Save className="w-4 h-4" />
                {updateMutation.isPending ? 'Saving...' : 'Save Changes'}
              </button>
            </div>
          </form>
        </div>

        {/* Labels */}
        <div className="bg-white rounded-lg shadow-sm border border-gray-200">
          <div className="px-6 py-4 border-b border-gray-200 flex items-center justify-between">
            <h2 className="text-lg font-semibold text-gray-900 flex items-center gap-2">
              <Tag className="w-5 h-5 text-gray-400" />
              Labels
            </h2>
            <button
              onClick={() => setIsCreateLabelModalOpen(true)}
              className="px-3 py-1.5 bg-primary-600 text-white text-sm rounded-lg hover:bg-primary-700 transition-colors"
            >
              Create Label
            </button>
          </div>
          <div className="p-6">
            {labels.length === 0 ? (
              <p className="text-gray-500 text-center py-8">No labels yet. Create your first label to categorize issues.</p>
            ) : (
              <div className="flex flex-wrap gap-2">
                {labels.map((label) => (
                  <div
                    key={label.id}
                    className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full text-sm font-medium group"
                    style={{
                      backgroundColor: label.color + '20',
                      color: label.color,
                      border: `1px solid ${label.color}`,
                    }}
                  >
                    <span>{label.name}</span>
                    <button
                      onClick={() => handleDeleteLabel(label.id, label.name)}
                      className="opacity-0 group-hover:opacity-100 transition-opacity hover:bg-white/20 rounded-full p-0.5"
                      title="Delete label"
                    >
                      <X className="w-3 h-3" />
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Danger Zone */}
        <div className="bg-white rounded-lg shadow-sm border border-red-200">
          <div className="px-6 py-4 border-b border-red-200">
            <h2 className="text-lg font-semibold text-red-900">Danger Zone</h2>
          </div>
          <div className="p-6 space-y-4">
            {project?.archived ? (
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="font-medium text-gray-900">Restore Project</h3>
                  <p className="text-sm text-gray-600">Make this project visible again</p>
                </div>
                <button
                  onClick={handleRestore}
                  disabled={restoreMutation.isPending}
                  className="flex items-center gap-2 px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors disabled:opacity-50"
                >
                  <RotateCcw className="w-4 h-4" />
                  {restoreMutation.isPending ? 'Restoring...' : 'Restore Project'}
                </button>
              </div>
            ) : (
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="font-medium text-gray-900">Archive Project</h3>
                  <p className="text-sm text-gray-600">Hide this project from the main view</p>
                </div>
                <button
                  onClick={handleArchive}
                  disabled={archiveMutation.isPending}
                  className="flex items-center gap-2 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors disabled:opacity-50"
                >
                  <Archive className="w-4 h-4" />
                  {archiveMutation.isPending ? 'Archiving...' : 'Archive Project'}
                </button>
              </div>
            )}
          </div>
        </div>
      </main>

      {/* Create Label Modal */}
      <CreateLabelModal
        isOpen={isCreateLabelModalOpen}
        onClose={() => setIsCreateLabelModalOpen(false)}
        projectId={Number(projectId)}
      />
    </div>
  );
}

