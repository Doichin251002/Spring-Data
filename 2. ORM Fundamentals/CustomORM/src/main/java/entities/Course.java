package entities;

import orm.annotations.Column;
import orm.annotations.Entity;
import orm.annotations.Id;

@Entity(name = "courses")
public class Course {
    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "course_matter")
    private String courseMatter;

    @Column(name = "duration_weeks")
    private int durationWeeks;

    public Course() {}

    public Course(String name, String courseMatter, int durationWeeks) {
        this.name = name;
        this.courseMatter = courseMatter;
        this.durationWeeks = durationWeeks;
    }

    @Override
    public String toString() {
        return "Course{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", durationWeeks=" + durationWeeks +
                '}';
    }
}
