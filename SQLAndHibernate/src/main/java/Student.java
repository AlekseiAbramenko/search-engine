import jakarta.persistence.*;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "Students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private int age;

    @Column(name = "registration_date")
    private Date registrationDate;

    @ManyToMany(cascade = CascadeType.ALL, fetch=FetchType.LAZY)
    @JoinTable(name = "Subscriptions",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id"))
    private List<Course> courses;

    @OneToMany(mappedBy = "student", fetch=FetchType.LAZY)
    private List<Subscription> subscriptions;

    @OneToMany(cascade = CascadeType.ALL, fetch=FetchType.LAZY)
    @JoinTable(name = "Purchaselist",
            joinColumns = @JoinColumn(name = "student_name"),
            inverseJoinColumns = @JoinColumn(name = "course_name"))
    private List<Course> courseNames;

    public Student() {
    }

    public Student(Integer id, String name, int age, Date registrationDate) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.registrationDate = registrationDate;
    }

    public List<Course> getCourseNames() {
        return courseNames;
    }

    public void setCourseNames(List<Course> courseNames) {
        this.courseNames = courseNames;
    }

    public List<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public List<Course> getCourses() {
        return courses;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }
}
