import entities.Employee;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;

public class IncreaseSalaries {
    public static void main(String[] args) {
        List<String> departments = List.of("Engineering", "Tool Design", "Marketing", "Information Services");

        EntityManager entityManager = Utils.createEntityManager();
        entityManager.getTransaction().begin();

        List<Employee> employees = entityManager.createQuery("FROM Employee WHERE department.name IN (:deps)", Employee.class)
                .setParameter("deps", departments)
                .getResultList();

        for (Employee employee : employees) {
            BigDecimal currentSalary = employee.getSalary();
            BigDecimal increasedSalary = currentSalary.add(currentSalary.multiply(BigDecimal.valueOf(0.12)));

            employee.setSalary(increasedSalary);
            entityManager.persist(employee);

            System.out.println(employee.salaryFormat());
        }

        entityManager.getTransaction().commit();
        entityManager.close();
    }
}
