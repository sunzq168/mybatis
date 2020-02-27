package com.sun.mapper;

import com.sun.model.Student;

public interface StudentMapper {
    Student selectById(long id);
}
