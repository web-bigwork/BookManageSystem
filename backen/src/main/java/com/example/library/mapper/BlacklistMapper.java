package com.example.library.mapper;

import com.example.library.model.Blacklist;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface BlacklistMapper {

    /**
     * 存在即更新，不存在则插入（MySQL 5.7 适用）
     * 注意：user_id 必须建立唯一索引或主键
     */
    @Insert({
            "INSERT INTO blacklist(user_id, reason, overdue_books_count, total_overdue_days, is_active, create_time, update_time) ",
            "VALUES(#{userId}, #{reason}, #{overdueBooksCount}, #{totalOverdueDays}, #{isActive}, NOW(), NOW()) ",
            "ON DUPLICATE KEY UPDATE ",
            "reason = VALUES(reason), ",
            "overdue_books_count = VALUES(overdue_books_count), ",
            "total_overdue_days = VALUES(total_overdue_days), ",
            "is_active = VALUES(is_active), ",
            "update_time = NOW()"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int upsert(Blacklist blacklist);

    /**
     * 根据用户ID查询黑名单记录
     */
    @Select("SELECT * FROM blacklist WHERE user_id = #{userId} AND is_active = 1")
    Blacklist selectByUserId(@Param("userId") Long userId);

    /**
     * 查询所有活跃的黑名单用户
     */
    @Select("SELECT * FROM blacklist WHERE is_active = 1 ORDER BY create_time DESC")
    List<Blacklist> selectAllActive();

    /**
     * 分页查询黑名单用户
     */
    @Select("SELECT * FROM blacklist WHERE is_active = 1 ORDER BY create_time DESC LIMIT #{start}, #{pageSize}")
    List<Blacklist> selectPage(@Param("start") int start, @Param("pageSize") int pageSize);

    /**
     * 统计活跃黑名单用户数量
     */
    @Select("SELECT COUNT(*) FROM blacklist WHERE is_active = 1")
    int countActive();

    /**
     * 移出黑名单（软删除）
     */
    @Update("UPDATE blacklist SET is_active = 0, update_time = NOW() WHERE user_id = #{userId} AND is_active = 1")
    int removeByUserId(@Param("userId") Long userId);

    /**
     * 更新黑名单信息
     */
    @Update("UPDATE blacklist SET overdue_books_count = #{overdueBooksCount}, total_overdue_days = #{totalOverdueDays}, " +
            "update_time = NOW() WHERE user_id = #{userId} AND is_active = 1")
    int updateByUserId(Blacklist blacklist);

    /**
     * 查询用户的逾期书籍详情（包含书籍信息，不显示已删除的书籍）
     */
    @Select("SELECT br.id, br.book_id AS bookId, br.borrow_date AS borrowDate, br.due_date AS dueDate, " +
            "b.title AS bookName, b.author AS bookAuthor, " +
            "DATEDIFF(NOW(), br.due_date) AS overdueDays " +
            "FROM borrow_record br " +
            "JOIN book b ON br.book_id = b.id " +
            "WHERE br.user_id = #{userId} AND br.status = 2 " +
            "AND (b.isDelete IS NULL OR b.isDelete = 0) " +  // 只显示未删除的书籍
            "ORDER BY br.due_date DESC")
    List<Map<String, Object>> selectUserOverdueDetails(@Param("userId") Long userId);

    /**
     * 查询所有用户的逾期统计信息，用于生成黑名单
     */
    @Select("SELECT br.user_id AS userId, u.userName AS username, u.phone, " +
            "COUNT(*) AS overdueBooksCount, " +
            "SUM(DATEDIFF(NOW(), br.due_date)) AS totalOverdueDays " +
            "FROM borrow_record br " +
            "JOIN user u ON br.user_id = u.id " +
            "JOIN book b ON br.book_id = b.id " +  // 关联书籍表
            "WHERE br.status = 2 AND u.isDelete = 0 " +
            "AND (b.isDelete IS NULL OR b.isDelete = 0) " +  // 只统计未删除的书籍
            "GROUP BY br.user_id, u.userName, u.phone " +
            "HAVING COUNT(*) >= 3 " +  // 逾期3本书及以上加入黑名单
            "ORDER BY overdueBooksCount DESC, totalOverdueDays DESC")
    List<Map<String, Object>> selectPotentialBlacklistUsers();
}
