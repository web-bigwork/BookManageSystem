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
    @Insert("INSERT INTO borrow_record(user_id, book_id, borrow_date,due_date, status) VALUES(#{userId}, #{bookId}, #{borrow_date}, #{dueDate}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(BorrowRecord record);

    @Select("<script>" +
            "SELECT * FROM borrow_record WHERE status = 0" +
            "<if test='userId != null'> AND user_id = #{userId}</if>" +
            "</script>")
    List<BorrowRecord> findBorrowingByUser(Long userId);

    /**
     * 查询逾期记录（status = 2）
     */
    @Select("<script>" +
            "SELECT * FROM borrow_record WHERE status = 2" +
            "<if test='userId != null'> AND user_id = #{userId}</if>" +
            "</script>")
    List<BorrowRecord> findOverdueByUser(Long userId);

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
            "ORDER BY br.file:///D:/xwechat_files/wxid_udcqbpcwizxk22_3a3f/temp/RWTemp/2025-12/6c56496ca449822ea3ac100bd34c126f/library.sql DESC " +
            "LIMIT #{start}, #{pageSize}")
    List<Map<String, Object>> selectAllReturned(@Param("start") int start,
                                                @Param("pageSize") int pageSize);

    @Select("SELECT COUNT(*) FROM borrow_record WHERE status = 1")
    int countAllReturned();

    @Select("SELECT COUNT(*) FROM borrow_record")
    int countAllRecords();

    @Select("SELECT br.id, br.user_id AS userId, br.book_id AS bookId, br.borrow_date AS borrowDate, " +
            "br.due_date AS dueDate, br.return_date AS returnDate, br.status, br.isDelete, " +
            "b.title AS bookTitle, b.author AS bookAuthor, " +
            "u.userName AS userName " +
            "FROM borrow_record br " +
            "JOIN book b ON br.book_id = b.id " +
            "JOIN user u ON br.user_id = u.id " +
            "WHERE br.isDelete IS NULL OR br.isDelete = 0 " +
            "ORDER BY br.borrow_date DESC " +
            "LIMIT #{start}, #{pageSize}")
    List<Map<String, Object>> selectAllRecords(@Param("start") int start,
                                               @Param("pageSize") int pageSize);

    @Select("SELECT COUNT(*) FROM borrow_record WHERE user_id = #{userId}")
    int countAllByUser(@Param("userId") Long userId);

    @Select("SELECT br.id, br.user_id AS userId, br.book_id AS bookId, br.borrow_date AS borrowDate, br.due_date AS dueDate, br.return_date AS returnDate, br.status, " +
            "b.title AS bookTitle, b.author AS bookAuthor, " +
            "u.userName AS userName " +
            "FROM borrow_record br " +
            "JOIN book b ON br.book_id = b.id " +
            "JOIN user u ON br.user_id = u.id " +
            "WHERE br.user_id = #{userId} " +
            "ORDER BY br.borrow_date DESC " +
            "LIMIT #{start}, #{pageSize}")
    List<Map<String, Object>> selectAllByUser(@Param("userId") Long userId,
                                              @Param("start") int start,
                                              @Param("pageSize") int pageSize);
    @Select("SELECT COUNT(*) FROM borrow_record WHERE status = 0")
    int countAllBorrowing();

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

    @Update("<script>" +
            "UPDATE borrow_record " +
            "<set>" +
            "<if test='status != null'>status = #{status},</if>" +
            "<if test='dueDate != null'>due_date = #{dueDate},</if>" +
            "return_date = #{returnTime}," +
            "</set>" +
            "WHERE id = #{id} AND (isDelete IS NULL OR isDelete = 0)" +
            "</script>")
    int updateById(com.example.library.model.BorrowRecord record);

    @Update("UPDATE borrow_record SET isDelete = 1 WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Update("UPDATE borrow_record SET status = #{status} WHERE id = #{id} AND (isDelete IS NULL OR isDelete = 0)")
    int updateStatusById(@Param("id") Long id, @Param("status") Integer status);

//    @Update("UPDATE borrow_record SET user_id = #{userId}, book_id = #{bookId}, borrow_date = #{borrow_date}, due_date = #{due_date}, return_date = #{return_date}, status = #{status} WHERE id = #{id}")
//    int updateById(BorrowRecord record);

    @Update("UPDATE borrow_record SET isDelete = 1 WHERE id = #{id}")
    int softDeleteById(@Param("id") Long id);
}
