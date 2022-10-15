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
            //System.out.println(course.getTeacher().getName());

//            List<Student> studentList = course.getStudents();
//            studentList.forEach(student -> System.out.println(student.getName()));

            Teacher teacher = session.get(Teacher.class, 10);


        sessionFactory.close();
    }
}
