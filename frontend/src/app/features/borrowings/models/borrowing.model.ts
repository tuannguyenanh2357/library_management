export interface BorrowingResponse {
  id: number;
  memberId: number;
  memberName: string;
  bookId: number;
  barCode: string;
  bookTitle: string;
  borrowDate: string;
  dueDate: string;
  returnDate?: string | null;
  bookCopy: string;
  renewalCount?: number;
}

export interface BorrowingCreationRequest {
  memberId: number;
  bookCopyId: number;
  dueDate: string;
}
