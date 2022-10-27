import Keys.LinkedPurchaseListKey;
import jakarta.persistence.*;

@Entity
@Table(name = "linkedpurchaselist")
public class LinkedPurchaseList {

    @EmbeddedId
    private LinkedPurchaseListKey id;

//    @Column(name = "student_id", nullable = true, unique = false)
//    private int student_id;
//
//    @Column(name = "course_id", nullable = true, unique = false)
//    private int course_id;

    public LinkedPurchaseList() {
    }

//    public LinkedPurchaseList(LinkedPurchaseListKey id, int student_id, int course_id) {
//        this.id = id;
//        this.student_id = student_id;
//        this.course_id = course_id;
//    }
//
//    public int getStudent_id() {
//        return student_id;
//    }
//
//    public void setStudent_id(int student_id) {
//        this.student_id = student_id;
//    }
//
//    public int getCourse_id() {
//        return course_id;
//    }
//
//    public void setCourse_id(int course_id) {
//        this.course_id = course_id;
//    }

    public LinkedPurchaseListKey getId() {
        return id;
    }

    public void setId(LinkedPurchaseListKey id) {
        this.id = id;
    }
}
