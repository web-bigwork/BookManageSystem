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
     * 编辑借阅记录（可改 status, due_date, return_date）
     * POST /borrow/update
     * BODY: { id: 1, status: 1, dueDate: "2025-12-20T12:00:00", returnTime: "2025-12-20 12:00:00" }
     */
    @PostMapping("/borrow/update")
    public Result updateBorrowRecord(@RequestBody Map<String, Object> body) {
        Object idObj = body.get("id");
        if (idObj == null) return Result.error("ID 不能为空");
        Long id = ((Number) idObj).longValue();

        com.example.library.model.BorrowRecord record = new com.example.library.model.BorrowRecord();
        record.setId(id);

        Object statusObj = body.get("status");
        if (statusObj instanceof Number) {
            record.setStatus(((Number) statusObj).intValue());
        } else if (statusObj != null) {
            try {
                record.setStatus(Integer.valueOf(statusObj.toString()));
            } catch (NumberFormatException ignored) {}
        }

        Object dueObj = body.get("dueDate");
        if (dueObj instanceof String) {
            try {
                record.setDue_date(java.time.LocalDateTime.parse((String) dueObj));
            } catch (Exception ignored) {}
        }

        Object returnObj = body.get("returnTime");
        if (returnObj instanceof String) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                java.util.Date rt = ((String) returnObj).isBlank() ? null : sdf.parse((String) returnObj);
                record.setReturnTime(rt);
            } catch (Exception ignored) {}
        }

        return borrowService.editRecord(record);
    }
}
