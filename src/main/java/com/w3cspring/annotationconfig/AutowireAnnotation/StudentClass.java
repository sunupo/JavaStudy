package com.w3cspring.annotationconfig.AutowireAnnotation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class StudentClass {
    Student student;
    Teacher teacher;

    @Autowired
    public void setStudent(@Qualifier(value = "student")Student student, @Qualifier(value = "teacherConfig")Teacher teacher) {
        this.student = student;
        this.teacher = teacher;
    }

    public Student getStudent() {
        return student;
    }

    public Teacher getTeacher() {
        return teacher;
    }
}
