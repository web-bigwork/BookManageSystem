package com.example.library.model;

import java.util.Date;

public class Book {
    /** 图书ID */
    private Long id;
    /** 书名 */
    private String title;
    /** 作者 */
    private String author;
    /** 是否删除 */
    private Integer isDelete;
    /** 创建时间 */
    private Date createTime;
    /** 更新时间 */
    private Date updateTime;
    /** 可借阅数 */
    private Integer availableQty;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getAvailable_qty() {
        return availableQty;
    }

    public void setAvailable_qty(Integer availableQty) {
        this.availableQty = availableQty;
    }

    @Override
    public String toString() {
        return "Book{" +
                "bookId=" + id +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", isDelete=" + isDelete +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", available_qty=" + availableQty +
                '}';
    }
}
