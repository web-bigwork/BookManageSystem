package com.example.library.controller;

import com.example.library.mapper.BookMapper;
import com.example.library.mapper.BorrowRecordMapper;
import com.example.library.mapper.UserMapper;
import com.example.library.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class DashController {

    @Autowired
    private BorrowRecordMapper borrowRecordMapper;

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private com.example.library.mapper.VisitMapper visitMapper;

    @GetMapping("/dashboard")
    public Result getDashboard() {
        int lendRecordCount = 0;
        try {
            lendRecordCount = borrowRecordMapper.countAllBorrowing();
        } catch (Exception ignored) {
        }

        int bookCount = 0;
        try {
            bookCount = bookMapper.count();
        } catch (Exception ignored) {
        }

        int userCount = 0;
        try {
            userCount = userMapper.count();
        } catch (Exception ignored) {
        }

        int visitCount = 0;
        try {
            Integer cnt = visitMapper.getCount();
            visitCount = (cnt != null) ? cnt : 0;
        } catch (Exception ignored) {
        }

        Map<String, Integer> data = new HashMap<>();
        data.put("lendRecordCount", lendRecordCount);
        data.put("visitCount", visitCount);
        data.put("bookCount", bookCount);
        data.put("userCount", userCount);

        return Result.success(data);
    }

    @GetMapping("/visit")
    public Result recordVisit() {
        try {
            visitMapper.incrementVisit();
        } catch (Exception e) {
            return Result.error("记录访问失败");
        }
        return Result.success();
    }
}