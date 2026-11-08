
import java.io.*;
import javax.swing.table.DefaultTableModel;
public class ProductLinkedList {

    // Node class
    private class Node {
        Product data;
        Node next;

        Node(Product data) {
            this.data = data;
            this.next = null;
        }
    }

    // Head of Linked List
    private Node head;

    // Constructor
    public ProductLinkedList() {
        head = null;
    }

    // Add Product
    public void addProduct(Product product) {

        Node newNode = new Node(product);

        if (head == null) {
            head = newNode;
        } else {
            Node current = head;

            while (current.next != null) {
                current = current.next;
            }

            current.next = newNode;
        }

        System.out.println("Product added successfully!");
    }

    // Display All Products
    public void displayProducts() {

        if (head == null) {
            System.out.println("Inventory is empty!");
            return;
        }

        Node current = head;

        while (current != null) {
            System.out.println("--------------------");
            current.data.displayProduct();
            current = current.next;
        }
    }

    // Search Product
    public void searchProduct(int id) {

        Node current = head;

        while (current != null) {

            if (current.data.getId() == id) {
                System.out.println("Product Found!");
                current.data.displayProduct();
                return;
            }

            current = current.next;
        }

        System.out.println("Product not found!");
    }

    // Update Product
    public void updateProduct(int id, String name,
                              String category, double price,
                              int quantity) {

        Node current = head;

        while (current != null) {

            if (current.data.getId() == id) {

                current.data.setName(name);
                current.data.setCategory(category);
                current.data.setPrice(price);
                current.data.setQuantity(quantity);

                System.out.println("Product updated successfully!");
                return;
            }

            current = current.next;
        }

        System.out.println("Product not found!");
    }

    // Delete Product
    public void deleteProduct(int id) {

        if (head == null) {
            System.out.println("Inventory is empty!");
            return;
        }

        if (head.data.getId() == id) {
            head = head.next;
            System.out.println("Product deleted successfully!");
            return;
        }

        Node current = head;

        while (current.next != null) {

            if (current.next.data.getId() == id) {
                current.next = current.next.next;
                System.out.println("Product deleted successfully!");
                return;
            }

            current = current.next;
        }

        System.out.println("Product not found!");
    }
    // Load Products into Dashboard Table
    public void loadIntoTable(DefaultTableModel model) {

        model.setRowCount(0);

        Node current = head;

        while (current != null) {

            Product p = current.data;

            model.addRow(new Object[]{
                    p.getId(),
                    p.getName(),
                    p.getCategory(),
                    p.getPrice(),
                    p.getQuantity()
            });

            current = current.next;
        }
    }
    // SAVE PRODUCTS TO FILE
    public void saveToFile(String filename) {

        try (DataOutputStream output = new DataOutputStream(
                new FileOutputStream(filename))) {

            int count = 0;
            Node current = head;

            while (current != null) {
                count++;
                current = current.next;
            }

            output.writeInt(count);

            current = head;

            while (current != null) {

                Product p = current.data;

                output.writeInt(p.getId());
                output.writeUTF(p.getName());
                output.writeUTF(p.getCategory());
                output.writeDouble(p.getPrice());
                output.writeInt(p.getQuantity());

                current = current.next;
            }

            System.out.println("Products saved successfully!");

        } catch (IOException e) {
            System.out.println("Error saving products: " + e.getMessage());
        }
    }

    // LOAD PRODUCTS FROM FILE
    public void loadFromFile(String filename) {

        File file = new File(filename);

        if (!file.exists()) {
            return;
        }

        try (DataInputStream input = new DataInputStream(
                new FileInputStream(file))) {

            head = null;

            int count = input.readInt();

            for (int i = 0; i < count; i++) {

                int id = input.readInt();
                String name = input.readUTF();
                String category = input.readUTF();
                double price = input.readDouble();
                int quantity = input.readInt();

                Product product = new Product(
                        id, name, category, price, quantity
                );

                addProduct(product);
            }

            System.out.println("Products loaded successfully!");

        } catch (IOException e) {
            System.out.println("Error loading products: " + e.getMessage());
        }
    }
    public String getLowStockProducts(int threshold) {

        StringBuilder result = new StringBuilder();
        Node current = head;

        while (current != null) {

            Product p = current.data;

            if (p.getQuantity() <= threshold) {

                result.append("Product: ")
                        .append(p.getName())
                        .append(" | Quantity: ")
                        .append(p.getQuantity())
                        .append("\n");
            }

            current = current.next;
        }

        if (result.length() == 0) {
            return "All products have sufficient stock!";
        }

        return result.toString();
    }
}

