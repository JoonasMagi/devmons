import { api } from '../lib/api';
import type { Project, CreateProjectRequest, UpdateProjectRequest } from '../types/project';

export const projectService = {
  async getMyProjects(): Promise<Project[]> {
    const response = await api.get<Project[]>('/projects/my');
    return response.data;
  },

  async getProject(id: number): Promise<Project> {
    const response = await api.get<Project>(`/projects/${id}`);
    return response.data;
  },

  async createProject(data: CreateProjectRequest): Promise<Project> {
    const response = await api.post<Project>('/projects', data);
    return response.data;
  },

  async updateProject(id: number, data: UpdateProjectRequest): Promise<Project> {
    const response = await api.put<Project>(`/projects/${id}`, data);
    return response.data;
  },

  async archiveProject(id: number): Promise<void> {
    await api.post(`/projects/${id}/archive`);
  },

  async restoreProject(id: number): Promise<void> {
    await api.post(`/projects/${id}/restore`);
  },
};

