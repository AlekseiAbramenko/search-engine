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

            Course course = session.get(Course.class, 1);
            List<Student> studentList = course.getStudents();
            //studentList.forEach(student -> System.out.println(student.getName()));

            Subscription subscription = session.get(Subscription.class, new SubscriptionKey(55,33));
            //System.out.println(subscription.getSubscriptionDate());

            Teacher teacher = session.get(Teacher.class, 1);
            //System.out.println(teacher.getName());

            Student student = session.get(Student.class, 10);
            //System.out.println(student.getName() + " " + student.getAge());

            PurchaseList purchaseList = session.get(PurchaseList.class, new PurchaseKey("Бойков Максим","Веб-разработчик c 0 до PRO"));
            System.out.println(purchaseList.getStudentName() + " " + purchaseList.getPrice()
                    + " " + purchaseList.getSubscriptionDate());

        sessionFactory.close();
    }
}
