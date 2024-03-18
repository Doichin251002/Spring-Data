
public class EmployeesMaximumSalaries {

    public static void main(String[] args) {
        Utils.createEntityManager()
                .createQuery("SELECT department.name, MAX(salary) FROM Employee GROUP BY department.id HAVING MAX(salary) NOT BETWEEN 30000 AND 70000",
                        Object[].class)
                .getResultList()
                .forEach(o -> System.out.println(o[0] + "   " + o[1]));
    }
}
