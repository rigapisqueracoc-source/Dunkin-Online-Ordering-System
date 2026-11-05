/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package dunkin.gui;
import java.awt.CardLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import dunkin.database.DatabaseConnection;

/**
 *
 * @author ava
 */
public class ManagerFrame extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ManagerFrame.class.getName());

    /**
     * Creates new form ManagerFrame
     */
    public ManagerFrame() {
        initComponents();
        
        setupManagerPanels();
        loadDashboard();
        loadProducts();
        loadDiscounts();
        loadOrders();
        loadSales();
    }
    
    // SET UP MANAGER PANELS
private void setupManagerPanels() {
    pnlContent.setLayout(new CardLayout());

    pnlContent.add(pnlDashboard, "DASHBOARD");
    pnlContent.add(pnlProducts, "PRODUCTS");
    pnlContent.add(pnlDiscounts, "DISCOUNTS");
    pnlContent.add(pnlOrders, "ORDERS");
    pnlContent.add(pnlSales, "SALES");

    showManagerPanel("DASHBOARD");
}

private void showManagerPanel(String name) {
    CardLayout cl = (CardLayout) pnlContent.getLayout();
    cl.show(pnlContent, name);
    pnlContent.revalidate();
    pnlContent.repaint();
}

// DASHBOARD LABELS
private void loadDashboard() {
    try (Connection con = DatabaseConnection.getConnection()) {

        // TOTAL PRODUCTS
        // Products do NOT reset every day.
        String sqlProducts =
                "SELECT COUNT(*) AS total "
              + "FROM products "
              + "WHERE available = 1";

        try (PreparedStatement pst = con.prepareStatement(sqlProducts);
             ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                lblTotalProducts.setText(
                        rs.getString("total"));
            }
        }

        // TOTAL ORDERS TODAY
        String sqlOrders =
                "SELECT COUNT(*) AS total "
              + "FROM orders "
              + "WHERE DATE(order_date) = CURDATE()";

        try (PreparedStatement pst = con.prepareStatement(sqlOrders);
             ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                lblTotalOrders.setText(
                        rs.getString("total"));
            }
        }

        // VERIFIED SALES TODAY
        String sqlSales =
                "SELECT COALESCE(SUM(total_amount), 0) AS total "
              + "FROM orders "
              + "WHERE DATE(order_date) = CURDATE() "
              + "AND payment_status = 'VERIFIED'";

        try (PreparedStatement pst = con.prepareStatement(sqlSales);
             ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                lblTotalSales.setText(
                        "₱" + rs.getDouble("total"));
            }
        }

        // PENDING ORDERS TODAY
        String sqlPending =
                "SELECT COUNT(*) AS total "
              + "FROM orders "
              + "WHERE DATE(order_date) = CURDATE() "
              + "AND order_status = 'PENDING'";

        try (PreparedStatement pst = con.prepareStatement(sqlPending);
             ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                lblPendingOrders.setText(
                        rs.getString("total"));
            }
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this,
                "Dashboard error: " + e.getMessage());
    }
}

// LOAD PRODUCTS TABLE
private void loadProducts() {
    DefaultTableModel model =
        (DefaultTableModel) tblProducts.getModel();
    model.setRowCount(0);

    String sql = "SELECT product_id, product_name, price, stock, "
           + "discount, available FROM products "
           + "WHERE available = 1 ORDER BY product_id";

    try (Connection con = DatabaseConnection.getConnection();
         PreparedStatement pst = con.prepareStatement(sql);
         ResultSet rs = pst.executeQuery()) {

        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getInt("product_id"),
                rs.getString("product_name"),
                rs.getDouble("price"),
                rs.getInt("stock"),
                rs.getDouble("discount"),
                rs.getBoolean("available")
            });
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this,
            "Products error: " + e.getMessage());
    }
}

// ADD PRODUCT
private void addProduct() {
    try {
       String sql = "INSERT INTO products "
           + "(product_name, price, stock, discount, available, category) "
           + "VALUES (?, ?, ?, ?, 1, ?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, txtProductName.getText().trim());
            pst.setDouble(2, Double.parseDouble(txtPrice.getText()));
            pst.setInt(3, Integer.parseInt(txtStock.getText()));
            pst.setDouble(4, Double.parseDouble(txtDiscount.getText()));
            pst.setString(5, cmbCategory.getSelectedItem().toString());

            if (txtProductName.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Enter a product name.");
                return;
            }

            pst.executeUpdate();
        }

        JOptionPane.showMessageDialog(this, "Product added!");
        loadProducts();
loadDashboard();
clearProductFields();

tblProducts.revalidate();
tblProducts.repaint();;

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this,
            "Cannot add product: " + e.getMessage());
    }
}

// UPDATE PRODUCT
private void updateProduct() {
    try {
        String sql = "UPDATE products SET product_name=?, price=?, "
                   + "stock=?, discount=? WHERE product_id=?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, txtProductName.getText().trim());
            pst.setDouble(2, Double.parseDouble(txtPrice.getText()));
            pst.setInt(3, Integer.parseInt(txtStock.getText()));
            pst.setDouble(4, Double.parseDouble(txtDiscount.getText()));
            pst.setInt(5, Integer.parseInt(txtProductId.getText()));

            int rows = pst.executeUpdate();

            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Product updated!");
            } else {
                JOptionPane.showMessageDialog(this, "Product ID not found.");
            }
        }

        loadProducts();
        loadDashboard();

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this,
            "Cannot update product: " + e.getMessage());
    }
}


private void deleteProduct() {
    int row = tblProducts.getSelectedRow();

    if (row == -1) {
        JOptionPane.showMessageDialog(
            this, "Please select a product from the list."
        );
        return;
    }

    int productId = Integer.parseInt(
        tblProducts.getValueAt(row, 0).toString()
    );

    int confirm = JOptionPane.showConfirmDialog(
        this,
        "Permanently delete this product?",
        "Confirm Delete",
        JOptionPane.YES_NO_OPTION
    );

    if (confirm != JOptionPane.YES_OPTION) {
        return;
    }

    String sql = "DELETE FROM products WHERE product_id = ?";

    try (Connection con = DatabaseConnection.getConnection();
         PreparedStatement pst = con.prepareStatement(sql)) {

        pst.setInt(1, productId);

        int deleted = pst.executeUpdate();

        if (deleted > 0) {
            JOptionPane.showMessageDialog(
                this, "Product deleted successfully!"
            );
        }

        loadProducts();
        loadDashboard();

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(
            this,
            "Could not delete product.\n"
            + "It may be linked to previous orders.\n"
            + e.getMessage()
        );
    }
}


// LOAD ORDERS
private void loadOrders() {
    DefaultTableModel model =
        (DefaultTableModel) tblOrders.getModel();
    model.setRowCount(0);

    String sql = "SELECT order_id, customer_id, order_date, "
               + "total_amount, payment_method, payment_status, order_status "
               + "FROM orders ORDER BY order_id DESC";

    try (Connection con = DatabaseConnection.getConnection();
         PreparedStatement pst = con.prepareStatement(sql);
         ResultSet rs = pst.executeQuery()) {

        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getInt("order_id"),
                rs.getInt("customer_id"),
                rs.getTimestamp("order_date"),
                rs.getDouble("total_amount"),
                rs.getString("payment_method"),
                rs.getString("payment_status"),
                rs.getString("order_status")
            });
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this,
            "Orders error: " + e.getMessage());
    }
}

// UPDATE ORDER STATUS
private void updateOrderStatus() {
    try {
        int row = tblOrders.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                "Select an order first.");
            return;
        }

        int orderId = Integer.parseInt(
            tblOrders.getValueAt(row, 0).toString());

        String status = cmbOrderStatus.getSelectedItem().toString();

        String sql = "UPDATE orders SET order_status=? WHERE order_id=?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, status);
            pst.setInt(2, orderId);
            pst.executeUpdate();
        }

        JOptionPane.showMessageDialog(this, "Order status updated!");
        loadOrders();
        loadDashboard();

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this,
            "Status error: " + e.getMessage());
    }
}

// LOAD SALES
// LOAD TODAY'S SALES
private void loadSales() {

    DefaultTableModel model =
            (DefaultTableModel) tblSales.getModel();

    model.setRowCount(0);

    String sql =
            "SELECT order_id, customer_id, order_date, "
          + "total_amount, payment_method, reference_number "
          + "FROM orders "
          + "WHERE DATE(order_date) = CURDATE() "
          + "AND payment_status = 'VERIFIED' "
          + "ORDER BY order_date DESC";

    double total = 0;

    try (Connection con = DatabaseConnection.getConnection();
         PreparedStatement pst = con.prepareStatement(sql);
         ResultSet rs = pst.executeQuery()) {

        while (rs.next()) {

            double amount =
                    rs.getDouble("total_amount");

            total += amount;

            model.addRow(new Object[]{
                rs.getInt("order_id"),
                rs.getInt("customer_id"),
                rs.getTimestamp("order_date"),
                amount,
                rs.getString("payment_method"),
                rs.getString("reference_number")
            });
        }

        lblSalesTotal.setText(
                "Total Verified Sales: ₱" + total);

    } catch (SQLException e) {

        JOptionPane.showMessageDialog(this,
                "Sales error: " + e.getMessage());
    }
}

// LOAD DISCOUNTS
private void loadDiscounts() {
    DefaultTableModel model =
        (DefaultTableModel) tblDiscounts.getModel();
    model.setRowCount(0);

    String sql = "SELECT product_id, product_name, price, discount "
               + "FROM products";

    try (Connection con = DatabaseConnection.getConnection();
         PreparedStatement pst = con.prepareStatement(sql);
         ResultSet rs = pst.executeQuery()) {

        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getInt("product_id"),
                rs.getString("product_name"),
                rs.getDouble("price"),
                rs.getDouble("discount")
            });
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this,
            "Discounts error: " + e.getMessage());
    }
}

// APPLY DISCOUNT TO SELECTED PRODUCT
private void applyDiscount() {
    try {
        int row = tblDiscounts.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                "Select a product first.");
            return;
        }

        int productId = Integer.parseInt(
            tblDiscounts.getValueAt(row, 0).toString());

        double discount =
            Double.parseDouble(lblDIs.getText());

        if (discount < 0 || discount > 100) {
            JOptionPane.showMessageDialog(this,
                "Discount must be between 0 and 100.");
            return;
        }

        String sql = "UPDATE products SET discount=? WHERE product_id=?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setDouble(1, discount);
            pst.setInt(2, productId);
            pst.executeUpdate();
        }

        JOptionPane.showMessageDialog(this, "Discount applied!");
        loadDiscounts();
        loadProducts();

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this,
            "Discount error: " + e.getMessage());
    }
}

// CLEAR PRODUCT FIELDS
private void clearProductFields() {
    txtProductId.setText("");
    txtProductName.setText("");
    txtPrice.setText("");
    txtStock.setText("");
    txtDiscount.setText("");
}

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        pnlHeader = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        pnlSidebar = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        btnDashboard = new javax.swing.JButton();
        btnManageProducts = new javax.swing.JButton();
        btnDiscounts = new javax.swing.JButton();
        btnManageOrders = new javax.swing.JButton();
        btnViewSales = new javax.swing.JButton();
        btnLogout = new javax.swing.JButton();
        pnlContent = new javax.swing.JPanel();
        pnlDashboard = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        lblTotalProducts = new javax.swing.JLabel();
        lblTotalOrders = new javax.swing.JLabel();
        lblTotalSales = new javax.swing.JLabel();
        lblPendingOrders = new javax.swing.JLabel();
        pnlProducts = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        lbl = new javax.swing.JLabel();
        stock = new javax.swing.JLabel();
        lblprice = new javax.swing.JLabel();
        lbldiscount = new javax.swing.JLabel();
        txtProductId = new javax.swing.JTextField();
        txtProductName = new javax.swing.JTextField();
        txtStock = new javax.swing.JTextField();
        txtPrice = new javax.swing.JTextField();
        txtDiscount = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblProducts = new javax.swing.JTable();
        btnAdd = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        jLabel20 = new javax.swing.JLabel();
        cmbCategory = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        pnlDiscounts = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        txtProductID = new javax.swing.JTextField();
        lblDIs = new javax.swing.JLabel();
        txtDiscountPercent = new javax.swing.JTextField();
        btnApplyDiscount = new javax.swing.JButton();
        btnRemoveDiscount = new javax.swing.JButton();
        btnRefreshDiscounts = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jScrollPane4 = new javax.swing.JScrollPane();
        tblDiscounts = new javax.swing.JTable();
        jLabel21 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        pnlOrders = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        btnRefreshOrders = new javax.swing.JButton();
        btnUpdateOrderStatus = new javax.swing.JButton();
        jLabel19 = new javax.swing.JLabel();
        cmbOrderStatus = new javax.swing.JComboBox<>();
        jScrollPane5 = new javax.swing.JScrollPane();
        jScrollPane6 = new javax.swing.JScrollPane();
        tblOrders = new javax.swing.JTable();
        jLabel22 = new javax.swing.JLabel();
        cmbPaymentStatus = new javax.swing.JComboBox<>();
        jLabel9 = new javax.swing.JLabel();
        pnlSales = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        lblSalesTotal = new javax.swing.JLabel();
        btnRefreshSales = new javax.swing.JButton();
        jScrollPane7 = new javax.swing.JScrollPane();
        jScrollPane8 = new javax.swing.JScrollPane();
        tblSales = new javax.swing.JTable();
        jLabel25 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel2.setBackground(new java.awt.Color(255, 153, 204));

        pnlHeader.setBackground(new java.awt.Color(204, 204, 204));

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblTitle.setForeground(new java.awt.Color(214, 105, 45));
        lblTitle.setText("MANAGER DASHBOARD");

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(214, 105, 45));
        jLabel1.setText("ORDERING AND SALES SYSTEM");

        javax.swing.GroupLayout pnlHeaderLayout = new javax.swing.GroupLayout(pnlHeader);
        pnlHeader.setLayout(pnlHeaderLayout);
        pnlHeaderLayout.setHorizontalGroup(
            pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlHeaderLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlHeaderLayout.createSequentialGroup()
                        .addComponent(lblTitle)
                        .addGap(322, 322, 322))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlHeaderLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(356, 356, 356))))
        );
        pnlHeaderLayout.setVerticalGroup(
            pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlHeaderLayout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addComponent(lblTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addGap(0, 16, Short.MAX_VALUE))
        );

        pnlSidebar.setBackground(new java.awt.Color(255, 204, 255));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 51, 204));
        jLabel2.setText("MANAGER MENU");

        btnDashboard.setBackground(new java.awt.Color(214, 105, 45));
        btnDashboard.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnDashboard.setForeground(new java.awt.Color(255, 255, 255));
        btnDashboard.setText("Dashboard");
        btnDashboard.addActionListener(this::btnDashboardActionPerformed);

        btnManageProducts.setBackground(new java.awt.Color(214, 105, 45));
        btnManageProducts.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnManageProducts.setForeground(new java.awt.Color(255, 255, 255));
        btnManageProducts.setText("Manage Products");
        btnManageProducts.addActionListener(this::btnManageProductsActionPerformed);

        btnDiscounts.setBackground(new java.awt.Color(214, 105, 45));
        btnDiscounts.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnDiscounts.setForeground(new java.awt.Color(255, 255, 255));
        btnDiscounts.setText("Product Discounts");
        btnDiscounts.addActionListener(this::btnDiscountsActionPerformed);

        btnManageOrders.setBackground(new java.awt.Color(214, 105, 45));
        btnManageOrders.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnManageOrders.setForeground(new java.awt.Color(255, 255, 255));
        btnManageOrders.setText("Manage Orders");
        btnManageOrders.addActionListener(this::btnManageOrdersActionPerformed);

        btnViewSales.setBackground(new java.awt.Color(214, 105, 45));
        btnViewSales.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnViewSales.setForeground(new java.awt.Color(255, 255, 255));
        btnViewSales.setText("View Sales");
        btnViewSales.addActionListener(this::btnViewSalesActionPerformed);

        btnLogout.setBackground(new java.awt.Color(214, 105, 45));
        btnLogout.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnLogout.setForeground(new java.awt.Color(255, 255, 255));
        btnLogout.setText("Logout");
        btnLogout.addActionListener(this::btnLogoutActionPerformed);

        javax.swing.GroupLayout pnlSidebarLayout = new javax.swing.GroupLayout(pnlSidebar);
        pnlSidebar.setLayout(pnlSidebarLayout);
        pnlSidebarLayout.setHorizontalGroup(
            pnlSidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlSidebarLayout.createSequentialGroup()
                .addContainerGap(99, Short.MAX_VALUE)
                .addGroup(pnlSidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlSidebarLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(95, 95, 95))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlSidebarLayout.createSequentialGroup()
                        .addGroup(pnlSidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(btnManageProducts, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnDashboard, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnDiscounts)
                            .addComponent(btnManageOrders, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnViewSales, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnLogout, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(106, 106, 106))))
        );
        pnlSidebarLayout.setVerticalGroup(
            pnlSidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSidebarLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel2)
                .addGap(24, 24, 24)
                .addComponent(btnDashboard, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnManageProducts, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnDiscounts, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnManageOrders, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnViewSales, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnLogout, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlContent.setBackground(new java.awt.Color(255, 204, 255));

        pnlDashboard.setBackground(new java.awt.Color(204, 204, 204));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(214, 105, 45));
        jLabel3.setText("DASHBOARD");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setText("Total Orders");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel5.setText("Verified Sales");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel6.setText("Total Products");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel7.setText("Pending Orders");

        lblTotalProducts.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblTotalProducts.setText("0");

        lblTotalOrders.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblTotalOrders.setText("0");

        lblTotalSales.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblTotalSales.setText("0");

        lblPendingOrders.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblPendingOrders.setText("0");

        javax.swing.GroupLayout pnlDashboardLayout = new javax.swing.GroupLayout(pnlDashboard);
        pnlDashboard.setLayout(pnlDashboardLayout);
        pnlDashboardLayout.setHorizontalGroup(
            pnlDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDashboardLayout.createSequentialGroup()
                .addGroup(pnlDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlDashboardLayout.createSequentialGroup()
                        .addGap(206, 206, 206)
                        .addGroup(pnlDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(jLabel5))
                        .addGap(210, 210, 210)
                        .addGroup(pnlDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addComponent(jLabel4)))
                    .addGroup(pnlDashboardLayout.createSequentialGroup()
                        .addGap(334, 334, 334)
                        .addComponent(jLabel3)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(pnlDashboardLayout.createSequentialGroup()
                .addGap(225, 225, 225)
                .addComponent(lblTotalSales)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblPendingOrders)
                .addGap(291, 291, 291))
            .addGroup(pnlDashboardLayout.createSequentialGroup()
                .addGap(242, 242, 242)
                .addComponent(lblTotalProducts)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblTotalOrders)
                .addGap(294, 294, 294))
        );
        pnlDashboardLayout.setVerticalGroup(
            pnlDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDashboardLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel3)
                .addGap(49, 49, 49)
                .addGroup(pnlDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel6))
                .addGap(17, 17, 17)
                .addGroup(pnlDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTotalProducts)
                    .addComponent(lblTotalOrders))
                .addGap(98, 98, 98)
                .addGroup(pnlDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jLabel7))
                .addGap(26, 26, 26)
                .addGroup(pnlDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTotalSales)
                    .addComponent(lblPendingOrders))
                .addContainerGap(43, Short.MAX_VALUE))
        );

        pnlProducts.setBackground(new java.awt.Color(204, 204, 204));

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(214, 105, 45));
        jLabel12.setText("MANAGE PRODUCTS");

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel16.setText("Product ID");

        lbl.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lbl.setText(" Product Name");

        stock.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        stock.setText("Stock");

        lblprice.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblprice.setText("Price (₱)");

        lbldiscount.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lbldiscount.setText("Discount (%)");

        txtProductName.addActionListener(this::txtProductNameActionPerformed);

        tblProducts.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Product ID", "Product Name", "Price", "Stock", "Discount", "Available"
            }
        ));
        jScrollPane2.setViewportView(tblProducts);

        jScrollPane1.setViewportView(jScrollPane2);

        btnAdd.setBackground(new java.awt.Color(214, 105, 45));
        btnAdd.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnAdd.setForeground(new java.awt.Color(255, 255, 255));
        btnAdd.setText("ADD PRODUCT");
        btnAdd.addActionListener(this::btnAddActionPerformed);

        btnUpdate.setBackground(new java.awt.Color(214, 105, 45));
        btnUpdate.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnUpdate.setForeground(new java.awt.Color(255, 255, 255));
        btnUpdate.setText("UPDATE");
        btnUpdate.addActionListener(this::btnUpdateActionPerformed);

        btnDelete.setBackground(new java.awt.Color(214, 105, 45));
        btnDelete.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnDelete.setForeground(new java.awt.Color(255, 255, 255));
        btnDelete.setText("DELETE");
        btnDelete.addActionListener(this::btnDeleteActionPerformed);

        btnClear.setBackground(new java.awt.Color(214, 105, 45));
        btnClear.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnClear.setForeground(new java.awt.Color(255, 255, 255));
        btnClear.setText("CLEAR");
        btnClear.addActionListener(this::btnClearActionPerformed);

        jLabel20.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel20.setText("Product List");

        cmbCategory.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "DONUT", "MUNCHKINS", "COFFEE", "DRINKS" }));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel8.setText("Category");

        javax.swing.GroupLayout pnlProductsLayout = new javax.swing.GroupLayout(pnlProducts);
        pnlProducts.setLayout(pnlProductsLayout);
        pnlProductsLayout.setHorizontalGroup(
            pnlProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProductsLayout.createSequentialGroup()
                .addGap(284, 284, 284)
                .addComponent(jLabel12)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(pnlProductsLayout.createSequentialGroup()
                .addContainerGap(26, Short.MAX_VALUE)
                .addGroup(pnlProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlProductsLayout.createSequentialGroup()
                        .addGroup(pnlProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(pnlProductsLayout.createSequentialGroup()
                                .addComponent(btnAdd)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(52, 52, 52)
                                .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(54, 54, 54)
                                .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(pnlProductsLayout.createSequentialGroup()
                                .addGroup(pnlProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtProductId, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel16)
                                    .addComponent(lblprice))
                                .addGap(94, 94, 94)
                                .addGroup(pnlProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtProductName, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lbl, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lbldiscount)
                                    .addComponent(txtDiscount, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(pnlProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(stock)
                                    .addComponent(txtStock)
                                    .addComponent(cmbCategory, 0, 154, Short.MAX_VALUE)
                                    .addComponent(jLabel8))))
                        .addGap(208, 208, 208))
                    .addGroup(pnlProductsLayout.createSequentialGroup()
                        .addGroup(pnlProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel20)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 788, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 33, Short.MAX_VALUE))))
        );
        pnlProductsLayout.setVerticalGroup(
            pnlProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProductsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel12)
                .addGroup(pnlProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlProductsLayout.createSequentialGroup()
                        .addGap(53, 53, 53)
                        .addGroup(pnlProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel16)
                            .addComponent(stock)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlProductsLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lbl)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtProductName, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtProductId, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtStock, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)))
                .addGap(18, 18, 18)
                .addGroup(pnlProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbldiscount)
                    .addComponent(lblprice)
                    .addComponent(jLabel8))
                .addGap(28, 28, 28)
                .addGroup(pnlProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDiscount, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(42, 42, 42)
                .addGroup(pnlProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel20)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pnlDiscounts.setBackground(new java.awt.Color(204, 204, 204));

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(214, 105, 45));
        jLabel13.setText("PRODUCT DISCOUNTS");

        jLabel17.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel17.setText("Select a product and set its discount.");

        jLabel18.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel18.setText("Selected Product");

        txtProductID.addActionListener(this::txtProductIDActionPerformed);

        lblDIs.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblDIs.setText("Discount Percentage (%)");

        btnApplyDiscount.setBackground(new java.awt.Color(214, 105, 45));
        btnApplyDiscount.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnApplyDiscount.setForeground(new java.awt.Color(255, 255, 255));
        btnApplyDiscount.setText("APPLY DISCOUNT");
        btnApplyDiscount.addActionListener(this::btnApplyDiscountActionPerformed);

        btnRemoveDiscount.setBackground(new java.awt.Color(214, 105, 45));
        btnRemoveDiscount.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnRemoveDiscount.setForeground(new java.awt.Color(255, 255, 255));
        btnRemoveDiscount.setText("REMOVE DISCOUNT");
        btnRemoveDiscount.addActionListener(this::btnRemoveDiscountActionPerformed);

        btnRefreshDiscounts.setBackground(new java.awt.Color(214, 105, 45));
        btnRefreshDiscounts.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnRefreshDiscounts.setForeground(new java.awt.Color(255, 255, 255));
        btnRefreshDiscounts.setText("REFRESH");
        btnRefreshDiscounts.addActionListener(this::btnRefreshDiscountsActionPerformed);

        tblDiscounts.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Product ID", "Product Name", "Final Price", "Discount"
            }
        ));
        jScrollPane4.setViewportView(tblDiscounts);

        jScrollPane3.setViewportView(jScrollPane4);

        jLabel21.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel21.setText("Products and Discounts");

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel10.setText("Product ID");

        javax.swing.GroupLayout pnlDiscountsLayout = new javax.swing.GroupLayout(pnlDiscounts);
        pnlDiscounts.setLayout(pnlDiscountsLayout);
        pnlDiscountsLayout.setHorizontalGroup(
            pnlDiscountsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDiscountsLayout.createSequentialGroup()
                .addGroup(pnlDiscountsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlDiscountsLayout.createSequentialGroup()
                        .addGap(285, 285, 285)
                        .addComponent(jLabel13))
                    .addGroup(pnlDiscountsLayout.createSequentialGroup()
                        .addGap(90, 90, 90)
                        .addGroup(pnlDiscountsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10)
                            .addGroup(pnlDiscountsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(lblDIs)
                                .addComponent(jLabel18)
                                .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(txtProductID)
                                .addComponent(txtDiscountPercent)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlDiscountsLayout.createSequentialGroup()
                .addGap(0, 77, Short.MAX_VALUE)
                .addGroup(pnlDiscountsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel21)
                    .addGroup(pnlDiscountsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlDiscountsLayout.createSequentialGroup()
                            .addComponent(btnApplyDiscount)
                            .addGap(58, 58, 58)
                            .addComponent(btnRemoveDiscount)
                            .addGap(61, 61, 61)
                            .addComponent(btnRefreshDiscounts, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(176, 176, 176))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlDiscountsLayout.createSequentialGroup()
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 697, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(73, 73, 73)))))
        );
        pnlDiscountsLayout.setVerticalGroup(
            pnlDiscountsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDiscountsLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel18)
                .addGap(31, 31, 31)
                .addComponent(jLabel10)
                .addGap(18, 18, 18)
                .addComponent(txtProductID, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblDIs)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtDiscountPercent, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlDiscountsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnApplyDiscount, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRemoveDiscount, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRefreshDiscounts, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel21)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(11, Short.MAX_VALUE))
        );

        pnlOrders.setBackground(new java.awt.Color(204, 204, 204));

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(214, 105, 45));
        jLabel14.setText("MANAGE ORDERS");

        btnRefreshOrders.setBackground(new java.awt.Color(214, 105, 45));
        btnRefreshOrders.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnRefreshOrders.setForeground(new java.awt.Color(255, 255, 255));
        btnRefreshOrders.setText("REFRESH ORDERS");
        btnRefreshOrders.addActionListener(this::btnRefreshOrdersActionPerformed);

        btnUpdateOrderStatus.setBackground(new java.awt.Color(214, 105, 45));
        btnUpdateOrderStatus.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnUpdateOrderStatus.setForeground(new java.awt.Color(255, 255, 255));
        btnUpdateOrderStatus.setText("UPDATE STATUS");
        btnUpdateOrderStatus.addActionListener(this::btnUpdateOrderStatusActionPerformed);

        jLabel19.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel19.setText("Order Status");

        cmbOrderStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "PENDING", "ACCEPTED", "PREPARING", "READY", "COMPLETED" }));

        tblOrders.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Order ID", "Customer ID", "Order Date", "Total Amount", "Payment Method", "Payment Status", "Order Status"
            }
        ));
        jScrollPane6.setViewportView(tblOrders);

        jScrollPane5.setViewportView(jScrollPane6);

        jLabel22.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel22.setText("Customer Orders");

        cmbPaymentStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "PENDING", "ACCEPTED", "PREPARING", "READY", "COMPLETED", " " }));

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel9.setText("Payment Status");

        javax.swing.GroupLayout pnlOrdersLayout = new javax.swing.GroupLayout(pnlOrders);
        pnlOrders.setLayout(pnlOrdersLayout);
        pnlOrdersLayout.setHorizontalGroup(
            pnlOrdersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlOrdersLayout.createSequentialGroup()
                .addGap(146, 146, 146)
                .addGroup(pnlOrdersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnRefreshOrders, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbOrderStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel19))
                .addGap(35, 35, 35)
                .addGroup(pnlOrdersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9)
                    .addComponent(jLabel14)
                    .addGroup(pnlOrdersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(cmbPaymentStatus, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnUpdateOrderStatus, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlOrdersLayout.createSequentialGroup()
                .addContainerGap(47, Short.MAX_VALUE)
                .addGroup(pnlOrdersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel22)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 763, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(37, 37, 37))
        );
        pnlOrdersLayout.setVerticalGroup(
            pnlOrdersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlOrdersLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel14)
                .addGap(18, 18, 18)
                .addGroup(pnlOrdersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRefreshOrders, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnUpdateOrderStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlOrdersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlOrdersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(cmbPaymentStatus)
                    .addComponent(cmbOrderStatus, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE))
                .addGap(29, 29, 29)
                .addComponent(jLabel22)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(17, Short.MAX_VALUE))
        );

        pnlSales.setBackground(new java.awt.Color(204, 204, 204));

        jLabel15.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(214, 105, 45));
        jLabel15.setText("SALES REPORT");

        jLabel23.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel23.setText("TOTAL VERIFIED SALES");

        lblSalesTotal.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblSalesTotal.setText("₱0.00");

        btnRefreshSales.setBackground(new java.awt.Color(214, 105, 45));
        btnRefreshSales.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnRefreshSales.setForeground(new java.awt.Color(255, 255, 255));
        btnRefreshSales.setText("REFRESH SALES");
        btnRefreshSales.addActionListener(this::btnRefreshSalesActionPerformed);

        tblSales.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Order ID", "Customer", "Date", "total", "Payment Method"
            }
        ));
        jScrollPane8.setViewportView(tblSales);

        jScrollPane7.setViewportView(jScrollPane8);

        jLabel25.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel25.setText("Sales Transaction");

        javax.swing.GroupLayout pnlSalesLayout = new javax.swing.GroupLayout(pnlSales);
        pnlSales.setLayout(pnlSalesLayout);
        pnlSalesLayout.setHorizontalGroup(
            pnlSalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlSalesLayout.createSequentialGroup()
                .addGap(0, 25, Short.MAX_VALUE)
                .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 799, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(23, 23, 23))
            .addGroup(pnlSalesLayout.createSequentialGroup()
                .addGroup(pnlSalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlSalesLayout.createSequentialGroup()
                        .addGap(339, 339, 339)
                        .addComponent(jLabel15))
                    .addGroup(pnlSalesLayout.createSequentialGroup()
                        .addGroup(pnlSalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlSalesLayout.createSequentialGroup()
                                .addGap(195, 195, 195)
                                .addComponent(jLabel23)
                                .addGap(104, 104, 104))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlSalesLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(lblSalesTotal)
                                .addGap(162, 162, 162)))
                        .addComponent(btnRefreshSales))
                    .addGroup(pnlSalesLayout.createSequentialGroup()
                        .addGap(33, 33, 33)
                        .addComponent(jLabel25)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlSalesLayout.setVerticalGroup(
            pnlSalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSalesLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel15)
                .addGap(38, 38, 38)
                .addGroup(pnlSalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlSalesLayout.createSequentialGroup()
                        .addComponent(jLabel23)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblSalesTotal))
                    .addComponent(btnRefreshSales, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(22, 22, 22)
                .addComponent(jLabel25)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlContentLayout = new javax.swing.GroupLayout(pnlContent);
        pnlContent.setLayout(pnlContentLayout);
        pnlContentLayout.setHorizontalGroup(
            pnlContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlContentLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(pnlOrders, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlDashboard, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlSales, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlProducts, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlDiscounts, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(163, 163, 163))
        );
        pnlContentLayout.setVerticalGroup(
            pnlContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlContentLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlDashboard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlProducts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlDiscounts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlOrders, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlSales, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(168, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(pnlHeader, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(pnlSidebar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(pnlContent, javax.swing.GroupLayout.PREFERRED_SIZE, 862, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlHeader, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(pnlContent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlSidebar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnViewSalesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnViewSalesActionPerformed
        showManagerPanel("SALES");
loadSales();
    }//GEN-LAST:event_btnViewSalesActionPerformed

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
        int confirm = javax.swing.JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to log out?",
            "Logout",
            javax.swing.JOptionPane.YES_NO_OPTION
        );

        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
            LoginFrame login = new LoginFrame();
            login.setVisible(true);
            this.dispose();
        }
    }//GEN-LAST:event_btnLogoutActionPerformed

    private void btnManageOrdersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnManageOrdersActionPerformed
        showManagerPanel("ORDERS");
loadOrders();
    }//GEN-LAST:event_btnManageOrdersActionPerformed

    private void txtProductNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtProductNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtProductNameActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        addProduct();
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnDashboardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDashboardActionPerformed
        showManagerPanel("DASHBOARD");
loadDashboard();
    }//GEN-LAST:event_btnDashboardActionPerformed

    private void btnManageProductsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnManageProductsActionPerformed
        showManagerPanel("PRODUCTS");
loadProducts();
    }//GEN-LAST:event_btnManageProductsActionPerformed

    private void btnDiscountsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDiscountsActionPerformed
        showManagerPanel("DISCOUNTS");
loadDiscounts();
    }//GEN-LAST:event_btnDiscountsActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        updateProduct();
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        deleteProduct();
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        clearProductFields();
    }//GEN-LAST:event_btnClearActionPerformed

    private void btnApplyDiscountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnApplyDiscountActionPerformed
        
try {
    int productId = Integer.parseInt(
            txtProductID.getText().trim());

    String discountText = txtDiscountPercent.getText().trim();
    double discount = Double.parseDouble(discountText);

    if (discount < 0 || discount > 100) {
        JOptionPane.showMessageDialog(this,
                "Discount must be between 0 and 100%.");
        return;
    }

    String sql = "UPDATE products SET discount = ? WHERE product_id = ?";

    try (java.sql.Connection con =
            dunkin.database.DatabaseConnection.getConnection();
         java.sql.PreparedStatement pst = con.prepareStatement(sql)) {

        pst.setDouble(1, discount);
        pst.setInt(2, productId);

        int updated = pst.executeUpdate();

        if (updated > 0) {
            JOptionPane.showMessageDialog(this,
                    "Discount applied successfully!");
            loadProducts();
            loadDashboard();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Product ID not found.");
        }
    }

} catch (NumberFormatException e) {
    JOptionPane.showMessageDialog(this,
            "Enter a valid Product ID and discount percentage.");
} catch (Exception e) {
    JOptionPane.showMessageDialog(this,
            "Unable to apply discount: " + e.getMessage());
}


    }//GEN-LAST:event_btnApplyDiscountActionPerformed

    private void btnRemoveDiscountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveDiscountActionPerformed
        
    int row = tblDiscounts.getSelectedRow();

    if (row == -1) {
        JOptionPane.showMessageDialog(this,
            "Please select a product first.");
        return;
    }

    int productId = Integer.parseInt(
        tblDiscounts.getValueAt(row, 0).toString());

    String sql = "UPDATE products SET discount=0 WHERE product_id=?";

    try (Connection con = DatabaseConnection.getConnection();
         PreparedStatement pst = con.prepareStatement(sql)) {

        pst.setInt(1, productId);
        pst.executeUpdate();

        JOptionPane.showMessageDialog(this,
            "Discount removed successfully!");

        loadDiscounts();
        loadProducts();

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this,
            "Error removing discount: " + e.getMessage());
    }
    }//GEN-LAST:event_btnRemoveDiscountActionPerformed

    private void btnRefreshDiscountsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshDiscountsActionPerformed
        loadDiscounts();
    }//GEN-LAST:event_btnRefreshDiscountsActionPerformed

    private void btnRefreshOrdersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshOrdersActionPerformed
        loadOrders();
    }//GEN-LAST:event_btnRefreshOrdersActionPerformed

    private void btnUpdateOrderStatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateOrderStatusActionPerformed
        

    int selectedRow = tblOrders.getSelectedRow();

    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this,
                "Please select an order from the table first.");
        return;
    }

    try {
        int orderId = Integer.parseInt(
                tblOrders.getValueAt(selectedRow, 0).toString());

        String orderStatus = cmbOrderStatus.getSelectedItem().toString();
        String paymentStatus = cmbPaymentStatus.getSelectedItem().toString();

        String sql = "UPDATE orders SET order_status = ?, "
                   + "payment_status = ? WHERE order_id = ?";

        try (java.sql.Connection con =
                dunkin.database.DatabaseConnection.getConnection();
             java.sql.PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, orderStatus);
            pst.setString(2, paymentStatus);
            pst.setInt(3, orderId);

            int updated = pst.executeUpdate();

            if (updated > 0) {
                JOptionPane.showMessageDialog(this,
                        "Order and payment statuses updated successfully!");
                loadOrders();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Order not found.");
            }
        }

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this,
                "Error updating status: " + e.getMessage());
    }

    }//GEN-LAST:event_btnUpdateOrderStatusActionPerformed

    private void btnRefreshSalesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshSalesActionPerformed
        loadSales();
        loadDashboard();
    }//GEN-LAST:event_btnRefreshSalesActionPerformed

    private void txtProductIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtProductIDActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtProductIDActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new ManagerFrame().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnApplyDiscount;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnDashboard;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnDiscounts;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnManageOrders;
    private javax.swing.JButton btnManageProducts;
    private javax.swing.JButton btnRefreshDiscounts;
    private javax.swing.JButton btnRefreshOrders;
    private javax.swing.JButton btnRefreshSales;
    private javax.swing.JButton btnRemoveDiscount;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JButton btnUpdateOrderStatus;
    private javax.swing.JButton btnViewSales;
    private javax.swing.JComboBox<String> cmbCategory;
    private javax.swing.JComboBox<String> cmbOrderStatus;
    private javax.swing.JComboBox<String> cmbPaymentStatus;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JLabel lbl;
    private javax.swing.JLabel lblDIs;
    private javax.swing.JLabel lblPendingOrders;
    private javax.swing.JLabel lblSalesTotal;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblTotalOrders;
    private javax.swing.JLabel lblTotalProducts;
    private javax.swing.JLabel lblTotalSales;
    private javax.swing.JLabel lbldiscount;
    private javax.swing.JLabel lblprice;
    private javax.swing.JPanel pnlContent;
    private javax.swing.JPanel pnlDashboard;
    private javax.swing.JPanel pnlDiscounts;
    private javax.swing.JPanel pnlHeader;
    private javax.swing.JPanel pnlOrders;
    private javax.swing.JPanel pnlProducts;
    private javax.swing.JPanel pnlSales;
    private javax.swing.JPanel pnlSidebar;
    private javax.swing.JLabel stock;
    private javax.swing.JTable tblDiscounts;
    private javax.swing.JTable tblOrders;
    private javax.swing.JTable tblProducts;
    private javax.swing.JTable tblSales;
    private javax.swing.JTextField txtDiscount;
    private javax.swing.JTextField txtDiscountPercent;
    private javax.swing.JTextField txtPrice;
    private javax.swing.JTextField txtProductID;
    private javax.swing.JTextField txtProductId;
    private javax.swing.JTextField txtProductName;
    private javax.swing.JTextField txtStock;
    // End of variables declaration//GEN-END:variables
}
