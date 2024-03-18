import entities.Department;
import entities.Employee;

import javax.persistence.EntityManager;

public class EmployeesFromDepartment {
    public static void main(String[] args) {
        Utils.createEntityManager()
                .createQuery("FROM Employee WHERE department.name = :department ORDER BY salary, id", Employee.class)
                .setParameter("department", "Research and Development")
                .getResultList()
                .forEach(e -> System.out.println(e.departmentAndSalaryFormat()));
    }
}
