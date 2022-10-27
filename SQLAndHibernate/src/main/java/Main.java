import Keys.LinkedPurchaseListKey;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
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

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<PurchaseList> query = builder.createQuery(PurchaseList.class);
        Root<PurchaseList> root = query.from(PurchaseList.class);
        query.select(root);
        List<PurchaseList> purchaseListList = session.createQuery(query).getResultList();

        Transaction transaction = session.beginTransaction();

        LinkedPurchaseList linkedPurchaseList = new LinkedPurchaseList();

//        purchaseListList.forEach(s -> System.out.println(s.getStudent().getId() + " " + s.getCourse().getId()));



        String hql = "CREATE TABLE IF NOT EXISTS LinkedPurchaseList " +
                "(student_id INT NOT NULL, " +
                "course_id INT NOT NULL)";

//        LinkedPurchaseList linkedPurchaseList = new LinkedPurchaseList();
//        for (int i = 0; i < purchaseListList.size(); i++) {
//            linkedPurchaseList.setId();
//        }

//
//        session.saveOrUpdate();
//        transaction.commit();

        session.close();
        sessionFactory.close();
    }


}
