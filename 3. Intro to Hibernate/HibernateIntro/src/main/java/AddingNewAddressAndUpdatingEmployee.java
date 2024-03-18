import entities.Address;
import entities.Employee;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Scanner;

public class AddingNewAddressAndUpdatingEmployee {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String inputLastName = scanner.nextLine();

        String addressText = "Vitoshka 15";

        EntityManager entityManager = Utils.createEntityManager();
        entityManager.getTransaction().begin();

        Address address = new Address();
        address.setText(addressText);
        entityManager.persist(address);

        List<Employee> employees = entityManager
                .createQuery("FROM Employee WHERE lastName = :inputLastName", Employee.class)
                .setParameter("inputLastName", inputLastName)
                .getResultList();

        employees.forEach(e -> e.setAddress(address));

        entityManager.flush();
        entityManager.getTransaction().commit();
        entityManager.close();
    }
}
