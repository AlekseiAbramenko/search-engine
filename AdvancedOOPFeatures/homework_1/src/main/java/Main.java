import java.util.List;
import java.util.Objects;

public class Main {

    public static final String STAFF_TXT = "data/staff.txt";

    public static void main(String[] args) {
        List<Employee> staff = Employee.loadStaffFromFile(STAFF_TXT);

        sortBySalaryAndAlphabet(staff);

    }

    public static void sortBySalaryAndAlphabet(List<Employee> staff) {
            staff.sort((o1, o2) -> {
                if (Objects.equals(o1.getSalary(), o2.getSalary())) {
                    return o1.getName().compareTo(o2.getName());
                }
                return o1.getSalary().compareTo(o2.getSalary());
            });

            for (Employee employee : staff) {
                System.out.println(employee);
            }
        }
        //TODO Метод должен отсортировать сотрудников по заработной плате и алфавиту.
    }