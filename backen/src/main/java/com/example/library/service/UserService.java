package com.example.library.service;

import com.example.library.model.PageBean;
import com.example.library.model.Result;
import com.example.library.model.User;

import java.util.List;

public interface UserService {

    Result addUser(String username, String password, String checkpassword, Integer userRole, String phone);

    public Result updateUser(String old_name, String username, String password, String checkpassword);

    public User login(String username, String password);

    public User adminLogin(String username, String password);

    PageBean page(Integer page, Integer pageSize);

    public List<User> findByName(String keyword);

    public boolean deleteUserById(Long id);

    Result resetPassword(String username, String newPassword);

    // 根据 id 查询用户
    com.example.library.model.User getById(Long id);

    // 根据用户名查询用户
    com.example.library.model.User getByUsername(String username);

    // 根据手机号查询用户
    com.example.library.model.User getByPhone(String phone);
}
