package com.example.library.service.impl;

import com.example.library.mapper.BookMapper;
import com.example.library.model.Book;
import com.example.library.model.Result;
import com.example.library.service.BookService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class BookServiceImpl implements BookService {

	@Autowired
	private BookMapper bookMapper;

	@Override
	public Result addBook(String title,String author,Integer available_qty) {
        if (StringUtils.isBlank(title)) {
            return Result.error("书名不能为空");
        }
		if (StringUtils.isBlank(author)) {
			return Result.error("作者不能为空");
		}

        int userExist = bookMapper.existsByName(title);
        if (userExist > 0) {
            return Result.error("图书已经存在");
        }

        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setAvailable_qty(available_qty);
        book.setIsDelete(0);
		Date now = new Date();
		book.setCreateTime(now);
		book.setUpdateTime(now);
		book.setIsDelete(0);
		int rows = bookMapper.insert(book);
		if (rows > 0) {
			return Result.success("新增书籍成功");
		}
		return Result.error("新增书籍失败");
	}

    @Override
    public Result updateBook(Long id, Integer availableQty) {
        Book book = new Book();
        book.setId(id);
        book.setAvailable_qty(availableQty);   // 只改这一列
        book.setUpdateTime(new Date());       // 更新时间顺手刷新
        int rows = bookMapper.updateBook(book);
        return rows > 0 ? Result.success("更新可借阅数成功")
                : Result.error("更新可借阅数失败");
    }

	@Override
	public boolean deleteBookById(Long id) {
		if (id == null) return false;
		return bookMapper.deleteById(id) > 0;
	}

	@Override
	public Book getById(Long id) {
		if (id == null) return null;
		return bookMapper.selectById(id);
	}

	@Override
	public List<Book> findByTitle(String keyword) {
		if (StringUtils.isBlank(keyword)) return List.of();
		return bookMapper.findByTitle('%' + keyword + '%');
	}

	@Override
	public List<Book> findByAuthor(String author) {
		if (StringUtils.isBlank(author)) return List.of();
		return bookMapper.findByAuthor('%' + author + '%');
	}

    @Override
    public com.example.library.model.PageBean<com.example.library.model.Book> page(int page, int pageSize) {
        int total = bookMapper.count();
        int start = (page - 1) * pageSize;
        List<Book> books = bookMapper.page(start, pageSize);
        com.example.library.model.PageBean<Book> pageBean = new com.example.library.model.PageBean<>();
        pageBean.setTotal(total);
        pageBean.setRows(books);
        return pageBean;
    }
}
