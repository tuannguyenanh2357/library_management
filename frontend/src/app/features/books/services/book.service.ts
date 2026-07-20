import { Injectable, inject } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable  } from "rxjs";
import { BooksResponse, CreateBookRequest, UpdateBookRequest } from "../models/books.model";
import { PageResponse } from "../../../shared/models/page.model";
import { environment } from '../../../../environments/environment';

@Injectable({
    providedIn: "root",
})
export class BookService {
    private apiUrl = `${environment.apiUrl}/books`;

    private http = inject(HttpClient);

    // lấy tất cả sách rồi phân trang
    getAllBooks(
        title?: string,
        author?: string,
        category?: string,
        page: number = 0,
        size: number = 10,
        id?: number,
        publisher?: string,
        isbn?: string
    ): Observable<PageResponse<BooksResponse>> {
        let params: any = { page: page.toString(), size: size.toString() };
        if (title) params.title = title;
        if (author) params.author = author;
        if (category) params.category = category;
        if (id !== undefined && id !== null) params.id = id.toString();
        if (publisher) params.publisher = publisher;
        if (isbn) params.isbn = isbn;
        return this.http.get<PageResponse<BooksResponse>>(this.apiUrl, { params });
    }

    // lấy danh sách các thể loại duy nhất
    getCategories(): Observable<string[]> {
        return this.http.get<string[]>(`${this.apiUrl}/categories`);
    }

    // lấy 10 cuốn sách được mượn nhiều nhat tuần
    getPopularBooks(): Observable<BooksResponse[]> {
        return this.http.get<BooksResponse[]>(`${this.apiUrl}/popular`);
    }

    // lay sach theo ID
    getBookById(id: number): Observable<BooksResponse> {
        return this.http.get<BooksResponse>(`${this.apiUrl}/${id}`);
    }

    // tạo 1 sách mơi
    createBook(request: CreateBookRequest): Observable<BooksResponse> {
        return this.http.post<BooksResponse>(this.apiUrl, request);
    }

    // Update sách
    updateBook(id: number, request: UpdateBookRequest): Observable<BooksResponse> {
        return this.http.put<BooksResponse>(`${this.apiUrl}/${id}`, request);
    }

    // xoa sách
    deleteBook(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }
}
