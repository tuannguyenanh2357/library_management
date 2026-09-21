export interface FineResponse {
  id: number;
  borrowingId: number;
  memberId: number;
  memberName: string;
  bookTitle: string;
  amount: number;
  reason: string;
  status: string; // 'UNPAID', 'PAID'
  issuedDate: string;
  paidDate?: string | null;
}
