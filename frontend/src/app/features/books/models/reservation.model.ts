export enum ReservationStatus {
  PENDING = 'PENDING',
  FULFILLED = 'FULFILLED',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
  EXPIRED = 'EXPIRED'
}

export interface ReservationResponse {
  id: number;
  memberId: number;
  memberName: string;
  bookId: number;
  bookTitle: string;
  bookCover: string;
  fulfilledCopyId?: number;
  fulfilledCopyBarcode?: string;
  status: ReservationStatus;
  requestDate: string;
  fulfilledDate?: string;
  expiryDate?: string;
  expectedAvailableDate?: string;
}

export interface ReservationCreationRequest {
  memberId: number;
  bookId: number;
}
