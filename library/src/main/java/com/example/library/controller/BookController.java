package com.example.library.controller;

import com.example.library.model.Book;
import com.example.library.model.PageBean;
import com.example.library.model.Result;
import com.example.library.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class BookController {

    @Autowired
    private BookService bookService;

    @GetMapping("/delById")
    public Result deleteUserById(Long id) {
        System.out.println("收到id=" + id);
        boolean success = bookService.deleteBookById(id);
        if (success) {
            return Result.success("图书已删除");
        }
        return Result.error("图书不存在或已被删除");
    }


    @PostMapping("/addBook")
    public Result addBook(@RequestBody Map<String, Object> addData) {
        System.out.println("原始 Map = " + addData);   // 关键调试
        // 1. 取参 & 强转
        String title   = (String) addData.get("title");
        String author  = (String) addData.get("author");
        Integer available_qty = (Integer) addData.get("available_qty");

        // 2. 简单校验
        if (title == null || title.isBlank() || author == null || author.isBlank()) {
            return Result.error("书名或作者为空");
        }
        if (available_qty == null || available_qty < 0) {
            available_qty = 0;   // 默认库存
        }

        Result result = bookService.addBook(title,author, available_qty);
        return result;
    }

    @PostMapping("/updateBook")
    public Result updateBookQty(@RequestBody Map<String, Object> updateData) {
        System.out.println("原始 Map = " + updateData);
        // 1. 取参 & 强转
        Integer id = (Integer) updateData.get("id");
        Integer availableQty = (Integer) updateData.get("available_qty");

        // 2. 简单校验
        if (id == null || id <= 0) {
            return Result.error("书籍ID不能为空或非法");
        }
        if (availableQty == null || availableQty < 0) {
            return Result.error("可借阅数不能小于0");
        }

        // 3. 调 service
        return bookService.updateBook(id.longValue(), availableQty);
    }

    @GetMapping("/findByTitle")
    public Result findByTitle(@RequestParam(required = false) String keyword) {
        List<Book> list = bookService.findByTitle(keyword);
        return Result.success(list);
    }

    @GetMapping("/findByAuthor")
    public Result findByAuthor(@RequestParam(required = false) String author) {
        List<Book> list = bookService.findByAuthor(author);
        return Result.success(list);
    }

    @GetMapping("/findById")
    public Result findById(@RequestParam Long id) {
        if (id == null) {
            return Result.error("书籍ID不能为空");
        }
        Book book = bookService.getById(id);
        if (book == null) {
            return Result.error("图书不存在");
        }
        return Result.success(book);
    }

    @GetMapping("/books")
    public Result getBooksPage(@RequestParam(defaultValue = "1") Integer page,
                               @RequestParam(defaultValue = "5") Integer pageSize) {
        PageBean<Book> pageBean = bookService.page(page, pageSize);
        return Result.success(pageBean);
    }
    
}
