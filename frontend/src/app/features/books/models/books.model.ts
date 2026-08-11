export interface TopBookProjection {
    bookId: number;
    title: string;
    author: string;
    category: string;
    borrowCount: number;
    imageUrl?: string;
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

