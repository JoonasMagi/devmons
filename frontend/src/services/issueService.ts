import { api } from '../lib/api';
import type { Issue, CreateIssueRequest, UpdateIssueRequest, IssueHistory, WorkflowState } from '../types/issue';

// Transform backend response to frontend Issue type
const transformIssue = (data: any): Issue => {
  return {
    ...data,
    issueType: {
      id: data.issueTypeId,
      name: data.issueTypeName,
      icon: data.issueTypeIcon,
    },
    workflowState: {
      id: data.workflowStateId,
      name: data.workflowStateName,
      order: data.workflowStateOrder,
      terminal: data.workflowStateTerminal,
    },
    reporter: {
      id: data.reporterId,
      username: data.reporterUsername,
      email: '', // Not provided by backend
      fullName: data.reporterFullName,
    },
    assignee: data.assigneeId ? {
      id: data.assigneeId,
      username: data.assigneeUsername,
      email: '', // Not provided by backend
      fullName: data.assigneeFullName,
    } : undefined,
  };
};

export const issueService = {
  async getProjectIssues(projectId: number): Promise<Issue[]> {
    const response = await api.get<any[]>(`/projects/${projectId}/issues`);
    return response.data.map(transformIssue);
  },

  async getIssue(id: number): Promise<Issue> {
    const response = await api.get<any>(`/issues/${id}`);
    return transformIssue(response.data);
  },

  async getIssueByKey(key: string): Promise<Issue> {
    const response = await api.get<any>(`/issues/key/${key}`);
    return transformIssue(response.data);
  },

  async createIssue(projectId: number, data: CreateIssueRequest): Promise<Issue> {
    const response = await api.post<any>(`/projects/${projectId}/issues`, data);
    return transformIssue(response.data);
  },

  async updateIssue(id: number, data: UpdateIssueRequest): Promise<Issue> {
    const response = await api.put<any>(`/issues/${id}`, data);
    return transformIssue(response.data);
  },

  async getIssueHistory(id: number): Promise<IssueHistory[]> {
    const response = await api.get<any[]>(`/issues/${id}/history`);
    return response.data.map((item) => ({
      id: item.id,
      field: item.fieldName,
      oldValue: item.oldValue,
      newValue: item.newValue,
      changedBy: {
        id: 0, // Not provided by backend
        username: item.changedByUsername,
        email: '', // Not provided by backend
        fullName: item.changedByFullName,
      },
      changedAt: item.changedAt,
    }));
  },
};

