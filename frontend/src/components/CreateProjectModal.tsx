import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { X, FolderPlus } from 'lucide-react';
import type { CreateProjectRequest } from '../types/project';

interface CreateProjectModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (data: CreateProjectRequest) => Promise<void>;
}

export function CreateProjectModal({ isOpen, onClose, onSubmit }: CreateProjectModalProps) {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
    watch,
  } = useForm<CreateProjectRequest>();

  const projectName = watch('name', '');

  const generateKey = (name: string): string => {
    return name
      .toUpperCase()
      .replace(/[^A-Z0-9]/g, '')
      .slice(0, 10);
  };

  const onSubmitForm = async (data: CreateProjectRequest) => {
    try {
      setLoading(true);
      setError('');
      await onSubmit(data);
      reset();
      onClose();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to create project');
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black bg-opacity-50">
      <div className="bg-white rounded-2xl shadow-2xl max-w-md w-full">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b border-gray-200">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-primary-100 rounded-lg flex items-center justify-center">
              <FolderPlus className="w-5 h-5 text-primary-600" />
            </div>
            <h2 className="text-xl font-bold text-gray-900">Create New Project</h2>
          </div>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 transition"
          >
            <X className="w-6 h-6" />
          </button>
        </div>

        {/* Body */}
        <form onSubmit={handleSubmit(onSubmitForm)} className="p-6 space-y-5">
          {error && (
            <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm">
              {error}
            </div>
          )}

          {/* Project Name */}
          <div>
            <label htmlFor="name" className="block text-sm font-medium text-gray-700 mb-2">
              Project Name <span className="text-red-500">*</span>
            </label>
            <input
              {...register('name', {
                required: 'Project name is required',
                minLength: { value: 3, message: 'Name must be at least 3 characters' },
                maxLength: { value: 100, message: 'Name must be at most 100 characters' },
              })}
              type="text"
              id="name"
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent transition"
              placeholder="My Awesome Project"
            />
            {errors.name && (
              <p className="mt-1 text-sm text-red-600">{errors.name.message}</p>
            )}
          </div>

          {/* Project Key */}
          <div>
            <label htmlFor="key" className="block text-sm font-medium text-gray-700 mb-2">
              Project Key <span className="text-red-500">*</span>
            </label>
            <input
              {...register('key', {
                required: 'Project key is required',
                minLength: { value: 2, message: 'Key must be at least 2 characters' },
                maxLength: { value: 10, message: 'Key must be at most 10 characters' },
                pattern: {
                  value: /^[A-Z0-9]+$/,
                  message: 'Key must contain only uppercase letters and numbers',
                },
              })}
              type="text"
              id="key"
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent transition font-mono uppercase"
              placeholder={generateKey(projectName) || 'PROJ'}
              maxLength={10}
            />
            {errors.key && (
              <p className="mt-1 text-sm text-red-600">{errors.key.message}</p>
            )}
            <p className="mt-1 text-xs text-gray-500">
              2-10 uppercase letters/numbers. Used for issue keys (e.g., {watch('key') || 'PROJ'}-123)
            </p>
          </div>

          {/* Description */}
          <div>
            <label htmlFor="description" className="block text-sm font-medium text-gray-700 mb-2">
              Description
            </label>
            <textarea
              {...register('description')}
              id="description"
              rows={3}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent transition resize-none"
              placeholder="Brief description of your project..."
            />
          </div>

          {/* Actions */}
          <div className="flex gap-3 pt-4">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 px-4 py-3 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition font-medium"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="flex-1 px-4 py-3 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition font-medium disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? 'Creating...' : 'Create Project'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

