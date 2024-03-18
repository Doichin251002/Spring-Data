import entities.Employee;

public class SalaryOver50000 {
    public static void main(String[] args) {
        Utils.createEntityManager()
                .createQuery("FROM Employee WHERE salary > 50000", Employee.class)
                .getResultList()
                .forEach(e -> System.out.println(e.getFirstName()));
    }
}
