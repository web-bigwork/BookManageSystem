package com.example.library.service;

import com.example.library.model.Result;

public interface BorrowService {
    Result borrowBook(Long userId, Long bookId);
    Result returnBook(Long borrowId);

    Result returnedPage(Long userId, int page, int pageSize);

    Result allReturnedPage(int page, int pageSize);

    Result renewByBorrowId(Long borrowId);

    Result borrowingPage(Long userId, int page, int pageSize);

    Result allBorrowingPage(int page, int pageSize);
}
