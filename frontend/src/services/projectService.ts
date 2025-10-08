import { api } from '../lib/api';
import type { Project, CreateProjectRequest, UpdateProjectRequest, ProjectMember, Label, CreateLabelRequest } from '../types/project';
import type { WorkflowState, IssueType } from '../types/issue';

export const projectService = {
  async getMyProjects(): Promise<Project[]> {
    const response = await api.get<Project[]>('/projects');
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

  async getProjectMembers(projectId: number): Promise<ProjectMember[]> {
    const response = await api.get<ProjectMember[]>(`/projects/${projectId}/members`);
    return response.data;
  },

  async getProjectLabels(projectId: number): Promise<Label[]> {
    const response = await api.get<Label[]>(`/projects/${projectId}/labels`);
    return response.data;
  },

  async createLabel(projectId: number, data: CreateLabelRequest): Promise<Label> {
    const response = await api.post<Label>(`/projects/${projectId}/labels`, data);
    return response.data;
  },

  async getWorkflowStates(projectId: number): Promise<WorkflowState[]> {
    const response = await api.get<WorkflowState[]>(`/projects/${projectId}/workflow-states`);
    return response.data;
  },

  async getIssueTypes(projectId: number): Promise<IssueType[]> {
    const response = await api.get<IssueType[]>(`/projects/${projectId}/issue-types`);
    return response.data;
  },
};

