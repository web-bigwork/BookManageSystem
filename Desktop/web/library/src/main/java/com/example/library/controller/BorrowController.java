package com.example.library.controller;

import com.example.library.model.Result;
import com.example.library.service.BorrowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class BorrowController {
    @Autowired
    private BorrowService borrowService;

    @PostMapping("/borrow")
    public Result borrowBook(@RequestBody Map<String, Object> data) {
        Long userId = ((Number)data.get("userId")).longValue();
        Long bookId = ((Number)data.get("bookId")).longValue();
        return borrowService.borrowBook(userId, bookId);
    }

    @PostMapping("/return")
    public Result returnBook(@RequestBody Map<String, Object> data) {
        Long borrowId = ((Number)data.get("borrowId")).longValue();
        return borrowService.returnBook(borrowId);
    }

    /**
     * 查询指定用户已归还图书记录（分页）
     */
    @PostMapping("/returned")
    public Result returnedPage(@RequestBody Map<String,Object> param){
        Long userId   = ((Number) param.get("userId")).longValue();
        int page      = (int) param.get("page");
        int pageSize  = (int) param.get("pageSize");
        return borrowService.returnedPage(userId, page, pageSize);
    }

    /**
     * 查询所有已归还图书记录（分页）
     */
    @PostMapping("/allReturned")
    public Result allReturnedPage(@RequestBody Map<String, Integer> param) {
        int page = param.getOrDefault("page", 1);
        int pageSize = param.getOrDefault("pageSize", 10);
        return borrowService.allReturnedPage(page, pageSize);
    }

    /**
     * 通过借书记录ID续借
     * POST /borrow/renewById
     * BODY: {"borrowId":2}
     */
    @PostMapping("/renewById")
    public Result renewByBorrowId(@RequestBody Map<String,Integer> param) {
        Long borrowId = ((Number) param.get("borrowId")).longValue();
        return borrowService.renewByBorrowId(borrowId);
    }

    /**
     * 查询指定用户借阅中图书记录（分页）
     */
    @PostMapping("/borrowing")
    public Result borrowingPage(@RequestBody Map<String,Object> param){
        Long userId   = ((Number) param.get("userId")).longValue();
        int page      = (int) param.get("page");
        int pageSize  = (int) param.get("pageSize");
        return borrowService.borrowingPage(userId, page, pageSize);
    }

    /**
     * 查询所有已归还图书记录（分页）
     */
    @PostMapping("/allBorrowing")
    public Result allBorrowingPage(@RequestBody Map<String, Integer> param) {
        int page = param.getOrDefault("page", 1);
        int pageSize = param.getOrDefault("pageSize", 10);
        return borrowService.allBorrowingPage(page, pageSize);
    }
}
