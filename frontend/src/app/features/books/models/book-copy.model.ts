export interface BookCopyResponse {
  id: number;
  barCode: string;
  status: string;
  author?: string;
  title?: string;
  dueDate?: string;
  bookId?: number;
}

export interface BookCopyCreationRequest {
  bookId: number;
  barCode?: string;
}

export interface BookCopyUpdateRequest {
  bookId: number;
  status: string;
}
