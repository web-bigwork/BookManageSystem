package com.example.library.controller;

import com.example.library.model.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class DashboardController {

    /**
     * 仪表盘统计接口
     * 对应前端 /dashboard 请求
     */
    @GetMapping("/dashboard")
    public Result getDashboardData() {
        Map<String, Object> data = new HashMap<>();

        // 简单示例数据：后续你可以改成调用 Service 做真实统计
        data.put("lendRecordCount", 100); // 总借阅数
        data.put("visitCount", 200);      // 访问量
        data.put("bookCount", 300);       // 图书总数
        data.put("userCount", 50);        // 用户总数

        return Result.success(data);
    }
}


