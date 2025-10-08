import type { Label } from './project';

export type Priority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export interface IssueType {
  id: number;
  name: string;
  icon: string;
}

export interface WorkflowState {
  id: number;
  name: string;
  order: number;
  terminal: boolean;
  allowedTransitions?: number[]; // IDs of states this can transition to
}

export interface User {
  id: number;
  username: string;
  email: string;
  fullName: string;
}

export interface Issue {
  id: number;
  key: string;
  title: string;
  description?: string;
  issueType: IssueType;
  workflowState: WorkflowState;
  priority: Priority;
  boardPosition?: number;
  assignee?: User;
  reporter: User;
  storyPoints?: number;
  dueDate?: string;
  labels: Label[];
  createdAt: string;
  updatedAt: string;
}

export interface CreateIssueRequest {
  title: string;
  description?: string;
  issueTypeId: number;
  priority: Priority;
  assigneeId?: number;
  storyPoints?: number;
  dueDate?: string;
  labelIds?: number[];
}

export interface UpdateIssueRequest {
  title?: string;
  description?: string;
  issueTypeId?: number;
  workflowStateId?: number;
  priority?: Priority;
  assigneeId?: number;
  storyPoints?: number;
  dueDate?: string;
  labelIds?: number[];
  boardPosition?: number;
}

export interface IssueHistory {
  id: number;
  field: string;
  oldValue?: string;
  newValue?: string;
  changedBy: User;
  changedAt: string;
}

export interface BoardColumn {
  workflowState: WorkflowState;
  issues: Issue[];
}

