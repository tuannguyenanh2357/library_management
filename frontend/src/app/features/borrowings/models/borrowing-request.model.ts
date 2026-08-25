export interface BorrowingRequestResponse {
  id: number;
  memberId: number;
  memberName: string;
  bookId: number;
  bookTitle: string;
  status: string;
  requestDate: string;
  expectedDueDate?: string;
  processedDate?: string | null;
  notes?: string | null;
  availableCopiesCount: number;
  assignedBookCopyId?: number | null;
  approvedDate?: string | null;
}

export interface BorrowingRequestCreationRequest {
  memberId: number;
  bookId: number;
  expectedDueDate: string;
}

export interface BorrowingRequestApprovalRequest {
  barcode: string;
  dueDate: string;
}
