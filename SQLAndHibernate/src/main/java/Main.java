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

        Course course = session.get(Course.class, 10);
//        System.out.println(course.getTeacher().getName());

//        List<Student> studentList = course.getStudents();
//        System.out.println("На курс " + course.getName() + " зарегистрированы студенты: ");
//        studentList.forEach(student -> System.out.println(student.getName()
//                + ". Дата регистрации: " + student.getRegistrationDate()));
//
//        System.out.println("");
//
        Student student = session.get(Student.class, 1);
//        List<Course> courses = student.getCourses();
//        System.out.println("Студент " + student.getName() + " зарегистрирован на курсы:");
//        courses.forEach(course1 -> System.out.println(course1.getName()
//                + ". Преподаватель: " + course1.getTeacher().getName()));

        Subscription subscription = session.get(Subscription.class, new SubscriptionKey(1, 10));
        System.out.println(subscription.getStudent().getName() + " " + subscription.getCourse().getName());

        List<Subscription> subscriptions = student.getSubscriptions();
        subscriptions.forEach(System.out::println);

        sessionFactory.close();
    }
}
