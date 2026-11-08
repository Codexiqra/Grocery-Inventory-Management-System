
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner input = new Scanner(System.in);

        ProductLinkedList inventory = new ProductLinkedList();

        int choice;

        do {

            System.out.println("\n================================");
            System.out.println("   GROCERY INVENTORY SYSTEM");
            System.out.println("================================");

            System.out.println("1. Add Product");
            System.out.println("2. View All Products");
            System.out.println("3. Search Product");
            System.out.println("4. Update Product");
            System.out.println("5. Delete Product");
            System.out.println("6. Exit");

            System.out.print("\nEnter your choice: ");

            choice = input.nextInt();

            switch (choice) {

                case 1:

                    System.out.print("Enter Product ID: ");
                    int id = input.nextInt();
                    input.nextLine();

                    System.out.print("Enter Product Name: ");
                    String name = input.nextLine();

                    System.out.print("Enter Category: ");
                    String category = input.nextLine();

                    System.out.print("Enter Price: ");
                    double price = input.nextDouble();

                    System.out.print("Enter Quantity: ");
                    int quantity = input.nextInt();

                    Product product = new Product(
                            id, name, category, price, quantity
                    );

                    inventory.addProduct(product);

                    break;

                case 2:

                    System.out.println("\n--- PRODUCT INVENTORY ---");

                    inventory.displayProducts();

                    break;

                case 3:

                    System.out.print("Enter Product ID to Search: ");

                    int searchId = input.nextInt();

                    inventory.searchProduct(searchId);

                    break;

                case 4:

                    System.out.print("Enter Product ID to Update: ");

                    int updateId = input.nextInt();
                    input.nextLine();

                    System.out.print("Enter New Product Name: ");
                    String newName = input.nextLine();

                    System.out.print("Enter New Category: ");
                    String newCategory = input.nextLine();

                    System.out.print("Enter New Price: ");
                    double newPrice = input.nextDouble();

                    System.out.print("Enter New Quantity: ");
                    int newQuantity = input.nextInt();

                    inventory.updateProduct(
                            updateId,
                            newName,
                            newCategory,
                            newPrice,
                            newQuantity
                    );

                    break;

                case 5:

                    System.out.print("Enter Product ID to Delete: ");

                    int deleteId = input.nextInt();

                    inventory.deleteProduct(deleteId);

                    break;

                case 6:

                    System.out.println("Thank you for using Grocery Inventory System!");

                    break;

                default:

                    System.out.println("Invalid Choice! Try Again.");

            }

        } while (choice != 6);

        input.close();
    }
}
