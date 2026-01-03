package com.example.library.service;

import com.example.library.model.Book;
import com.example.library.model.Result;

import java.util.List;

public interface BookService {

	Result addBook(String title, String author, Integer available_qty);

	Result updateBook(Long id,Integer available_qty);

	boolean deleteBookById(Long id);

	Book getById(Long id);

	List<Book> findByTitle(String keyword);

	List<Book> findByAuthor(String author);

	com.example.library.model.PageBean<com.example.library.model.Book> page(int page, int pageSize);
}
