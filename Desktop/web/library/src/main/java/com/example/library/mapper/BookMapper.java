
package com.example.library.mapper;

import com.example.library.model.Book;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BookMapper {

	@Insert("insert into book(title, author, isDelete, createTime, updateTime, available_qty) " +
			"values(#{title}, #{author}, #{isDelete}, #{createTime}, #{updateTime}, #{available_qty})")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int insert(Book book);

	@Update("UPDATE book SET available_qty = #{available_qty}, updateTime = #{updateTime} WHERE id = #{id}")
	int updateBook(Book book);

	@Select("SELECT COUNT(*) FROM book WHERE title = #{title} AND isDelete = 0")
	public int existsByName(@Param("title") String title);

	@Update("UPDATE book SET isDelete = 1, updateTime = NOW() WHERE id = #{id} AND isDelete = 0")
	int deleteById(Long id);

	@Select("select * from book where id=#{id}")
	Book selectById(Long id);

	@Select("select * from book where title like #{keyword} and (isDelete is null or isDelete=0)")
	List<Book> findByTitle(String keyword);

	@Select("select * from book where author like #{author} and (isDelete is null or isDelete=0)")
	List<Book> findByAuthor(@Param("author") String author);

    @Select("select count(*) from book where isDelete is null or isDelete=0")
    int count();

    @Select("select * from book where isDelete is null or isDelete=0 limit #{start}, #{pageSize}")
    List<Book> page(@Param("start") int start, @Param("pageSize") int pageSize);
}
