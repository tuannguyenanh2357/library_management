/** Entity sách dùng chung giữa nhiều feature (books, dashboard, borrowings) */
export interface BooksResponse {
  id: number;
  title: string;
  author: string;
  isbn: string;
  publisher: string;
  category: string;
  description: string;
  publicationYear: number;
  imageUrl?: string;
  dailyFineAmount?: number;
  availableCopiesCount?: number;
}
