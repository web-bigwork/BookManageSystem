package com.example.library.mapper;

import com.example.library.model.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM user WHERE id = #{id}")
    User selectById(Long id);

    @Select("select * from user")
    public List<User> list();

    /**
     * 插入用户
     */
        @Insert("insert into user(userName, userPassword, phone, isDelete, userRole, createTime, updateTime) " +
            "values(#{userName}, #{userPassword}, #{phone} ,#{isDelete}, #{userRole}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int saveUser(User user);

    @Update("UPDATE user SET userName = #{username}, userPassword = #{password} WHERE username=#{old_username}")
    int update(@Param("username") String username,@Param("password") String password,@Param("old_username") String old_username);

    @Select("SELECT COUNT(*) FROM user WHERE userName = #{username} AND isDelete = 0")
    public int existsByName(@Param("username") String username);

    @Select("SELECT COUNT(*) FROM user WHERE isDelete=0")
    public int count();

    @Select("SELECT * FROM user WHERE isDelete=0 limit #{start},#{pageSize}")
    public List<User> page(@Param("start") Integer start, @Param("pageSize") Integer pageSize);

    @Select("select * from user where userName=#{username} AND userPassword=#{password} AND isDelete=0")
    public User getByNameAndPassword(@Param("username") String username, @Param("password") String password);

    @Select("SELECT * FROM user WHERE username LIKE CONCAT('%', #{keyword}, '%') AND isDelete=0")
    List<User> findByName(String keyword);

    @Update("UPDATE user SET isDelete = 1, updateTime = NOW() WHERE id = #{id} AND isDelete = 0")
    public int deleteUserById(@Param("id") Long id);

    @Select("SELECT * FROM user WHERE username = #{username}")
    User selectByName(String username);

    @Select("SELECT * FROM user WHERE phone = #{phone} AND isDelete = 0")
    User selectByPhone(@Param("phone") String phone);

    @Update("UPDATE user SET userPassword = #{password}, updateTime = NOW() WHERE username = #{username} AND isDelete = 0")
    int updatePassword(@Param("username") String username, @Param("password") String password);

    @Update("UPDATE user SET userName = #{userName}, phone = #{phone}, userRole = #{userRole}, updateTime = NOW() WHERE id = #{id} AND isDelete = 0")
    int updateById(com.example.library.model.User user);

}
