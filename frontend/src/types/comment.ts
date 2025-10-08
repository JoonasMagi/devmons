export interface Comment {
  id: number;
  issueId: number;
  author: {
    id: number;
    username: string;
    fullName: string;
  };
  content: string;
  createdAt: string;
  updatedAt: string;
  isEdited: boolean;
}

export interface CreateCommentRequest {
  content: string;
}

export interface UpdateCommentRequest {
  content: string;
}

