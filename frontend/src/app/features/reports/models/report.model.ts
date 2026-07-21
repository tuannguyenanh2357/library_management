export interface DailyEntry {
  date: string;
  dayName: string;
  collectedAmount: number;
  collectedCount: number;
}

export interface TopOffenderEntry {
  memberId: number;
  memberName: string;
  email: string;
  fineCount: number;
  totalFineAmount: number;
  paidAmount: number;
  unpaidAmount: number;
}

export interface TopPenalizedBookEntry {
  bookId: number;
  bookTitle: string;
  author: string;
  fineCount: number;
  totalFineAmount: number;
}

export interface WeeklyRevenueReport {
  fromDate: string;
  toDate: string;
  rangeDays: number;

  collectedAmount: number;
  collectedCount: number;
  pendingAmount: number;
  pendingCount: number;
  totalBorrowings: number;

  prevCollectedAmount: number;
  growthPercent: number;

  dailyBreakdown: DailyEntry[];
  topOffenders: TopOffenderEntry[];
  topBooks: TopPenalizedBookEntry[];
}
