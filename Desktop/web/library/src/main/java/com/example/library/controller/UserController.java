package com.example.library.controller;

import com.example.library.mapper.UserMapper;
import com.example.library.model.PageBean;
import com.example.library.model.Result;
import com.example.library.model.User;
import com.example.library.service.UserService;
import com.example.library.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result login(@RequestBody Map<String, String> loginData){
        String username = loginData.get("username");
        String password = loginData.get("password");

        if (StringUtils.isAnyBlank(username, password)) {
            return Result.error("用户名或密码为空");
        }
        User userResult = userService.login(username, password);
        if(userResult!=null){
            Claims claims = Jwts.claims();
            claims.put("id", userResult.getId());
            claims.put("username", userResult.getUserName());
            claims.put("role", userResult.getUserRole());

            String token = JwtUtil.generateTokenWithClaims(claims);
            Result result = Result.success("用户登录成功");
            result.setData(token);
            return result;
        }else{
            return Result.error("用户登录失败");
        }
    }

    @PostMapping("/admin/login")
    public Result adminLogin(@RequestBody Map<String, String> loginData){
        String username = loginData.get("username");
        String password = loginData.get("password");

        if (StringUtils.isAnyBlank(username, password)) {
            return Result.error("用户名或密码为空");
        }
        User adminResult = userService.adminLogin(username, password);
        if(adminResult!=null){
            Claims claims = Jwts.claims();
            claims.put("id", adminResult.getId());
            claims.put("username", adminResult.getUserName());
            claims.put("role", adminResult.getUserRole());

            String token = JwtUtil.generateTokenWithClaims(claims);
            Result result = Result.success("管理员登录成功");
            result.setData(token);
            return result;
        }else{
            return Result.error("该账号没有管理员权限");
        }
    }

    @PostMapping("/register")
    public Result addUser(@RequestBody Map<String, Object> registerData) {
        String username = (String) registerData.get("username");
        String password = (String) registerData.get("password");
        String phone = (String) registerData.get("phone");
        String checkpassword = (String) registerData.get("checkpassword");
        Object userRoleObj = registerData.get("userRole");
        Integer userRole = null;
        if (userRoleObj != null) {
            if (userRoleObj instanceof Number) {
                userRole = ((Number) userRoleObj).intValue();
            } else {
                String userRoleStr = userRoleObj.toString();
                if (StringUtils.isNotBlank(userRoleStr)) {
                    try {
                        userRole = Integer.valueOf(userRoleStr);
                    } catch (NumberFormatException e) {
                        return Result.error("用户角色格式不正确");
                    }
                }
            }
        }

        if (StringUtils.isAnyBlank(username,password,checkpassword,phone)) {
            return Result.error("用户名或密码或手机号为空");
        }

        Result result = userService.addUser(username, password, checkpassword, userRole,phone);
        return result;
    }

    @GetMapping("/users")
    public Result getPage(@RequestParam(defaultValue="1")Integer page, @RequestParam(defaultValue="5")Integer pageSize){
        PageBean pageBean=userService.page(page, pageSize);
        return Result.success(pageBean);
    }

    @PostMapping("/updateUser")
    public Result updateUser(@RequestBody Map<String, String> updateData) {
        String old_username = updateData.get("old_username");
        String username = updateData.get("username");
        String password = updateData.get("password");
        String checkpassword = updateData.get("checkpassword");

        if (StringUtils.isAnyBlank(old_username, username, password, checkpassword)) {
            return Result.error("用户ID、用户名或密码为空");
        }

        Result result = userService.updateUser(old_username,username, password, checkpassword);
        return result;
    }

    @GetMapping("/deleteById")
    public Result deleteUserById(Long id) {
        boolean success = userService.deleteUserById(id);
        if (success) {
            return Result.success("用户已删除");
        }
        return Result.error("用户不存在或已被删除");
    }

    @GetMapping("/findUser")
    public Result getUser(String keyword){

        List<User> users=userService.findByName(keyword);
        return Result.success(users);
    }

    @PostMapping("/resetPassword")
    public Result resetPassword(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String newPassword = payload.get("newPassword");
        return userService.resetPassword(username, newPassword);
    }
}
