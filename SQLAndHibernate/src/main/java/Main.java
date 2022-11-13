import Keys.LinkedPurchaseListKey;
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
        try {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure("hibernate.cfg.xml").build();
        Metadata metadata = new MetadataSources(registry).getMetadataBuilder().build();
        SessionFactory sessionFactory = metadata.getSessionFactoryBuilder().build();

        Session session = sessionFactory.openSession();

        Transaction transaction = session.beginTransaction();

        String hql  = "From " + PurchaseList.class.getSimpleName();
        List<PurchaseList> purchase = session.createQuery(hql,PurchaseList.class).getResultList();
//        purchase.forEach(s -> System.out.println(s.getCourse().getId() + " " + s.getStudent().getId()));


        for (int i = 0; i < purchase.size(); i++) {
            LinkedPurchaseList linkedPurchaseList = new LinkedPurchaseList();
            linkedPurchaseList.setId(new LinkedPurchaseListKey
                    (purchase.get(i).getStudent().getId(), purchase.get(i).getCourse().getId()));
            session.saveOrUpdate(linkedPurchaseList);
        }

        transaction.commit();
        session.close();
        sessionFactory.close();

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }
}
