import entities.Employee;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.List;
import java.util.Scanner;

public class ContainsEmployee {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String fullName = scanner.nextLine();

        String isExistEmployee = Utils.createEntityManager()
                .createQuery("FROM Employee WHERE CONCAT_WS(' ', firstName, lastName) = :fullName ", Employee.class)
                .setParameter("fullName", fullName)
                .getResultList()
                .isEmpty() ? "No" : "Yes";

        System.out.println(isExistEmployee);
    }
}
