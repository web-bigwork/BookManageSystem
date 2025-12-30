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
     * 查询指定用户正在借阅的图书（未归还的）
     * GET /borrowing/{userId}
     * 返回该用户 status=0（未归还）的所有借阅记录
     */
    @GetMapping("/borrowing/{userId}")
    public Result getBorrowingBooks(@PathVariable Long userId) {
        return borrowService.getBorrowingBooks(userId);
    }

    /**
     * 查询指定用户逾期的图书
     * GET /overdue/{userId}
     * 返回该用户 status=2（逾期）的所有借阅记录
     */
    @GetMapping("/overdue/{userId}")
    public Result getOverdueBooks(@PathVariable Long userId) {
        return borrowService.getOverdueBooks(userId);
    }

    /**
     * 分页查询所有借阅记录（包含在借和已还）
     * GET /borrow/records?page=1&pageSize=10
     */
    @GetMapping("/borrow/records")
    public Result getAllBorrowRecords(@RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "10") int pageSize) {
        return borrowService.allRecordsPage(page, pageSize);
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
                String dueDateStr = (String) dueObj;
                // 处理前端发送的格式，如 '2025-12-26T15:48:56'
                if (dueDateStr.contains("T") && !dueDateStr.contains(".")) {
                    dueDateStr += ".000"; // 添加毫秒部分
                }
                record.setDue_date(java.time.LocalDateTime.parse(dueDateStr));
            } catch (Exception e) {
                System.err.println("解析 dueDate 失败: " + dueObj + ", 错误: " + e.getMessage());
            }
        }

        Object returnObj = body.get("returnTime");
        if (returnObj instanceof String && !((String) returnObj).trim().isEmpty()) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                record.setReturnTime(sdf.parse((String) returnObj));
            } catch (Exception ignored) {}
        } else if (returnObj == null || (returnObj instanceof String && ((String) returnObj).trim().isEmpty())) {
            // 如果前端传递了空字符串或null，设置为null
            record.setReturnTime(null);
        }

        return borrowService.editRecord(record);
    }



    /**
     * 编辑借阅记录
     * POST /borrow/edit
     * BODY: { id, userId, bookId, borrow_date, due_date, return_date, status }
     */
    @PostMapping("/borrow/edit")
    public Result editBorrowRecord(@RequestBody Map<String, Object> body) {
        Number idNum = (Number) body.get("id");
        if (idNum == null) return Result.error("借阅记录ID不能为空");
        com.example.library.model.BorrowRecord record = new com.example.library.model.BorrowRecord();
        record.setId(idNum.longValue());
        if (body.get("userId") instanceof Number) record.setUserId(((Number) body.get("userId")).longValue());
        if (body.get("bookId") instanceof Number) record.setBookId(((Number) body.get("bookId")).longValue());
        // borrow_date and return_date parsing omitted - expect frontend to send proper Date string or null
        if (body.get("status") instanceof Number) record.setStatus(((Number) body.get("status")).intValue());
        // due_date: expect ISO_LOCAL_DATE_TIME string or similar; leave null if not present
        return borrowService.editRecord(record);
    }

    /**
     * 软删除借阅记录
     * GET /borrow/delete/{id}
     */
    @GetMapping("/borrow/delete/{id}")
    public Result deleteBorrowRecord(@PathVariable Long id) {
        return borrowService.deleteRecord(id);
    }

    /**
     * 分页查询指定用户的所有借阅记录
     * GET /borrow/user/{userId}/records?page=1&pageSize=10
     */
    @GetMapping("/borrow/user/{userId}/records")
    public Result getUserAllBorrowRecords(@PathVariable Long userId,
                                          @RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "5") int pageSize) {
        return borrowService.userAllRecordsPage(userId, page, pageSize);
    }

    /**
     * 分页查询黑名单用户
     * GET /blacklist/users?page=1&pageSize=10
     */
    @GetMapping("/blacklist/users")
    public Result getBlacklistUsers(@RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int pageSize) {
        return borrowService.getBlacklistUsers(page, pageSize);
    }

    /**
     * 获取用户的逾期书籍详情
     * GET /blacklist/user/{userId}/details
     */
    @GetMapping("/blacklist/user/{userId}/details")
    public Result getUserOverdueDetails(@PathVariable Long userId) {
        return borrowService.getUserOverdueDetails(userId);
    }

    /**
     * 移出黑名单
     * POST /blacklist/remove
     * BODY: { "userId": 123 }
     */
    @PostMapping("/blacklist/remove")
    public Result removeFromBlacklist(@RequestBody Map<String, Object> body) {
        Object userIdObj = body.get("userId");
        if (userIdObj == null) return Result.error("用户ID不能为空");
        Long userId = ((Number) userIdObj).longValue();
        return borrowService.removeFromBlacklist(userId);
    }
}
