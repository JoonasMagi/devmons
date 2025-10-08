export interface Project {
  id: number;
  name: string;
  key: string;
  description?: string;
  ownerId: number;
  ownerUsername: string;
  archived: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateProjectRequest {
  name: string;
  key: string;
  description?: string;
}

export interface UpdateProjectRequest {
  name?: string;
  description?: string;
}

export interface Label {
  id: number;
  name: string;
  color: string;
}

export interface CreateLabelRequest {
  name: string;
  color: string;
}

export interface ProjectMember {
  id?: number;
  userId: number;
  username: string;
  fullName: string;
  email: string;
  role: string;
  joinedAt?: string;
}

