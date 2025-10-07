import { api } from '../lib/api';
import type { Issue, CreateIssueRequest, UpdateIssueRequest, IssueHistory, WorkflowState } from '../types/issue';

export const issueService = {
  async getProjectIssues(projectId: number): Promise<Issue[]> {
    const response = await api.get<Issue[]>(`/projects/${projectId}/issues`);
    return response.data;
  },

  async getIssue(id: number): Promise<Issue> {
    const response = await api.get<Issue>(`/issues/${id}`);
    return response.data;
  },

  async getIssueByKey(key: string): Promise<Issue> {
    const response = await api.get<Issue>(`/issues/key/${key}`);
    return response.data;
  },

  async createIssue(projectId: number, data: CreateIssueRequest): Promise<Issue> {
    const response = await api.post<Issue>(`/projects/${projectId}/issues`, data);
    return response.data;
  },

  async updateIssue(id: number, data: UpdateIssueRequest): Promise<Issue> {
    const response = await api.put<Issue>(`/issues/${id}`, data);
    return response.data;
  },

  async getIssueHistory(id: number): Promise<IssueHistory[]> {
    const response = await api.get<IssueHistory[]>(`/issues/${id}/history`);
    return response.data;
  },

  async getWorkflowStates(projectId: number): Promise<WorkflowState[]> {
    // This endpoint doesn't exist yet in backend, but we can get it from project issues
    // For now, we'll extract unique workflow states from issues
    const issues = await this.getProjectIssues(projectId);
    const statesMap = new Map<number, WorkflowState>();
    
    issues.forEach(issue => {
      if (!statesMap.has(issue.workflowState.id)) {
        statesMap.set(issue.workflowState.id, issue.workflowState);
      }
    });
    
    return Array.from(statesMap.values()).sort((a, b) => a.order - b.order);
  },
};

