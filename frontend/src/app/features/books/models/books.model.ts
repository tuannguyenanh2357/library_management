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

export interface CreateBookRequest {
    title: string;
    author: string;
    isbn: string;
    publisher: string;
    category: string;
    description: string;
    publicationYear: number;
    imageUrl?: string;
    dailyFineAmount?: number;
}

export interface UpdateBookRequest {
    title: string;
    author: string;
    isbn: string;
    publisher: string;
    category: string;
    description: string;
    publicationYear: number;
    imageUrl?: string;
    dailyFineAmount?: number;

}

