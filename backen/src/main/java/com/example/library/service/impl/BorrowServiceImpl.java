package com.example.library.service.impl;

import com.example.library.mapper.BlacklistMapper;
import com.example.library.mapper.BookMapper;
import com.example.library.mapper.BorrowRecordMapper;
import com.example.library.mapper.UserMapper;
import com.example.library.model.Blacklist;
import com.example.library.model.Book;
import com.example.library.model.BorrowRecord;
import com.example.library.model.PageBean;
import com.example.library.model.Result;
import com.example.library.service.BorrowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class BorrowServiceImpl implements BorrowService {
    @Autowired
    private BorrowRecordMapper borrowRecordMapper;
    @Autowired
    private BookMapper bookMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private BlacklistMapper blacklistMapper;

    @Override
    public Result borrowBook(Long userId, Long bookId) {
        // 校验用户是否在黑名单中
        Blacklist blacklist = blacklistMapper.selectByUserId(userId);
        if (blacklist != null && blacklist.getIsActive() != null && blacklist.getIsActive() == 1) {
            return Result.error("您已被列入黑名单，无法借书");
        }

        // 校验用户借书数量未超限
        int currentBorrowCount = borrowRecordMapper.findBorrowingByUser(userId).size();
        Integer maxBorrowNumObj = userMapper.selectById(userId).getMaxBorrowNum();
        int maxBorrowNum = (maxBorrowNumObj != null) ? maxBorrowNumObj : 5;
        if (currentBorrowCount >= maxBorrowNum) {
            return Result.error("已达最大借书数量");
        }
        Book book = bookMapper.selectById(bookId);
        if (book == null || book.getAvailable_qty() == null || book.getAvailable_qty() <= 0) {
            return Result.error("图书可借阅数为0");
        }
        // 插入借阅记录
        BorrowRecord record = new BorrowRecord();
        record.setUserId(userId);
        record.setBookId(bookId);
        record.setBorrow_date(new Date());
        record.setStatus(0);
        record.setDue_date(LocalDateTime.now().plusDays(1)); // 1 天后应还
        int inserted = borrowRecordMapper.insert(record);
        if (inserted > 0) {
            // 更新图书可借阅数
            book.setAvailable_qty(book.getAvailable_qty() - 1);
            book.setUpdateTime(new Date());
            bookMapper.updateBook(book);
            return Result.success("借书成功");
        }
        return Result.error("借书失败");
    }

    @Override
    public Result returnBook(Long borrowId) {
        int updated = borrowRecordMapper.returnBook(borrowId);
        boolean bookUpdated = false;
        if (updated > 0) {
            // 查询借阅记录，获取bookId
            BorrowRecord record = borrowRecordMapper.selectById(borrowId);
            System.out.println("进入更新图书分支，record=" + record);
            if (record != null && record.getBookId() != null) {
                System.out.println("准备查询图书，bookId=" + record.getBookId());
                Book book = bookMapper.selectById(record.getBookId());
                System.out.println("查到的book=" + book);
                if (book != null && book.getAvailable_qty() != null) {
                    book.setAvailable_qty(book.getAvailable_qty() + 1);
                    book.setUpdateTime(new Date());
                    System.out.println("准备更新图书，book=" + book);
                    int rows = bookMapper.updateBook(book);
                    System.out.println("更新完成，影响行数=" + rows);
                    bookUpdated = rows > 0;
                }
            }
            if (bookUpdated) {
                return Result.success("还书成功，可借阅数已更新");
            } else {
                return Result.success("还书成功，但可借阅数未更新");
            }
        }
        return Result.error("还书失败");
    }

    @Override
    public Result returnedPage(Long userId, int page, int pageSize) {
        int total = borrowRecordMapper.countReturnedByUser(userId);
        if (total == 0) {
            return Result.success(new PageBean<>(0, List.of()));
        }
        int start = (page - 1) * pageSize;
        List<Map<String, Object>> rows = borrowRecordMapper.selectReturnedByUser(userId, start, pageSize);
        return Result.success(new PageBean<>(total, rows));
    }

    @Override
    public Result allReturnedPage(int page, int pageSize) {
        int total = borrowRecordMapper.countAllReturned();
        int start = (page - 1) * pageSize;
        List<Map<String, Object>> rows =
                total == 0 ? List.of() : borrowRecordMapper.selectAllReturned(start, pageSize);
        return Result.success(new PageBean<>(total, rows));
    }

    @Override
    public Result renewByBorrowId(Long borrowId) {
        // 1. 必须有在借记录
        BorrowRecord record = borrowRecordMapper.selectBorrowingById(borrowId);
        if (record == null) {
            return Result.error("未找到该图书的在借记录");
        }
        // 2. 简单续借：+3 天（可改成配置）
        LocalDateTime newDue = record.getDue_date().plusDays(3);
        int updated = borrowRecordMapper.renewById(record.getId(), newDue);
        return updated > 0
                ? Result.success("续借成功，新到期日：" + newDue.toLocalDate())
                : Result.error("续借失败");
    }

    @Override
    public Result getBorrowingBooks(Long userId) {
        if (userId == null) {
            return Result.error("用户ID不能为空");
        }
        List<BorrowRecord> records = borrowRecordMapper.findBorrowingByUser(userId);
        if (records == null || records.isEmpty()) {
            return Result.success(List.of());
        }
        // 将借阅记录和图书信息组合返回
        List<java.util.Map<String, Object>> rows = new java.util.ArrayList<>();
        for (BorrowRecord r : records) {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", r.getId());
            m.put("userId", r.getUserId());
            m.put("bookId", r.getBookId());
            m.put("borrowDate", r.getBorrow_date());
            m.put("dueDate", r.getDue_date());
            m.put("status", r.getStatus());
            Book book = bookMapper.selectById(r.getBookId());
            if (book != null) {
                m.put("bookTitle", book.getTitle());
                m.put("bookAuthor", book.getAuthor());
                m.put("available_qty", book.getAvailable_qty());
            }
            rows.add(m);
        }
        return Result.success(rows);
    }

    @Override
    public Result allRecordsPage(int page, int pageSize) {
        int total = borrowRecordMapper.countAllRecords();
        if (total == 0) {
            return Result.success(new PageBean<>(0, List.of()));
        }
        int start = (page - 1) * pageSize;
        List<Map<String, Object>> rows = borrowRecordMapper.selectAllRecords(start, pageSize);
        return Result.success(new PageBean<>(total, rows));
    }

    @Override
    public Result editRecord(com.example.library.model.BorrowRecord record) {
        if (record == null || record.getId() == null) {
            return Result.error("借阅记录ID不能为空");
        }
        System.out.println("editRecord: 开始更新记录 ID=" + record.getId() + ", status=" + record.getStatus() + ", dueDate=" + record.getDue_date());

        // 查询现有记录
        BorrowRecord existing = borrowRecordMapper.selectById(record.getId());
        if (existing == null) {
            System.out.println("editRecord: 记录不存在 ID=" + record.getId());
            return Result.error("借阅记录不存在");
        }
        System.out.println("editRecord: 现有记录状态: ID=" + existing.getId() + ", status=" + existing.getStatus() +
                          ", returnTime=" + existing.getReturnTime() + ", isDelete=" + existing.getIsDelete());

        Integer oldStatus = existing.getStatus();
        Integer newStatus = record.getStatus();

        // 如果是从已还改为在借，需要清除归还时间并设置新的到期时间
        if (oldStatus != null && oldStatus == 1 && newStatus != null && newStatus == 0) {
            // 清除归还时间，因为这本书现在又变成在借状态
            record.setReturnTime(null);
            if (record.getDue_date() == null) {
                // 设置为当前时间 + 1天
                record.setDue_date(LocalDateTime.now().plusDays(1));
                System.out.println("editRecord: 撤销还书，清除归还时间，自动设置到期时间为: " + record.getDue_date());
            }
        }

        // 执行更新
        int rows = borrowRecordMapper.updateById(record);
        System.out.println("editRecord: updateById 返回行数: " + rows + ", record=" + record);
        if (rows == 0) {
            System.out.println("editRecord: 更新失败，检查记录是否存在且未被删除");
            // 检查记录状态
            BorrowRecord checkRecord = borrowRecordMapper.selectById(record.getId());
            if (checkRecord == null) {
                System.out.println("editRecord: 记录不存在");
                return Result.error("借阅记录不存在");
            }
            if (checkRecord.getIsDelete() != null && checkRecord.getIsDelete() == 1) {
                System.out.println("editRecord: 记录已被删除");
                return Result.error("借阅记录已被删除");
            }
            return Result.error("更新借阅记录失败");
        }

        // 如果状态发生变化，需要同步更新图书可借数量
        try {
            if (oldStatus != null && newStatus != null && !oldStatus.equals(newStatus)) {
                Book book = bookMapper.selectById(existing.getBookId());
                if (book != null && book.getAvailable_qty() != null) {
                    if (oldStatus == 0 && newStatus == 1) {
                        // 还书：增加可借数量
                        book.setAvailable_qty(book.getAvailable_qty() + 1);
                        book.setUpdateTime(new java.util.Date());
                        bookMapper.updateBook(book);
                        System.out.println("editRecord: 还书成功，图书 " + existing.getBookId() + " 可借数量 +1 = " + book.getAvailable_qty());
                    } else if (oldStatus == 1 && newStatus == 0) {
                        // 变为在借：减少可借数量
                        if (book.getAvailable_qty() > 0) {
                            book.setAvailable_qty(book.getAvailable_qty() - 1);
                            book.setUpdateTime(new java.util.Date());
                            bookMapper.updateBook(book);
                            System.out.println("editRecord: 撤销还书成功，图书 " + existing.getBookId() + " 可借数量 -1 = " + book.getAvailable_qty());
                        } else {
                            System.out.println("editRecord: 撤销还书失败，图书 " + existing.getBookId() + " 可借数量为 " + book.getAvailable_qty() + "，无法减少");
                            // 可以选择抛出异常或返回错误，但为了兼容性，这里先允许状态变更但不减少数量
                        }
                    }
                } else {
                    System.out.println("editRecord: 未找到图书或图书可借数量信息，bookId=" + existing.getBookId() + ", book=" + book);
                }
            }
        } catch (Exception e) {
            System.err.println("editRecord: 更新图书数量时出错: " + e.getMessage());
            e.printStackTrace();
        }

        return Result.success("借阅记录更新成功");
    }

    @Override
    public Result deleteRecord(Long id) {
        if (id == null) return Result.error("ID 不能为空");
        BorrowRecord existing = borrowRecordMapper.selectById(id);
        if (existing == null) return Result.error("借阅记录不存在");

        // 如果是未还状态，归档时要把图书可借数量加回
        try {
            if (existing.getStatus() != null && existing.getStatus() == 0) {
                Book book = bookMapper.selectById(existing.getBookId());
                if (book != null && book.getAvailable_qty() != null) {
                    book.setAvailable_qty(book.getAvailable_qty() + 1);
                    book.setUpdateTime(new java.util.Date());
                    bookMapper.updateBook(book);
                }
            }
        } catch (Exception ignored) {
        }

        int rows = borrowRecordMapper.deleteById(id);
        if (rows == 0) return Result.error("删除借阅记录失败");
        return Result.success("借阅记录已删除");
    }

    @Override
    public Result userAllRecordsPage(Long userId, int page, int pageSize) {
        if (userId == null) {
            return Result.error("用户ID不能为空");
        }
        int total = borrowRecordMapper.countAllByUser(userId);
        if (total == 0) {
            return Result.success(new PageBean<>(0, List.of()));
        }
        int start = (page - 1) * pageSize;
        List<Map<String, Object>> rows = borrowRecordMapper.selectAllByUser(userId, start, pageSize);
        return Result.success(new PageBean<>(total, rows));
    }

    @Override
    public Result getOverdueBooks(Long userId) {
        if (userId == null) {
            return Result.error("用户ID不能为空");
        }
        List<BorrowRecord> records = borrowRecordMapper.findOverdueByUser(userId);
        if (records == null || records.isEmpty()) {
            return Result.success(List.of());
        }
        // 将借阅记录和图书信息组合返回
        List<java.util.Map<String, Object>> rows = new java.util.ArrayList<>();
        for (BorrowRecord r : records) {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", r.getId());
            m.put("userId", r.getUserId());
            m.put("bookId", r.getBookId());
            m.put("borrowDate", r.getBorrow_date());
            m.put("dueDate", r.getDue_date());
            m.put("status", r.getStatus());
            Book book = bookMapper.selectById(r.getBookId());
            if (book != null) {
                m.put("bookTitle", book.getTitle());
                m.put("bookAuthor", book.getAuthor());
                m.put("available_qty", book.getAvailable_qty());
            }
            rows.add(m);
        }
        return Result.success(rows);
    }

    /**
     * 定时任务：每5分钟检查一次逾期借阅记录
     * 将逾期的记录（status=0 且 due_date < now）标记为逾期状态（status=2）
     */
    @Scheduled(fixedRate = 60000) // 5分钟 = 300秒 = 300000毫秒
    public void checkOverdueRecords() {
        try {
            System.out.println("开始检查逾期借阅记录...");

            // 查询所有在借记录
            List<BorrowRecord> borrowingRecords = borrowRecordMapper.findBorrowingByUser(null); // 传null获取所有用户的在借记录

            LocalDateTime now = LocalDateTime.now();
            int overdueCount = 0;

            for (BorrowRecord record : borrowingRecords) {
                if (record.getDue_date() != null && record.getDue_date().isBefore(now)) {
                    // 标记为逾期状态
                    record.setStatus(2); // 2表示逾期
                    int rows = borrowRecordMapper.updateById(record);
                    if (rows > 0) {
                        overdueCount++;
                        System.out.println("记录ID " + record.getId() + " 已标记为逾期");
                    }
                }
            }

            if (overdueCount > 0) {
                System.out.println("本次检查发现并标记了 " + overdueCount + " 条逾期记录");
            } else {
                System.out.println("本次检查未发现新的逾期记录");
            }

        } catch (Exception e) {
            System.err.println("检查逾期记录时发生错误: " + e.getMessage());
            e.printStackTrace();
        }

        // 同步黑名单：将逾期3本书以上的用户加入黑名单
        try {
            syncBlacklist();
        } catch (Exception e) {
            System.err.println("同步黑名单时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

//    @Override
//    public Result editRecord(BorrowRecord record) {
//        if (record == null || record.getId() == null) {
//            return Result.error("借阅记录ID不能为空");
//        }
//        BorrowRecord exist = borrowRecordMapper.selectById(record.getId());
//        if (exist == null) {
//            return Result.error("借阅记录不存在");
//        }
//        int rows = borrowRecordMapper.updateById(record);
//        return rows > 0 ? Result.success("更新借阅记录成功") : Result.error("更新借阅记录失败");
//    }

//    @Override
//    public Result deleteRecord(Long id) {
//        if (id == null) return Result.error("借阅记录ID不能为空");
//        BorrowRecord exist = borrowRecordMapper.selectById(id);
//        if (exist == null) {
//            return Result.error("借阅记录不存在");
//        }
//        int rows = borrowRecordMapper.softDeleteById(id);
//        return rows > 0 ? Result.success("删除借阅记录成功") : Result.error("删除借阅记录失败");
//    }

    @Override
    public Result getBlacklistUsers(int page, int pageSize) {
        int total = blacklistMapper.countActive();
        if (total == 0) {
            return Result.success(new PageBean<>(0, List.of()));
        }
        int start = (page - 1) * pageSize;
        List<Blacklist> blacklists = blacklistMapper.selectPage(start, pageSize);

        // 为每个黑名单用户添加逾期详情
        List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        for (Blacklist blacklist : blacklists) {
            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("userId", blacklist.getUserId());
            item.put("overdueBooksCount", blacklist.getOverdueBooksCount());
            item.put("totalOverdueDays", blacklist.getTotalOverdueDays());
            item.put("createTime", blacklist.getCreateTime());

            // 从用户表获取用户信息
            try {
                com.example.library.model.User user = userMapper.selectById(blacklist.getUserId());
                if (user != null) {
                    item.put("username", user.getUserName());
                    item.put("phone", user.getPhone());
                }
            } catch (Exception e) {
                System.err.println("获取用户信息失败: " + e.getMessage());
            }

            result.add(item);
        }

        return Result.success(new PageBean<>(total, result));
    }

    @Override
    public Result getUserOverdueDetails(Long userId) {
        if (userId == null) {
            return Result.error("用户ID不能为空");
        }

        List<Map<String, Object>> details = blacklistMapper.selectUserOverdueDetails(userId);
        System.out.println("getUserOverdueDetails: 用户 " + userId + " 的逾期详情数量: " + (details != null ? details.size() : 0));
        if (details == null || details.isEmpty()) {
            return Result.success(List.of());
        }

        // 格式化返回数据
        List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        for (Map<String, Object> detail : details) {
            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("bookName", detail.get("bookName"));
            item.put("lendTime", formatDateTime(detail.get("borrowDate")));
            item.put("dueTime", formatDateTime(detail.get("dueDate")));
            item.put("overdueDays", detail.get("overdueDays"));
            result.add(item);
        }

        return Result.success(result);
    }

    @Override
    public Result removeFromBlacklist(Long userId) {
        if (userId == null) {
            return Result.error("用户ID不能为空");
        }

        int rows = blacklistMapper.removeByUserId(userId);
        if (rows > 0) {
            return Result.success("已成功移出黑名单");
        } else {
            return Result.error("用户不在黑名单中或操作失败");
        }
    }

    /**
     * 同步黑名单：将逾期3本书以上的用户自动加入黑名单
     */
    private void syncBlacklist() {
        try {
            System.out.println("开始同步黑名单...");

            // 获取所有潜在的黑名单用户（逾期3本书以上）
            List<Map<String, Object>> potentialUsers = blacklistMapper.selectPotentialBlacklistUsers();
            System.out.println("潜在黑名单用户数量: " + (potentialUsers != null ? potentialUsers.size() : 0));
            if (potentialUsers != null) {
                for (Map<String, Object> user : potentialUsers) {
                    System.out.println("潜在黑名单用户: " + user.get("userId") + ", 逾期书数: " + user.get("overdueBooksCount"));
                }
            }

            int addedCount = 0;
            int updatedCount = 0;
            int removedCount = 0;

            // 获取所有活跃的黑名单用户
            List<Blacklist> activeBlacklists = blacklistMapper.selectAllActive();
            System.out.println("当前活跃黑名单用户数量: " + activeBlacklists.size());

            // 创建当前应该在黑名单中的用户ID集合
            java.util.Set<Long> shouldBeBlacklisted = new java.util.HashSet<>();
            for (Map<String, Object> user : potentialUsers) {
                Long userId = safeToLong(user.get("userId"));
                if (userId != null) {
                    shouldBeBlacklisted.add(userId);
                }
            }

            // 处理潜在黑名单用户（逾期3本以上）
            for (Map<String, Object> user : potentialUsers) {
                Long userId = safeToLong(user.get("userId"));
                Integer overdueBooksCount = safeToInteger(user.get("overdueBooksCount"));
                Integer totalOverdueDays = safeToInteger(user.get("totalOverdueDays"));

                if (userId == null || overdueBooksCount == null || totalOverdueDays == null) {
                    System.err.println("跳过无效的用户数据: " + user);
                    continue;
                }

                // 检查用户是否已在黑名单中
                Blacklist existing = blacklistMapper.selectByUserId(userId);
                if (existing == null) {
                    // 新加入黑名单
                    Blacklist blacklist = new Blacklist();
                    blacklist.setUserId(userId);
                    blacklist.setReason("逾期" + overdueBooksCount + "本书，累计逾期" + totalOverdueDays + "天");
                    blacklist.setOverdueBooksCount(overdueBooksCount);
                    blacklist.setTotalOverdueDays(totalOverdueDays);
                    blacklist.setIsActive(1);

                    int rows = blacklistMapper.upsert(blacklist);
                    if (rows > 0) {
                        addedCount++;
                        System.out.println("用户 " + userId + " 已加入黑名单（逾期" + overdueBooksCount + "本）");
                    }
                } else {
                    // 更新黑名单信息
                    existing.setOverdueBooksCount(overdueBooksCount);
                    existing.setTotalOverdueDays(totalOverdueDays);
                    existing.setReason("逾期" + overdueBooksCount + "本书，累计逾期" + totalOverdueDays + "天");

                    int rows = blacklistMapper.updateByUserId(existing);
                    if (rows > 0) {
                        updatedCount++;
                    }
                }
            }

            // 移除不再符合条件的用户（逾期书数量 < 3）
            for (Blacklist blacklist : activeBlacklists) {
                Long userId = blacklist.getUserId();
                if (!shouldBeBlacklisted.contains(userId)) {
                    // 查询该用户的当前逾期书数量
                    List<Map<String, Object>> userOverdueBooks = blacklistMapper.selectUserOverdueDetails(userId);
                    int currentOverdueCount = userOverdueBooks != null ? userOverdueBooks.size() : 0;

                    if (currentOverdueCount < 3) {
                        // 逾期书数量不足3本，移出黑名单
                        int rows = blacklistMapper.removeByUserId(userId);
                        if (rows > 0) {
                            removedCount++;
                            System.out.println("用户 " + userId + " 已从黑名单移除（当前逾期" + currentOverdueCount + "本）");
                        }
                    }
                }
            }

            if (addedCount > 0 || updatedCount > 0) {
                System.out.println("黑名单同步完成：新增 " + addedCount + " 个用户，更新 " + updatedCount + " 个用户");
            } else {
                System.out.println("黑名单同步完成：无变化");
            }

        } catch (Exception e) {
            System.err.println("同步黑名单时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 安全地将对象转换为Long类型
     */
    private Long safeToLong(Object obj) {
        if (obj == null) return null;
        try {
            if (obj instanceof Number) {
                return ((Number) obj).longValue();
            } else if (obj instanceof String) {
                return Long.valueOf((String) obj);
            }
        } catch (Exception e) {
            System.err.println("转换Long失败: " + obj + ", 错误: " + e.getMessage());
        }
        return null;
    }

    /**
     * 安全地将对象转换为Integer类型
     */
    private Integer safeToInteger(Object obj) {
        if (obj == null) return null;
        try {
            if (obj instanceof Number) {
                return ((Number) obj).intValue();
            } else if (obj instanceof String) {
                return Integer.valueOf((String) obj);
            }
        } catch (Exception e) {
            System.err.println("转换Integer失败: " + obj + ", 错误: " + e.getMessage());
        }
        return null;
    }

    /**
     * 格式化日期时间为字符串
     */
    private String formatDateTime(Object dateTime) {
        if (dateTime == null) return "";
        String str = String.valueOf(dateTime);
        // 如果包含T，替换为空格并截取前19位
        return str.replace('T', ' ').substring(0, Math.min(19, str.length()));
    }
}
