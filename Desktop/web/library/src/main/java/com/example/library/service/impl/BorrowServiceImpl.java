package com.example.library.service.impl;

import com.example.library.mapper.BookMapper;
import com.example.library.mapper.BorrowRecordMapper;
import com.example.library.mapper.UserMapper;
import com.example.library.model.Book;
import com.example.library.model.BorrowRecord;
import com.example.library.model.PageBean;
import com.example.library.model.Result;
import com.example.library.service.BorrowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class BorrowServiceImpl implements BorrowService {
    @Autowired
    private BorrowRecordMapper borrowRecordMapper;
    @Autowired
    private BookMapper bookMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public Result borrowBook(Long userId, Long bookId) {
        // 校验用户借书数量未超限
        int currentBorrowCount = borrowRecordMapper.findBorrowingByUser(userId).size();
        Integer maxBorrowNumObj = userMapper.selectById(userId).getMaxBorrowNum();
        int maxBorrowNum = (maxBorrowNumObj != null) ? maxBorrowNumObj : 5;
        if (currentBorrowCount >= maxBorrowNum) {
            return Result.error("已达最大借书数量");
        }
        Book book = bookMapper.selectById(bookId);
        if (book == null || book.getAvailable_qty() == null || book.getAvailable_qty() <= 0) {
            return Result.error("图书可借阅数为0");
        }
        // 插入借阅记录
        BorrowRecord record = new BorrowRecord();
        record.setUserId(userId);
        record.setBookId(bookId);
        record.setBorrow_date(new Date());
        record.setStatus(0);
        record.setDue_date(LocalDateTime.now().plusDays(1)); // 1 天后应还
        int inserted = borrowRecordMapper.insert(record);
        if (inserted > 0) {
            // 更新图书可借阅数
            book.setAvailable_qty(book.getAvailable_qty() - 1);
            book.setUpdateTime(new Date());
            bookMapper.updateBook(book);
            return Result.success("借书成功");
        }
        return Result.error("借书失败");
    }

    @Override
    public Result returnBook(Long borrowId) {
        int updated = borrowRecordMapper.returnBook(borrowId);
        boolean bookUpdated = false;
        if (updated > 0) {
            // 查询借阅记录，获取bookId
            BorrowRecord record = borrowRecordMapper.selectById(borrowId);
            System.out.println("进入更新图书分支，record=" + record);
            if (record != null && record.getBookId() != null) {
                System.out.println("准备查询图书，bookId=" + record.getBookId());
                Book book = bookMapper.selectById(record.getBookId());
                System.out.println("查到的book=" + book);
                if (book != null && book.getAvailable_qty() != null) {
                    book.setAvailable_qty(book.getAvailable_qty() + 1);
                    book.setUpdateTime(new Date());
                    System.out.println("准备更新图书，book=" + book);
                    int rows = bookMapper.updateBook(book);
                    System.out.println("更新完成，影响行数=" + rows);
                    bookUpdated = rows > 0;
                }
            }
            if (bookUpdated) {
                return Result.success("还书成功，可借阅数已更新");
            } else {
                return Result.success("还书成功，但可借阅数未更新");
            }
        }
        return Result.error("还书失败");
    }

    @Override
    public Result returnedPage(Long userId, int page, int pageSize) {
        int total = borrowRecordMapper.countReturnedByUser(userId);
        if (total == 0) {
            return Result.success(new PageBean<>(0, List.of()));
        }
        int start = (page - 1) * pageSize;
        List<Map<String, Object>> rows = borrowRecordMapper.selectReturnedByUser(userId, start, pageSize);
        return Result.success(new PageBean<>(total, rows));
    }

    @Override
    public Result allReturnedPage(int page, int pageSize) {
        int total = borrowRecordMapper.countAllReturned();
        int start = (page - 1) * pageSize;
        List<Map<String, Object>> rows =
                total == 0 ? List.of() : borrowRecordMapper.selectAllReturned(start, pageSize);
        return Result.success(new PageBean<>(total, rows));
    }

    @Override
    public Result renewByBorrowId(Long borrowId) {
        // 1. 必须有在借记录
        BorrowRecord record = borrowRecordMapper.selectBorrowingById(borrowId);
        if (record == null) {
            return Result.error("未找到该图书的在借记录");
        }
        // 2. 简单续借：+3 天（可改成配置）
        LocalDateTime newDue = record.getDue_date().plusDays(3);
        int updated = borrowRecordMapper.renewById(record.getId(), newDue);
        return updated > 0
                ? Result.success("续借成功，新到期日：" + newDue.toLocalDate())
                : Result.error("续借失败");
    }

    @Override
    public Result borrowingPage(Long userId, int page, int pageSize) {
        int total = borrowRecordMapper.countBorrowingByUser(userId);
        if (total == 0) {
            return Result.success(new PageBean<>(0, List.of()));
        }
        int start = (page - 1) * pageSize;
        List<Map<String, Object>> rows = borrowRecordMapper.selectBorrowingByUser(userId, start, pageSize);
        return Result.success(new PageBean<>(total, rows));
    }

    @Override
    public Result allBorrowingPage(int page, int pageSize) {
        int total = borrowRecordMapper.countAllBorrowing();
        int start = (page - 1) * pageSize;
        List<Map<String, Object>> rows =
                total == 0 ? List.of() : borrowRecordMapper.selectAllBorrowing(start, pageSize);
        return Result.success(new PageBean<>(total, rows));
    }
}
