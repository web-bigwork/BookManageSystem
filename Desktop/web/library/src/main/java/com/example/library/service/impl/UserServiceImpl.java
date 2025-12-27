package com.example.library.service.impl;

import com.example.library.mapper.UserMapper;
import com.example.library.model.PageBean;
import com.example.library.model.Result;
import com.example.library.model.User;
import com.example.library.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    private static final String SALT = "com.quiz";

    @Override
    public Result updateUser(String old_name, String username, String password, String checkpassword){
        // 1. 校验两次密码
        if (!Objects.equals(password, checkpassword)) {
            return Result.error("两次输入的密码不一致");
        }


        // 4. 保存
        int rows = userMapper.update(username, password, old_name);
        if (rows == 0) {
            return Result.error("更新失败，请稍后重试");
        }

        return Result.success();
    }

    @Override
    public User login(String username, String password){
        //对密码进行加密;
        final String SALT = "com.quiz";
        String encrptedPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());

        return userMapper.getByNameAndPassword(username,encrptedPassword);
    }

    @Override
    public User adminLogin(String username, String password) {
        User user = login(username, password);
        if (user == null) {
            return null;
        }
        Integer role = user.getUserRole();
        if (role != null && role == 1) {
            return user;
        }
        return null;
    }

    @Override
    public PageBean page(Integer page, Integer pageSize){
        //获取总的记录数；
        Integer total=userMapper.count();

        //获取分页查询结果列表；
        Integer start = (page-1)*pageSize;
        List<User> userList=userMapper.page(start, pageSize);

        //封装PageBean对象；
        PageBean pageBean = new PageBean();
        pageBean.setTotal(total);
        pageBean.setRows(userList);

        return pageBean;
    }

    public Result addUser(String username, String password, String checkpassword, Integer userRole,String phone){
        //此处的逻辑代码；
        if (StringUtils.isAnyBlank(username, password, checkpassword)) {
            return Result.error("用户名或密码为空");
        }

        if (!password.equals(checkpassword)) {
            return Result.error("两次输入的密码不一致");
        }

        String regex = "^[a-zA-Z0-9]+$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(username);
        if (!matcher.matches()) {
            return Result.error("用户名包含特殊字符");
        }

        //默认普通用户
        int finalRole = (userRole == null) ? 0 : userRole;
        if (finalRole != 0 && finalRole != 1) {
            return Result.error("用户角色不合法");
        }

        //查询数据库，确定是否已经存在用户名;
        int userExist = userMapper.existsByName(username);
        if (userExist > 0) {
            return Result.error("用户名已经存在");
        }

        //对密码进行加密;
        String encrptedPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());

        User user = new User();
        user.setUserName(username);
        user.setUserPassword(encrptedPassword);
        user.setPhone(phone);
        /**
         * 注册默认是普通用户，所以userRole设置为0；
         */
        user.setUserRole(finalRole);
        user.setIsDelete(0);

        Date now = new Date();
        user.setCreateTime(now);
        user.setUpdateTime(now);

        //4.插入到数据库；
        int result = userMapper.saveUser(user);

        if (result > 0)
            return Result.success("新增用户成功");
        else
            return Result.error("注册用户失败");
    }

    @Override
    public boolean deleteUserById(Long id) {
        int result = userMapper.deleteUserById(id);
        return result > 0;
    }

    @Override
    public List<User> findByName(String keyword){
        List<User> userList=userMapper.findByName(keyword);
        for(User user:userList){
            user.setUserPassword("*******");
        }
        return userList;
    }

    @Override
    public Result resetPassword(String username, String newPassword) {
        if (StringUtils.isBlank(username)) {
            return Result.error("用户名不能为空");
        }
        User user = userMapper.selectByName(username);
        if (user == null || (user.getIsDelete() != null && user.getIsDelete() == 1)) {
            return Result.error("用户不存在");
        }
        Integer role = user.getUserRole();
        String finalPassword;
        if (role == null || role == 0) {
            finalPassword = "123456";
        } else {
            if (StringUtils.isBlank(newPassword)) {
                return Result.error("管理员重置密码需要输入新密码");
            }
            finalPassword = newPassword;
        }
        String encrypted = DigestUtils.md5DigestAsHex((SALT + finalPassword).getBytes());
        int rows = userMapper.updatePassword(username, encrypted);
        if (rows == 0) {
            return Result.error("重置密码失败，请稍后重试");
        }
        return Result.success("密码重置成功");
    }
}
