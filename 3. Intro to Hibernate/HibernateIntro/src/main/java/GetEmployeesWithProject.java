import entities.Employee;

import javax.persistence.EntityManager;
import java.util.Scanner;

public class GetEmployeesWithProject {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int employeeId = Integer.parseInt(scanner.nextLine());

        EntityManager entityManager = Utils.createEntityManager();
        Employee employee = entityManager
                .createQuery("FROM Employee WHERE id = :inputId", Employee.class)
                .setParameter("inputId", employeeId)
                .getSingleResult();

        System.out.println(employee.jobAndProjectsFormat());
    }
}
