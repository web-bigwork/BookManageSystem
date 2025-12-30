package com.example.library.service;

import com.example.library.model.Result;

public interface BorrowService {
    Result borrowBook(Long userId, Long bookId);
    Result returnBook(Long borrowId);

    Result returnedPage(Long userId, int page, int pageSize);

    Result allReturnedPage(int page, int pageSize);

    Result renewByBorrowId(Long borrowId);

    // 获取指定用户正在借阅（未归还 status=0）的借阅记录，包含图书信息
    Result getBorrowingBooks(Long userId);

    // 获取指定用户逾期（status=2）的借阅记录，包含图书信息
    Result getOverdueBooks(Long userId);

    // 分页查询所有借阅记录（包含在借和已还），返回联表信息
    Result allRecordsPage(int page, int pageSize);

    // 查询黑名单用户列表
    Result getBlacklistUsers(int page, int pageSize);

    // 获取用户逾期详情（用于黑名单详情查看）
    Result getUserOverdueDetails(Long userId);

    // 移出黑名单
    Result removeFromBlacklist(Long userId);

    // 编辑借阅记录
    Result editRecord(com.example.library.model.BorrowRecord record);

    // 软删除借阅记录
    Result deleteRecord(Long id);

    // 分页查询指定用户的所有借阅记录
    Result userAllRecordsPage(Long userId, int page, int pageSize);
}
