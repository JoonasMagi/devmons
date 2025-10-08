import { Fragment } from 'react';
import { Dialog, Transition } from '@headlessui/react';
import { X, Tag } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { projectService } from '../services/projectService';
import type { CreateLabelRequest } from '../types/project';

interface CreateLabelModalProps {
  isOpen: boolean;
  onClose: () => void;
  projectId: number;
}

const PRESET_COLORS = [
  '#EF4444', // Red
  '#F59E0B', // Orange
  '#10B981', // Green
  '#3B82F6', // Blue
  '#8B5CF6', // Purple
  '#EC4899', // Pink
  '#6B7280', // Gray
  '#14B8A6', // Teal
];

export function CreateLabelModal({ isOpen, onClose, projectId }: CreateLabelModalProps) {
  const queryClient = useQueryClient();
  const { register, handleSubmit, formState: { errors }, reset, watch, setValue } = useForm<CreateLabelRequest>({
    defaultValues: {
      color: PRESET_COLORS[0],
    },
  });

  const selectedColor = watch('color');

  const createMutation = useMutation({
    mutationFn: (data: CreateLabelRequest) => projectService.createLabel(projectId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['project-labels', projectId] });
      toast.success('Label created successfully');
      reset({
        name: '',
        color: PRESET_COLORS[0],
      });
      onClose();
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to create label');
    },
  });

  const onSubmit = (data: CreateLabelRequest) => {
    createMutation.mutate(data);
  };

  const handleClose = () => {
    reset({
      name: '',
      color: PRESET_COLORS[0],
    });
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
              <Dialog.Panel className="w-full max-w-md transform overflow-hidden rounded-2xl bg-white p-6 text-left align-middle shadow-xl transition-all">
                {/* Header */}
                <div className="flex items-center justify-between mb-6">
                  <Dialog.Title as="h3" className="text-lg font-semibold text-gray-900 flex items-center gap-2">
                    <Tag className="w-5 h-5 text-primary-600" />
                    Create Label
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
                  {/* Label Name */}
                  <div>
                    <label htmlFor="name" className="block text-sm font-medium text-gray-700 mb-1">
                      Label Name
                    </label>
                    <input
                      {...register('name', {
                        required: 'Label name is required',
                        maxLength: {
                          value: 50,
                          message: 'Label name must be at most 50 characters',
                        },
                      })}
                      type="text"
                      id="name"
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                      placeholder="e.g., frontend, urgent, bug"
                    />
                    {errors.name && (
                      <p className="mt-1 text-sm text-red-600">{errors.name.message}</p>
                    )}
                  </div>

                  {/* Color Picker */}
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Color
                    </label>
                    <div className="grid grid-cols-4 gap-2 mb-3">
                      {PRESET_COLORS.map((color) => (
                        <button
                          key={color}
                          type="button"
                          onClick={() => setValue('color', color)}
                          className={`w-full h-10 rounded-lg transition-all ${
                            selectedColor === color
                              ? 'ring-2 ring-offset-2 ring-primary-500 scale-110'
                              : 'hover:scale-105'
                          }`}
                          style={{ backgroundColor: color }}
                        />
                      ))}
                    </div>
                    <input
                      {...register('color', { required: 'Color is required' })}
                      type="color"
                      value={selectedColor}
                      onChange={(e) => setValue('color', e.target.value)}
                      className="w-full h-10 rounded-lg border border-gray-300 cursor-pointer"
                    />
                    {errors.color && (
                      <p className="mt-1 text-sm text-red-600">{errors.color.message}</p>
                    )}
                  </div>

                  {/* Preview */}
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Preview
                    </label>
                    <div className="flex items-center gap-2">
                      <span
                        className="px-3 py-1.5 rounded-full text-sm font-medium"
                        style={{
                          backgroundColor: selectedColor + '20',
                          color: selectedColor,
                          border: `1px solid ${selectedColor}`,
                        }}
                      >
                        {watch('name') || 'Label Name'}
                      </span>
                    </div>
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
                      {createMutation.isPending ? 'Creating...' : 'Create Label'}
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

