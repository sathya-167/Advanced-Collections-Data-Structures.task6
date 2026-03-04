import java.util.*;

public class InventoryManagementSystem {

    // ================= PRODUCT CLASS =================
    static class Product implements Comparable<Product> {
        private String sku;
        private String name;
        private String category;
        private double price;
        private int quantity;
        private Date lastUpdated;

        public Product(String sku, String name, String category, double price, int quantity) {
            this.sku = sku;
            this.name = name;
            this.category = category;
            this.price = price;
            this.quantity = quantity;
            this.lastUpdated = new Date();
        }

        // Natural Sorting by SKU
        @Override
        public int compareTo(Product other) {
            return this.sku.compareTo(other.sku);
        }

        // For HashSet uniqueness
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null || getClass() != obj.getClass())
                return false;
            Product product = (Product) obj;
            return sku.equals(product.sku);
        }

        @Override
        public int hashCode() {
            return sku.hashCode();
        }

        public String getSku() {
            return sku;
        }

        public String getName() {
            return name;
        }

        public String getCategory() {
            return category;
        }

        public double getPrice() {
            return price;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
            this.lastUpdated = new Date();
        }

        public double getInventoryValue() {
            return price * quantity;
        }
    }

    // ================= COMPARATORS =================
    static class PriceComparator implements Comparator<Product> {
        public int compare(Product p1, Product p2) {
            return Double.compare(p1.getPrice(), p2.getPrice());
        }
    }

    static class ValueComparator implements Comparator<Product> {
        public int compare(Product p1, Product p2) {
            return Double.compare(p2.getInventoryValue(), p1.getInventoryValue());
        }
    }

    static class NameComparator implements Comparator<Product> {
        public int compare(Product p1, Product p2) {
            return p1.getName().compareTo(p2.getName());
        }
    }

    // ================= COLLECTIONS =================
    private HashSet<Product> productSet = new HashSet<>();
    private LinkedList<String> transactionHistory = new LinkedList<>();
    private Stack<Product> undoStack = new Stack<>();
    private Queue<Product> lowStockQueue = new LinkedList<>();
    private double totalInventoryValue = 0;

    // ================= METHODS =================

    public void addProduct(Product product) {
        if (productSet.add(product)) {
            totalInventoryValue += product.getInventoryValue();
            transactionHistory.addFirst("ADD ➜ " + product.getSku() + " at " + new Date());

            if (product.getQuantity() < 10) {
                lowStockQueue.add(product);
                System.out.println("⚠ WARNING: Low stock item added!");
            }

            System.out.println("✔ SUCCESS: " + product.getName() + " added to inventory.");
        } else {
            System.out.println("✖ ERROR: SKU already exists!");
        }
    }

    public void updateProductQuantity(String sku, int newQty) {
        for (Product p : productSet) {
            if (p.getSku().equalsIgnoreCase(sku)) {

                undoStack.push(new Product(
                        p.getSku(),
                        p.getName(),
                        p.getCategory(),
                        p.getPrice(),
                        p.getQuantity()));

                int oldQty = p.getQuantity();
                totalInventoryValue -= p.getPrice() * oldQty;

                p.setQuantity(newQty);

                totalInventoryValue += p.getPrice() * newQty;

                transactionHistory.addFirst("UPDATE ➜ " + sku + " from " + oldQty + " to " + newQty);

                System.out.println("✔ Quantity updated successfully.");
                return;
            }
        }
        System.out.println("✖ Product not found!");
    }

    public void undoLastUpdate() {
        if (undoStack.isEmpty()) {
            System.out.println("No operations to undo.");
            return;
        }

        Product prev = undoStack.pop();
        updateProductQuantity(prev.getSku(), prev.getQuantity());
        System.out.println("✔ Last update undone successfully.");
    }

    public void displayProductsSortedBy(String criteria) {

        List<Product> list = new ArrayList<>(productSet);

        switch (criteria.toLowerCase()) {
            case "sku":
                Collections.sort(list);
                break;
            case "price":
                Collections.sort(list, new PriceComparator());
                break;
            case "value":
                Collections.sort(list, new ValueComparator());
                break;
            case "name":
                Collections.sort(list, new NameComparator());
                break;
            default:
                System.out.println("Invalid sort option.");
                return;
        }

        System.out.println("\n================ PRODUCT LIST ================");
        System.out.printf("%-10s %-15s %-15s %-10s %-8s %-12s\n",
                "SKU", "NAME", "CATEGORY", "PRICE", "QTY", "VALUE");
        System.out.println("--------------------------------------------------------------");

        for (Product p : list) {
            System.out.printf("%-10s %-15s %-15s ₹%-9.2f %-8d ₹%-11.2f\n",
                    p.getSku(),
                    p.getName(),
                    p.getCategory(),
                    p.getPrice(),
                    p.getQuantity(),
                    p.getInventoryValue());
        }
    }

    public void displayLowStock() {
        System.out.println("\n======= LOW STOCK ITEMS =======");
        if (lowStockQueue.isEmpty()) {
            System.out.println("All products sufficiently stocked.");
            return;
        }

        for (Product p : lowStockQueue) {
            System.out.println("SKU: " + p.getSku() + " | Qty: " + p.getQuantity());
        }
    }

    public void displayTransactions(int count) {
        System.out.println("\n======= TRANSACTION HISTORY =======");
        int i = 0;
        for (String t : transactionHistory) {
            if (i++ == count)
                break;
            System.out.println(t);
        }
    }

    public void displayStats() {
        System.out.println("\n====== INVENTORY REPORT ======");
        System.out.println("Total Unique Products: " + productSet.size());
        System.out.println("Total Inventory Worth: ₹" + totalInventoryValue);
        System.out.println("Generated On: " + new Date());
        System.out.println("================================");
    }

    // ================= MAIN =================
    public static void main(String[] args) {

        InventoryManagementSystem ims = new InventoryManagementSystem();
        Scanner sc = new Scanner(System.in);

        System.out.println("=======================================");
        System.out.println("     WELCOME TO MY INVENTORY SYSTEM");
        System.out.println("=======================================");

        while (true) {
            System.out.println("\n========= MENU =========");
            System.out.println("1 ➜ Add Product");
            System.out.println("2 ➜ Update Quantity");
            System.out.println("3 ➜ View Products");
            System.out.println("4 ➜ Low Stock Alerts");
            System.out.println("5 ➜ Transaction History");
            System.out.println("6 ➜ Inventory Statistics");
            System.out.println("7 ➜ Undo Last Update");
            System.out.println("8 ➜ Exit");
            System.out.println("========================");
            System.out.print("Choose option: ");

            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {

                case 1:
                    System.out.print("Enter SKU: ");
                    String sku = sc.nextLine();

                    System.out.print("Enter Name: ");
                    String name = sc.nextLine();

                    System.out.print("Enter Category: ");
                    String category = sc.nextLine();

                    System.out.print("Enter Price: ");
                    double price = sc.nextDouble();

                    System.out.print("Enter Quantity: ");
                    int qty = sc.nextInt();

                    ims.addProduct(new Product(sku, name, category, price, qty));
                    break;

                case 2:
                    System.out.print("Enter SKU: ");
                    String uSku = sc.nextLine();

                    System.out.print("New Quantity: ");
                    int newQty = sc.nextInt();

                    ims.updateProductQuantity(uSku, newQty);
                    break;

                case 3:
                    System.out.print("Sort by (sku/price/value/name): ");
                    String sort = sc.nextLine();
                    ims.displayProductsSortedBy(sort);
                    break;

                case 4:
                    ims.displayLowStock();
                    break;

                case 5:
                    System.out.print("How many transactions? ");
                    int count = sc.nextInt();
                    ims.displayTransactions(count);
                    break;

                case 6:
                    ims.displayStats();
                    break;

                case 7:
                    ims.undoLastUpdate();
                    break;

                case 8:
                    System.out.println("Thank you for using the system!");
                    System.exit(0);

                default:
                    System.out.println("Invalid choice!");
            }
        }
    }
}