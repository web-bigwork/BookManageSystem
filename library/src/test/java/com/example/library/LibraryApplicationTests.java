package com.example.library;

import com.example.library.mapper.UserMapper;
import com.example.library.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@SpringBootTest
class LibraryApplicationTests {

    @Autowired
    private UserMapper userMapper;

    @Test
    void contextLoads() {
        System.out.println("Hello World");
    }

    @Test
    public void testSaveUser() {
        User user = new User();
        user.setUserName("zkz");
        user.setUserPassword("123456");
        user.setUserRole(0);
        user.setIsDelete(0);
        Date now = new Date();
        user.setCreateTime(now);
        user.setUpdateTime(now);
        // 执行插入操作,插入后返回插入成功的行数，这里默认就应该是1；
        int result = userMapper.saveUser(user);
        System.out.println(result);
        // 生成的id会默认回填到user对象中；
        System.out.println(user.getId());
    }

}
