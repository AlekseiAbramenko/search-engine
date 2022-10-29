import Keys.LinkedPurchaseListKey;
import jakarta.persistence.*;

@Entity
@Table(name = "linkedpurchaselist")
public class LinkedPurchaseList {

    @EmbeddedId
    private LinkedPurchaseListKey id;

    public LinkedPurchaseList() {
    }

    public LinkedPurchaseListKey getId() {
        return id;
    }

    public void setId(LinkedPurchaseListKey id) {
        this.id = id;
    }
}
