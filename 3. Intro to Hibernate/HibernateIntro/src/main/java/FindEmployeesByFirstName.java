import entities.Employee;

import java.util.Scanner;

public class FindEmployeesByFirstName {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String namePattern = scanner.nextLine();

        Utils.createEntityManager()
                .createQuery("FROM Employee WHERE firstName LIKE :pattern", Employee.class)
                .setParameter("pattern", namePattern + "%")
                .getResultList()
                .forEach(e -> System.out.println(e.jobAndSalaryFormat()));
    }
}
