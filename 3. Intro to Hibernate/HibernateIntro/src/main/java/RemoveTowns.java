import entities.Address;
import entities.Town;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Scanner;

public class RemoveTowns {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String townName = scanner.nextLine();

        EntityManager entityManager = Utils.createEntityManager();
        entityManager.getTransaction().begin();

        Town town = entityManager.createQuery("FROM Town WHERE name = :inputTown", Town.class)
                .setParameter("inputTown", townName)
                .getSingleResult();

        List<Address> addresses = entityManager.createQuery("FROM Address WHERE town.name LIKE :inputTown", Address.class)
                .setParameter("inputTown", townName)
                .getResultList();

        addresses.forEach(t -> t.getEmployees()
                .forEach(em -> em.setAddress(null)));
        addresses.forEach(entityManager::remove);
        entityManager.remove(town);

        String addressForm = addresses.size() > 1 ? "addresses" : "address";

        System.out.printf("%d %s in %s deleted", addresses.size(), addressForm, townName);

        entityManager.getTransaction().commit();
        entityManager.close();
    }
}
