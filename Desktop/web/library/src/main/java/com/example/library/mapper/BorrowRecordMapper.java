package com.example.library.mapper;

import com.example.library.model.BorrowRecord;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface BorrowRecordMapper {
        @Select("SELECT * FROM borrow_record WHERE id = #{id}")
        BorrowRecord selectById(Long id);
    @Insert("INSERT INTO borrow_record(user_id, book_id, borrow_date,due_date, status) VALUES(#{userId}, #{bookId}, #{borrow_date}, #{due_date}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(BorrowRecord record);

    @Select("SELECT * FROM borrow_record WHERE user_id = #{userId} AND status = 0")
    List<BorrowRecord> findBorrowingByUser(Long userId);

    @Select("SELECT * FROM borrow_record WHERE book_id = #{bookId} AND status = 0")
    List<BorrowRecord> findBorrowingByBook(Long bookId);

    @Update("UPDATE borrow_record SET status = 1, return_date = NOW() WHERE id = #{id}")
    int returnBook(Long id);

    /**
     * 分页查询指定用户已归还记录，联查图书标题、作者
     */
    @Select("SELECT br.id, br.borrow_date, br.due_date, br.return_date, br.status, " +
            "b.title AS bookTitle, b.author AS bookAuthor ," +
            "u.userName AS userName " +
            "FROM borrow_record br " +
            "JOIN book b ON br.book_id = b.id " +
            "JOIN user u ON br.user_id = u.id " +
            "WHERE br.user_id = #{userId} AND br.status = 1 " +
            "ORDER BY br.return_date DESC " +
            "LIMIT #{start}, #{pageSize}")
    List<Map<String, Object>> selectReturnedByUser(@Param("userId") Long userId,
                                                   @Param("start") int start,
                                                   @Param("pageSize") int pageSize);

    @Select("SELECT COUNT(*) FROM borrow_record WHERE user_id = #{userId} AND status = 1")
    int countReturnedByUser(@Param("userId") Long userId);

    /**
     * 分页查询所有已归还记录，联查图书
     */
    @Select("SELECT br.id, br.user_id, br.borrow_date, br.due_date, br.return_date, br.status, " +
            "b.title AS bookTitle, b.author AS bookAuthor, " +
            "u.userName AS userName " +
            "FROM borrow_record br " +
            "JOIN book b ON br.book_id = b.id " +
            "JOIN user u ON br.user_id = u.id " +
            "WHERE br.status = 1 " +
            "ORDER BY br.return_date DESC " +
            "LIMIT #{start}, #{pageSize}")
    List<Map<String, Object>> selectAllReturned(@Param("start") int start,
                                                @Param("pageSize") int pageSize);

    @Select("SELECT COUNT(*) FROM borrow_record WHERE status = 1")
    int countAllReturned();

    /**
     * 根据主键查询在借记录（status = 0）
     */
    @Select("SELECT * FROM borrow_record WHERE id = #{id} AND status = 0")
    BorrowRecord selectBorrowingById(@Param("id") Long id);

    /**
     * 续借：更新到期日 & 更新时间
     */
    @Update("UPDATE borrow_record " +
            "SET due_date = #{dueDate} " +
            "WHERE id = #{id} AND status = 0")
    int renewById(@Param("id") Long id, @Param("dueDate") LocalDateTime dueDate);

    /**
     * 分页查询指定用户借阅中记录，联查图书标题、作者
     */
    @Select("SELECT br.id, br.borrow_date, br.due_date, br.return_date, br.status, " +
            "b.title AS bookTitle, b.author AS bookAuthor ," +
            "u.userName AS userName " +
            "FROM borrow_record br " +
            "JOIN book b ON br.book_id = b.id " +
            "JOIN user u ON br.user_id = u.id " +
            "WHERE br.user_id = #{userId} AND br.status = 0 " +
            "ORDER BY br.return_date DESC " +
            "LIMIT #{start}, #{pageSize}")
    List<Map<String, Object>> selectBorrowingByUser(@Param("userId") Long userId,
                                                   @Param("start") int start,
                                                   @Param("pageSize") int pageSize);

    @Select("SELECT COUNT(*) FROM borrow_record WHERE user_id = #{userId} AND status = 0")
    int countBorrowingByUser(@Param("userId") Long userId);

    /**
     * 分页查询所有已归还记录，联查图书
     */
    @Select("SELECT br.id, br.user_id, br.borrow_date, br.due_date, br.return_date, br.status, " +
            "b.title AS bookTitle, b.author AS bookAuthor, " +
            "u.userName AS userName " +
            "FROM borrow_record br " +
            "JOIN book b ON br.book_id = b.id " +
            "JOIN user u ON br.user_id = u.id " +
            "WHERE br.status = 0 " +
            "ORDER BY br.return_date DESC " +
            "LIMIT #{start}, #{pageSize}")
    List<Map<String, Object>> selectAllBorrowing(@Param("start") int start,
                                                @Param("pageSize") int pageSize);

    @Select("SELECT COUNT(*) FROM borrow_record WHERE status = 0")
    int countAllBorrowing();
}
