package com.example.library.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface VisitMapper {

    @Select("SELECT `count` FROM visit_count WHERE id = 1")
    Integer getCount();

    @Insert("INSERT INTO visit_count(id, `count`) VALUES(1,1) ON DUPLICATE KEY UPDATE `count` = `count` + 1")
    int incrementVisit();
}


