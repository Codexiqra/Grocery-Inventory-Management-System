import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class GroceryGUI extends JFrame {

    ProductLinkedList inventory = new ProductLinkedList();

    DefaultTableModel model;
    JTable table;
    JComboBox<String> categoryFilter;
    JComboBox<String> sortFilter;
    TableRowSorter<DefaultTableModel> sorter;

    JLabel totalProducts, totalQuantity, totalValue, lowStockCount;

    CardLayout cardLayout;
    JPanel mainContent;

    JTextField idField, nameField, priceField, quantityField, globalSearch;
    JComboBox<String> addCategory;

    // BROWN AND WHITE THEME

    Color darkGreen = new Color(62, 39, 35);
    Color green = new Color(121, 85, 72);
    Color lightGreen = new Color(215, 204, 200);
    Color background = new Color(250, 247, 243);
    Color white = Color.WHITE;
    Color textColor = new Color(78, 52, 46);

    Font headingFont = new Font("Arial", Font.BOLD, 24);
    Font normalFont = new Font("Arial", Font.PLAIN, 14);

    String[] categories = {
            "Fruits", "Vegetables", "Dairy", "Bakery", "Beverages"
    };

    public GroceryGUI() {

        setTitle("FreshMart - Grocery Management System");
        setSize(1450, 900);
        setMinimumSize(new Dimension(1100, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        inventory.loadFromFile("products.dat");

        setLayout(new BorderLayout());

        String[] columns = {
                "Product ID", "Product Name", "Category", "Price", "Quantity"
        };

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        add(createHeader(), BorderLayout.NORTH);
        add(createSidebar(), BorderLayout.WEST);

        cardLayout = new CardLayout();
        mainContent = new JPanel(cardLayout);
        mainContent.setBackground(background);

        mainContent.add(createDashboard(), "Dashboard");
        mainContent.add(createProductsPage(), "Products");
        mainContent.add(createAddProductPage(), "AddProduct");

        add(mainContent, BorderLayout.CENTER);

        refreshTable();
        setVisible(true);
    }

    // HEADER

    private JPanel createHeader() {

        JPanel header = new JPanel(new BorderLayout(20, 10));
        header.setBackground(background);
        header.setPreferredSize(new Dimension(1200, 90));
        header.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));

        JLabel logo = new JLabel("☕ FreshMart");
        logo.setFont(new Font("Arial", Font.BOLD, 30));
        logo.setForeground(darkGreen);

        globalSearch = new JTextField();
        globalSearch.setFont(new Font("Arial", Font.PLAIN, 16));
        globalSearch.setText("Search products...");
        globalSearch.setForeground(Color.GRAY);
        globalSearch.setPreferredSize(new Dimension(450, 45));

        globalSearch.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (globalSearch.getText().equals("Search products...")) {
                    globalSearch.setText("");
                    globalSearch.setForeground(textColor);
                }
            }

            public void focusLost(FocusEvent e) {
                if (globalSearch.getText().isBlank()) {
                    globalSearch.setText("Search products...");
                    globalSearch.setForeground(Color.GRAY);
                }
            }
        });

        globalSearch.addActionListener(e -> {
            refreshTable();
            showPage("Products");
            applyFilters();
        });

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(background);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        searchPanel.add(globalSearch, BorderLayout.CENTER);

        JLabel admin = new JLabel("● Store Admin   ▼");
        admin.setFont(new Font("Arial", Font.BOLD, 16));
        admin.setForeground(darkGreen);

        header.add(logo, BorderLayout.WEST);
        header.add(searchPanel, BorderLayout.CENTER);
        header.add(admin, BorderLayout.EAST);

        return header;
    }

    // SIDEBAR

    private JPanel createSidebar() {

        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(darkGreen);
        sidebar.setPreferredSize(new Dimension(260, 0));

        JPanel menuPanel = new JPanel(new GridLayout(8, 1, 8, 12));
        menuPanel.setBackground(darkGreen);
        menuPanel.setBorder(BorderFactory.createEmptyBorder(25, 15, 20, 15));

        String[] menu = {
                "🏠   Dashboard",
                "▣   View Products",
                "✚   Add Product",
                "⌕   Search Product",
                "✎   Update Product",
                "▤   Delete Product",
                "⚠   Low Stock Alert",
                "⇥   Exit"
        };

        String[] actions = {
                "Dashboard", "View Products", "Add Product",
                "Search Product", "Update Product",
                "Delete Product", "Low Stock Alert", "Exit"
        };

        for (int i = 0; i < menu.length; i++) {

            final String action = actions[i];

            JButton button = new JButton(menu[i]);
            button.setFont(new Font("Arial", Font.BOLD, 15));
            button.setBackground(darkGreen);
            button.setForeground(white);
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createEmptyBorder(15, 12, 15, 5));
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));

            button.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    button.setBackground(green);
                }

                public void mouseExited(MouseEvent e) {
                    button.setBackground(darkGreen);
                }
            });

            button.addActionListener(e -> {
                switch (action) {

                    case "Dashboard":
                        refreshTable();
                        showPage("Dashboard");
                        break;

                    case "View Products":
                        refreshTable();
                        showPage("Products");
                        break;

                    case "Add Product":
                        clearAddProductFields();
                        showPage("AddProduct");
                        break;

                    case "Search Product":
                        searchProduct();
                        break;

                    case "Update Product":
                        updateProduct();
                        break;

                    case "Delete Product":
                        deleteProduct();
                        break;

                    case "Low Stock Alert":
                        showLowStock();
                        break;

                    case "Exit":
                        int choice = JOptionPane.showConfirmDialog(
                                this,
                                "Are you sure you want to exit?",
                                "Exit FreshMart",
                                JOptionPane.YES_NO_OPTION
                        );

                        if (choice == JOptionPane.YES_OPTION) {
                            inventory.saveToFile("products.dat");
                            System.exit(0);
                        }
                        break;
                }
            });

            menuPanel.add(button);
        }

        JLabel footer = new JLabel(
                "<html><center><i>Good Food<br>Good Mood</i><br>♡</center></html>"
        );

        footer.setForeground(white);
        footer.setFont(new Font("Serif", Font.ITALIC, 24));
        footer.setHorizontalAlignment(SwingConstants.CENTER);
        footer.setBorder(BorderFactory.createEmptyBorder(15, 10, 25, 10));

        sidebar.add(menuPanel, BorderLayout.NORTH);
        sidebar.add(footer, BorderLayout.SOUTH);

        return sidebar;
    }

    // BUTTON DESIGN

    private JButton createButton(String name, Color color) {

        JButton button = new JButton(name);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(color);
        button.setForeground(textColor);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    // IMAGE LOADER

    private ImageIcon loadImage(String path, int width, int height) {

        try {

            File file = new File(System.getProperty("user.dir"), path);

            System.out.println("Checking image: " + file.getAbsolutePath());

            if (!file.exists()) {
                file = new File("src", path);
            }

            System.out.println("Image Exists: " + file.exists());

            if (!file.exists()) {
                System.out.println("IMAGE NOT FOUND: " + path);
                return null;
            }

            ImageIcon original = new ImageIcon(file.getAbsolutePath());

            if (original.getIconWidth() <= 0) {
                System.out.println("IMAGE CANNOT BE READ: " + path);
                return null;
            }

            Image scaled = original.getImage().getScaledInstance(
                    width, height, Image.SCALE_SMOOTH
            );

            System.out.println("IMAGE LOADED: " + path);

            return new ImageIcon(scaled);

        } catch (Exception e) {
            System.out.println("IMAGE ERROR: " + path);
            e.printStackTrace();
            return null;
        }
    }

    // DASHBOARD

    private JPanel createDashboard() {

        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(background);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(background);

        JPanel banner = new JPanel(new BorderLayout());
        banner.setPreferredSize(new Dimension(1100, 265));
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 265));
        banner.setBackground(darkGreen);

        ImageIcon hero = loadImage(
                "images/freshmart_hero_banner.png", 1200, 265
        );

        if (hero != null) {
            banner.add(new JLabel(hero), BorderLayout.CENTER);
        } else {
            JLabel welcome = new JLabel(
                    "<html><div style='padding:25px;color:white;'>"
                            + "<h1>Freshness Delivered Every Day!</h1>"
                            + "<h2>Healthy Choices • Better Life</h2>"
                            + "<p>Fresh products. Better health. Happier you.</p>"
                            + "</div></html>"
            );
            welcome.setForeground(white);
            banner.add(welcome, BorderLayout.CENTER);
        }

        banner.setBorder(BorderFactory.createLineBorder(lightGreen, 2));

        content.add(banner);
        content.add(Box.createVerticalStrut(12));

        totalProducts = new JLabel("0");
        totalQuantity = new JLabel("0");
        totalValue = new JLabel("0.00");
        lowStockCount = new JLabel("0");

        JPanel cards = new JPanel(new GridLayout(1, 4, 15, 10));
        cards.setBackground(background);
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 105));

        cards.add(createCard("🛒 Total Products", totalProducts));
        cards.add(createCard("▣ Total Quantity", totalQuantity));
        cards.add(createCard("💰 Inventory Value (Rs.)", totalValue));
        cards.add(createCard("⚠ Low Stock Products", lowStockCount));

        content.add(cards);
        content.add(Box.createVerticalStrut(15));

        JLabel categoryTitle = new JLabel("☕ Product Categories");
        categoryTitle.setFont(new Font("Arial", Font.BOLD, 21));
        categoryTitle.setForeground(darkGreen);
        categoryTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(categoryTitle);
        content.add(Box.createVerticalStrut(10));

        JPanel categoriesPanel = new JPanel(new GridLayout(1, 5, 12, 12));
        categoriesPanel.setBackground(background);
        categoriesPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 195));

        String[] imagePaths = {
                "images/fruits.png",
                "images/vegetables.png",
                "images/dairy.png",
                "images/bakery.png",
                "images/beverages.png"
        };

        Color[] categoryColors = {
                new Color(239, 228, 220),
                new Color(230, 216, 207),
                new Color(245, 237, 228),
                new Color(222, 204, 193),
                new Color(236, 225, 216)
        };

        for (int i = 0; i < categories.length; i++) {

            final String selected = categories[i];

            JPanel categoryCard = new JPanel(new BorderLayout(5, 5));
            categoryCard.setBackground(categoryColors[i]);

            categoryCard.setBorder(
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(205, 185, 174)),
                            BorderFactory.createEmptyBorder(8, 8, 8, 8)
                    )
            );

            JLabel imageLabel = new JLabel();
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);

            ImageIcon categoryImage = loadImage(imagePaths[i], 170, 115);

            if (categoryImage != null) {
                imageLabel.setIcon(categoryImage);
            } else {
                imageLabel.setText("☕");
                imageLabel.setFont(new Font("Arial", Font.PLAIN, 45));
            }

            JLabel name = new JLabel(selected);
            name.setFont(new Font("Arial", Font.BOLD, 16));
            name.setForeground(darkGreen);
            name.setHorizontalAlignment(SwingConstants.CENTER);

            JLabel explore = new JLabel("Explore Products →");
            explore.setFont(new Font("Arial", Font.PLAIN, 13));
            explore.setForeground(darkGreen);
            explore.setHorizontalAlignment(SwingConstants.CENTER);

            JPanel labels = new JPanel(new GridLayout(2, 1));
            labels.setOpaque(false);
            labels.add(name);
            labels.add(explore);

            categoryCard.add(imageLabel, BorderLayout.CENTER);
            categoryCard.add(labels, BorderLayout.SOUTH);

            categoryCard.setCursor(new Cursor(Cursor.HAND_CURSOR));

            categoryCard.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    categoryFilter.setSelectedItem(selected);
                    refreshTable();
                    applyFilters();
                    showPage("Products");
                }
            });

            categoriesPanel.add(categoryCard);
        }

        content.add(categoriesPanel);
        content.add(Box.createVerticalStrut(15));

        JPanel lower = new JPanel(new GridLayout(1, 2, 15, 10));
        lower.setBackground(background);
        lower.setPreferredSize(new Dimension(1100, 260));
        lower.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));

        JPanel recentPanel = new JPanel(new BorderLayout(8, 10));
        recentPanel.setBackground(white);
        recentPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(lightGreen, 2),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                )
        );

        JLabel recentTitle = new JLabel("🛒 Recent Products");
        recentTitle.setFont(new Font("Arial", Font.BOLD, 18));
        recentTitle.setForeground(darkGreen);

        JButton viewAll = createButton("View All →", green);
        viewAll.setForeground(white);

        viewAll.addActionListener(e -> {
            refreshTable();
            showPage("Products");
        });

        JPanel recentHeader = new JPanel(new BorderLayout());
        recentHeader.setBackground(white);
        recentHeader.add(recentTitle, BorderLayout.WEST);
        recentHeader.add(viewAll, BorderLayout.EAST);

        JTable recentTable = new JTable(model);
        styleTable(recentTable);

        JScrollPane recentScroll = new JScrollPane(recentTable);
        recentScroll.setBorder(BorderFactory.createEmptyBorder());

        recentPanel.add(recentHeader, BorderLayout.NORTH);
        recentPanel.add(recentScroll, BorderLayout.CENTER);

        JPanel quickPanel = new JPanel(new BorderLayout(8, 10));
        quickPanel.setBackground(white);
        quickPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(lightGreen, 2),
                        BorderFactory.createEmptyBorder(12, 12, 12, 12)
                )
        );

        JLabel quickTitle = new JLabel("⚡ Quick Actions");
        quickTitle.setFont(new Font("Arial", Font.BOLD, 18));
        quickTitle.setForeground(darkGreen);

        JPanel actions = new JPanel(new GridLayout(2, 2, 10, 10));
        actions.setBackground(white);

        JButton addButton = createButton("+ Add Product", green);
        addButton.setForeground(white);

        JButton searchButton = createButton("⌕ Search Product", lightGreen);
        JButton updateButton = createButton("✎ Update Product", new Color(232, 220, 211));
        JButton lowButton = createButton("⚠ Low Stock Alert", new Color(242, 225, 220));

        addButton.addActionListener(e -> {
            clearAddProductFields();
            showPage("AddProduct");
        });

        searchButton.addActionListener(e -> searchProduct());
        updateButton.addActionListener(e -> updateProduct());
        lowButton.addActionListener(e -> showLowStock());

        actions.add(addButton);
        actions.add(searchButton);
        actions.add(updateButton);
        actions.add(lowButton);

        quickPanel.add(quickTitle, BorderLayout.NORTH);
        quickPanel.add(actions, BorderLayout.CENTER);

        lower.add(recentPanel);
        lower.add(quickPanel);

        content.add(lower);

        JLabel footer = new JLabel("☕ FreshMart - Grocery Management System");
        footer.setFont(new Font("Arial", Font.BOLD, 14));
        footer.setForeground(darkGreen);
        footer.setHorizontalAlignment(SwingConstants.CENTER);
        footer.setBorder(BorderFactory.createEmptyBorder(15, 5, 8, 5));

        content.add(footer);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.getHorizontalScrollBar().setUnitIncrement(15);

        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // DASHBOARD CARDS

    private JPanel createCard(String title, JLabel value) {

        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(white);

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(lightGreen, 2),
                        BorderFactory.createEmptyBorder(15, 12, 15, 12)
                )
        );

        JLabel heading = new JLabel(title);
        heading.setFont(new Font("Arial", Font.BOLD, 13));
        heading.setForeground(darkGreen);

        value.setFont(new Font("Arial", Font.BOLD, 26));
        value.setForeground(green);

        card.add(heading, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);

        return card;
    }

    // TABLE DESIGN

    private void styleTable(JTable target) {

        target.setRowHeight(38);
        target.setFont(normalFont);
        target.setForeground(textColor);
        target.setSelectionBackground(lightGreen);
        target.setSelectionForeground(darkGreen);
        target.setGridColor(new Color(225, 215, 207));
        target.setShowVerticalLines(true);

        target.getTableHeader().setBackground(darkGreen);
        target.getTableHeader().setForeground(white);
        target.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        target.getTableHeader().setPreferredSize(new Dimension(0, 38));
    }

    // PRODUCTS PAGE

    private JPanel createProductsPage() {

        JPanel panel = new JPanel(new BorderLayout(15, 20));
        panel.setBackground(background);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel heading = new JLabel("Product Inventory");
        heading.setFont(headingFont);
        heading.setForeground(darkGreen);

        categoryFilter = new JComboBox<>(new String[]{
                "All Categories", "Fruits", "Vegetables",
                "Dairy", "Bakery", "Beverages", "Snacks", "Other"
        });

        categoryFilter.setFont(normalFont);
        categoryFilter.setBackground(white);
        categoryFilter.setPreferredSize(new Dimension(190, 38));

        sortFilter = new JComboBox<>(new String[]{
                "Default",
                "Name (A-Z)",
                "Price (Low to High)",
                "Price (High to Low)",
                "Quantity (Low to High)",
                "Quantity (High to Low)"
        });

        sortFilter.setFont(normalFont);
        sortFilter.setBackground(white);
        sortFilter.setPreferredSize(new Dimension(200, 38));

        JLabel filterLabel = new JLabel("Filter by Category:");
        filterLabel.setFont(new Font("Arial", Font.BOLD, 14));
        filterLabel.setForeground(darkGreen);

        JLabel sortLabel = new JLabel("Sort By:");
        sortLabel.setFont(new Font("Arial", Font.BOLD, 14));
        sortLabel.setForeground(darkGreen);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        filterPanel.setBackground(background);
        filterPanel.add(filterLabel);
        filterPanel.add(categoryFilter);
        filterPanel.add(sortLabel);
        filterPanel.add(sortFilter);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(background);
        topPanel.add(heading, BorderLayout.WEST);
        topPanel.add(filterPanel, BorderLayout.EAST);

        panel.add(topPanel, BorderLayout.NORTH);

        table = new JTable(model);
        styleTable(table);

        sorter = new TableRowSorter<>(model);

        sorter.setComparator(3, (a, b) ->
                Double.compare(
                        Double.parseDouble(a.toString()),
                        Double.parseDouble(b.toString())
                )
        );

        sorter.setComparator(4, (a, b) ->
                Integer.compare(
                        Integer.parseInt(a.toString()),
                        Integer.parseInt(b.toString())
                )
        );

        table.setRowSorter(sorter);

        categoryFilter.addActionListener(e -> applyFilters());
        sortFilter.addActionListener(e -> applySorting());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(lightGreen, 2));

        panel.add(scroll, BorderLayout.CENTER);

        JButton refreshButton = createButton("Refresh Products", green);
        refreshButton.setForeground(white);

        JButton exportButton = createButton("Export CSV", darkGreen);
        exportButton.setForeground(white);

        refreshButton.addActionListener(e -> {
            refreshTable();
            applyFilters();
            applySorting();
        });

        exportButton.addActionListener(e -> exportToCSV());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 5));
        bottom.setBackground(background);

        bottom.add(refreshButton);
        bottom.add(exportButton);

        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    // EXPORT CSV

    private void exportToCSV() {

        JFileChooser chooser = new JFileChooser();

        chooser.setDialogTitle("Save Inventory Report");

        chooser.setSelectedFile(new File("FreshMart_Inventory.csv"));

        int result = chooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {

            File file = chooser.getSelectedFile();

            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new File(file.getAbsolutePath() + ".csv");
            }

            try (java.io.PrintWriter writer =
                         new java.io.PrintWriter(
                                 new java.io.FileWriter(file))) {

                writer.println(
                        "Product ID,Product Name,Category,Price,Quantity"
                );

                for (int i = 0; i < model.getRowCount(); i++) {

                    for (int j = 0; j < model.getColumnCount(); j++) {

                        Object value = model.getValueAt(i, j);

                        String text = value == null
                                ? ""
                                : value.toString();

                        text = "\"" + text.replace("\"", "\"\"") + "\"";

                        writer.print(text);

                        if (j < model.getColumnCount() - 1) {
                            writer.print(",");
                        }
                    }

                    writer.println();
                }

                JOptionPane.showMessageDialog(
                        this,
                        "Inventory report exported successfully!\n\n"
                                + "File: " + file.getAbsolutePath(),
                        "Export Successful",
                        JOptionPane.INFORMATION_MESSAGE
                );

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                        this,
                        "Error exporting report: " + ex.getMessage(),
                        "Export Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    // FILTERS

    private void applyFilters() {

        if (sorter == null) return;

        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        if (categoryFilter != null) {

            String selected = categoryFilter.getSelectedItem().toString();

            if (!selected.equals("All Categories")) {
                filters.add(
                        RowFilter.regexFilter(
                                "(?i)^" + Pattern.quote(selected) + "$", 2
                        )
                );
            }
        }

        if (globalSearch != null) {

            String search = globalSearch.getText().trim();

            if (!search.isEmpty() && !search.equals("Search products...")) {
                filters.add(
                        RowFilter.regexFilter(
                                "(?i)" + Pattern.quote(search), 0, 1, 2
                        )
                );
            }
        }

        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    // SORTING

    private void applySorting() {

        if (sorter == null || sortFilter == null) return;

        String selected = sortFilter.getSelectedItem().toString();

        switch (selected) {

            case "Name (A-Z)":
                sorter.setSortKeys(
                        List.of(new RowSorter.SortKey(1, SortOrder.ASCENDING))
                );
                break;

            case "Price (Low to High)":
                sorter.setSortKeys(
                        List.of(new RowSorter.SortKey(3, SortOrder.ASCENDING))
                );
                break;

            case "Price (High to Low)":
                sorter.setSortKeys(
                        List.of(new RowSorter.SortKey(3, SortOrder.DESCENDING))
                );
                break;

            case "Quantity (Low to High)":
                sorter.setSortKeys(
                        List.of(new RowSorter.SortKey(4, SortOrder.ASCENDING))
                );
                break;

            case "Quantity (High to Low)":
                sorter.setSortKeys(
                        List.of(new RowSorter.SortKey(4, SortOrder.DESCENDING))
                );
                break;

            default:
                sorter.setSortKeys(null);
                break;
        }
    }

    // ADD PRODUCT PAGE

    private JPanel createAddProductPage() {

        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(background);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 35, 30, 35));

        JLabel heading = new JLabel("Add New Product");
        heading.setFont(headingFont);
        heading.setForeground(darkGreen);

        JLabel subtitle = new JLabel(
                "Enter product details to add them to your inventory."
        );
        subtitle.setFont(normalFont);
        subtitle.setForeground(Color.GRAY);

        JPanel top = new JPanel(new GridLayout(2, 1, 5, 5));
        top.setBackground(background);
        top.add(heading);
        top.add(subtitle);

        panel.add(top, BorderLayout.NORTH);

        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(white);

        formCard.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(lightGreen, 2),
                        BorderFactory.createEmptyBorder(30, 35, 30, 35)
                )
        );

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        idField = new JTextField(20);
        addFormRow(formCard, gbc, 0, "Product ID", idField);

        nameField = new JTextField(20);
        addFormRow(formCard, gbc, 1, "Product Name", nameField);

        addCategory = new JComboBox<>(new String[]{
                "Select Category", "Fruits", "Vegetables",
                "Dairy", "Bakery", "Beverages", "Snacks", "Other"
        });

        addCategory.setFont(normalFont);
        addCategory.setBackground(white);
        addCategory.setPreferredSize(new Dimension(250, 40));

        addFormRow(formCard, gbc, 2, "Category", addCategory);

        priceField = new JTextField(20);
        addFormRow(formCard, gbc, 3, "Price", priceField);

        quantityField = new JTextField(20);
        addFormRow(formCard, gbc, 4, "Quantity", quantityField);

        JButton addButton = createButton("+ Add Product", green);
        addButton.setForeground(white);

        JButton clearButton = createButton("Clear Fields", lightGreen);

        JButton viewButton = createButton("View Products", darkGreen);
        viewButton.setForeground(white);

        addButton.addActionListener(e -> addProduct());
        clearButton.addActionListener(e -> clearAddProductFields());

        viewButton.addActionListener(e -> {
            refreshTable();
            showPage("Products");
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(white);

        buttonPanel.add(addButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(viewButton);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.weightx = 1;

        formCard.add(buttonPanel, gbc);

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(background);

        GridBagConstraints centerGbc = new GridBagConstraints();
        centerGbc.fill = GridBagConstraints.HORIZONTAL;
        centerGbc.weightx = 1;
        centerGbc.weighty = 1;
        centerGbc.gridx = 0;
        centerGbc.gridy = 0;
        centerGbc.insets = new Insets(10, 10, 10, 10);

        center.add(formCard, centerGbc);
        panel.add(center, BorderLayout.CENTER);

        return panel;
    }

    // FORM ROW

    private void addFormRow(
            JPanel panel, GridBagConstraints gbc,
            int row, String labelText, JComponent field
    ) {

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 15));
        label.setForeground(darkGreen);

        field.setFont(normalFont);
        field.setPreferredSize(new Dimension(250, 40));

        gbc.gridwidth = 1;
        gbc.weightx = 0.4;
        gbc.gridx = 0;
        gbc.gridy = row;

        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.6;

        panel.add(field, gbc);
    }

    // CLEAR FORM

    private void clearAddProductFields() {

        if (idField != null) {
            idField.setText("");
            nameField.setText("");
            addCategory.setSelectedIndex(0);
            priceField.setText("");
            quantityField.setText("");
        }
    }

    // REFRESH TABLE AND STATISTICS

    private void refreshTable() {

        inventory.loadIntoTable(model);

        int quantity = 0;
        int lowStock = 0;
        double value = 0;

        for (int i = 0; i < model.getRowCount(); i++) {

            double price = Double.parseDouble(
                    model.getValueAt(i, 3).toString()
            );

            int qty = Integer.parseInt(
                    model.getValueAt(i, 4).toString()
            );

            quantity += qty;
            value += price * qty;

            if (qty <= 5) lowStock++;
        }

        if (totalProducts != null) {
            totalProducts.setText(String.valueOf(model.getRowCount()));
            totalQuantity.setText(String.valueOf(quantity));
            totalValue.setText(String.format("%.2f", value));
            lowStockCount.setText(String.valueOf(lowStock));
        }
    }

    // NAVIGATION

    private void showPage(String page) {
        cardLayout.show(mainContent, page);
    }

    // ADD PRODUCT

    private void addProduct() {

        try {

            if (idField.getText().isBlank()
                    || nameField.getText().isBlank()
                    || priceField.getText().isBlank()
                    || quantityField.getText().isBlank()
                    || addCategory.getSelectedIndex() == 0) {

                JOptionPane.showMessageDialog(
                        this, "Please fill all product details!"
                );
                return;
            }

            int productId = Integer.parseInt(idField.getText().trim());
            double productPrice = Double.parseDouble(priceField.getText().trim());
            int productQuantity = Integer.parseInt(quantityField.getText().trim());

            if (productId <= 0 || productPrice < 0 || productQuantity < 0) {
                JOptionPane.showMessageDialog(
                        this, "Enter valid positive values!"
                );
                return;
            }

            for (int i = 0; i < model.getRowCount(); i++) {

                if (Integer.parseInt(model.getValueAt(i, 0).toString()) == productId) {
                    JOptionPane.showMessageDialog(
                            this, "Product ID already exists!"
                    );
                    return;
                }
            }

            Product product = new Product(
                    productId,
                    nameField.getText().trim(),
                    addCategory.getSelectedItem().toString(),
                    productPrice,
                    productQuantity
            );

            inventory.addProduct(product);
            inventory.saveToFile("products.dat");

            refreshTable();

            JOptionPane.showMessageDialog(
                    this, "Product Added Successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE
            );

            clearAddProductFields();

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter valid numbers for ID, Price and Quantity!"
            );

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this, "Unable to add product: " + ex.getMessage()
            );
        }
    }

    // SEARCH PRODUCT

    private void searchProduct() {

        String id = JOptionPane.showInputDialog(this, "Enter Product ID:");

        if (id == null || id.isBlank()) return;

        try {

            int searchId = Integer.parseInt(id.trim());
            refreshTable();

            for (int i = 0; i < model.getRowCount(); i++) {

                if (Integer.parseInt(model.getValueAt(i, 0).toString()) == searchId) {

                    showPage("Products");

                    if (categoryFilter != null) {
                        categoryFilter.setSelectedItem("All Categories");
                    }

                    if (globalSearch != null) {
                        globalSearch.setText("");
                    }

                    applyFilters();

                    int viewRow = table.convertRowIndexToView(i);

                    if (viewRow >= 0) {
                        table.setRowSelectionInterval(viewRow, viewRow);
                        table.scrollRectToVisible(
                                table.getCellRect(viewRow, 0, true)
                        );
                    }

                    JOptionPane.showMessageDialog(
                            this, "Product Found Successfully!"
                    );
                    return;
                }
            }

            JOptionPane.showMessageDialog(this, "Product Not Found!");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid Product ID!");
        }
    }

    // UPDATE PRODUCT

    private void updateProduct() {

        String id = JOptionPane.showInputDialog(
                this, "Enter Product ID to Update:"
        );

        if (id == null || id.isBlank()) return;

        try {

            int updateId = Integer.parseInt(id.trim());
            refreshTable();

            for (int i = 0; i < model.getRowCount(); i++) {

                if (Integer.parseInt(model.getValueAt(i, 0).toString()) == updateId) {

                    JTextField name = new JTextField(model.getValueAt(i, 1).toString());
                    JTextField category = new JTextField(model.getValueAt(i, 2).toString());
                    JTextField price = new JTextField(model.getValueAt(i, 3).toString());
                    JTextField quantity = new JTextField(model.getValueAt(i, 4).toString());

                    Object[] fields = {
                            "Product Name:", name,
                            "Category:", category,
                            "Price:", price,
                            "Quantity:", quantity
                    };

                    int result = JOptionPane.showConfirmDialog(
                            this, fields, "Update Product",
                            JOptionPane.OK_CANCEL_OPTION
                    );

                    if (result != JOptionPane.OK_OPTION) return;

                    if (name.getText().isBlank()
                            || category.getText().isBlank()
                            || price.getText().isBlank()
                            || quantity.getText().isBlank()) {
                        throw new IllegalArgumentException();
                    }

                    double newPrice = Double.parseDouble(price.getText().trim());
                    int newQuantity = Integer.parseInt(quantity.getText().trim());

                    if (newPrice < 0 || newQuantity < 0) {
                        throw new IllegalArgumentException();
                    }

                    inventory.updateProduct(
                            updateId,
                            name.getText().trim(),
                            category.getText().trim(),
                            newPrice,
                            newQuantity
                    );

                    inventory.saveToFile("products.dat");
                    refreshTable();

                    JOptionPane.showMessageDialog(
                            this, "Product Updated Successfully!"
                    );
                    return;
                }
            }

            JOptionPane.showMessageDialog(this, "Product Not Found!");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this, "Please enter valid information!"
            );
        }
    }

    // DELETE PRODUCT

    private void deleteProduct() {

        String id = JOptionPane.showInputDialog(
                this, "Enter Product ID to Delete:"
        );

        if (id == null || id.isBlank()) return;

        try {

            int deleteId = Integer.parseInt(id.trim());
            refreshTable();

            for (int i = 0; i < model.getRowCount(); i++) {

                if (Integer.parseInt(model.getValueAt(i, 0).toString()) == deleteId) {

                    int confirm = JOptionPane.showConfirmDialog(
                            this,
                            "Are you sure you want to delete this product?",
                            "Confirm Delete",
                            JOptionPane.YES_NO_OPTION
                    );

                    if (confirm == JOptionPane.YES_OPTION) {

                        inventory.deleteProduct(deleteId);
                        inventory.saveToFile("products.dat");
                        refreshTable();

                        JOptionPane.showMessageDialog(
                                this, "Product Deleted Successfully!"
                        );
                    }

                    return;
                }
            }

            JOptionPane.showMessageDialog(this, "Product Not Found!");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid Product ID!");
        }
    }

    // LOW STOCK ALERT

    private void showLowStock() {

        refreshTable();

        String result = inventory.getLowStockProducts(5);

        JOptionPane.showMessageDialog(
                this, result, "Low Stock Alert",
                JOptionPane.WARNING_MESSAGE
        );
    }

    // MAIN METHOD

    public static void main(String[] args) {

        System.out.println("========== IMAGE TEST ==========");

        System.out.println("PROJECT LOCATION: "
                + System.getProperty("user.dir"));

        File folder = new File(
                System.getProperty("user.dir"), "images"
        );

        System.out.println("IMAGES FOLDER EXISTS: "
                + folder.exists());

        System.out.println("IMAGES FOLDER PATH: "
                + folder.getAbsolutePath());

        if (folder.exists() && folder.isDirectory()) {

            File[] files = folder.listFiles();

            if (files != null) {
                for (File f : files) {
                    System.out.println(
                            "IMAGE FILE: " + f.getName()
                                    + " | EXISTS: " + f.exists()
                    );
                }
            }
        }

        System.out.println("========== TEST COMPLETE ==========");

        SwingUtilities.invokeLater(GroceryGUI::new);
    }
}