import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure("hibernate.cfg.xml").build();

        Metadata metadata = new MetadataSources(registry).getMetadataBuilder().build();
        SessionFactory sessionFactory = metadata.getSessionFactoryBuilder().build();

        Session session = sessionFactory.openSession();

        Course course = session.get(Course.class, 3);
        System.out.println(course.getTeacher().getName());

        List<Student> studentList = course.getStudents();
        System.out.println("На курс " + course.getName() + " зарегистрированы студенты: ");
        studentList.forEach(student -> System.out.println(student.getName()));

        System.out.println();

        Student student = session.get(Student.class, 1);
        List<Course> courses = student.getCourses();
        System.out.println("Студент " + student.getName() + " зарегистрирован на курсы:");
        courses.forEach(course1 -> System.out.println(course1.getName()
                + ". Преподаватель: " + course1.getTeacher().getName()));

        System.out.println();

        Subscription subscription = session.get(Subscription.class, new SubscriptionKey(1, 10));
        System.out.println(subscription.getStudent().getName() + " " + subscription.getCourse().getName());

        System.out.println();

        List<Subscription> subscriptions = student.getSubscriptions();
        System.out.println("Студент: " + student.getName() + " подписан на курсы:");
        subscriptions.forEach( subscription1 -> System.out.println(subscription1.getCourse().getName()));
        System.out.println();

        List<Subscription> subscriptionList = course.getSubscriptions();
        System.out.println("На курс " + course.getName() + " Зарегистрированы студенты:");
        subscriptionList.forEach(s -> System.out.println(s.getStudent().getName()));

        System.out.println();

        Teacher teacher = session.get(Teacher.class, 10);
        List<Course> courseList = teacher.getCourses();
        System.out.println("Преподаватель " + teacher.getName() + " ведет курсы:");
        courseList.forEach(c -> System.out.println(c.getName()));

        sessionFactory.close();
    }
}
