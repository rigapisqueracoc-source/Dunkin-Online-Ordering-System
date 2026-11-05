/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package dunkin.gui;
import javax.swing.JButton;
import dunkin.database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.util.HashMap;
import java.util.Map;
/**
 *
 * @author ava
 */
public class CustomerFrame extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(CustomerFrame.class.getName());

    /**
     * Creates new form CustomerFrame
     */
    public CustomerFrame() {
        initComponents();
        btnCancelOrder.addActionListener(this::btnCancelOrderActionPerformed);
    updateCartCount();
    styleCustomerFrame();
    showMenuView();
    showProducts("DONUT");
    
    lblTotalCartItem.setText(
        "Total cart item: " + OrderData.getTotalItems()
    );
    }
    
    
private void styleCustomerFrame() {

    // Sidebar background
    pnlsidebar.setBackground(new java.awt.Color(255, 240, 250));

    // Navigation buttons
    styleNavButton(btnHome);
    styleNavButton(btnMenu);
    styleNavButton(btnMyOrders);
    styleNavButton(btnMyCart);
    styleNavButton(btnLogout);
}

private void styleNavButton(javax.swing.JButton button) {

    button.setBackground(new java.awt.Color(255, 85, 190));
    button.setForeground(java.awt.Color.WHITE);

    button.setFont(
        new java.awt.Font("Arial", java.awt.Font.BOLD, 12)
    );

    button.setFocusPainted(false);
    button.setBorderPainted(false);
    button.setOpaque(true);
}

    
private String selectedProduct = "";
private double selectedPrice = 0.0; 


    


private void showProducts(String category) {
    pnlProducts.removeAll();
    pnlProducts.setLayout(new java.awt.GridLayout(0, 3, 10, 10));
    pnlProducts.setBackground(java.awt.Color.WHITE);

    String sql = "SELECT product_name, price, discount, stock, available "
               + "FROM products WHERE category = ? "
               + "ORDER BY product_id";

    try (
        java.sql.Connection con =
            dunkin.database.DatabaseConnection.getConnection();
        java.sql.PreparedStatement pst = con.prepareStatement(sql)
    ) {
        pst.setString(1, category);

        try (java.sql.ResultSet rs = pst.executeQuery()) {
            boolean found = false;

            while (rs.next()) {
                found = true;

                String name = rs.getString("product_name");
                double price = rs.getDouble("price");
                double discount = rs.getDouble("discount");
                int stock = rs.getInt("stock");
                boolean available = rs.getBoolean("available");

                double finalPrice = price - (price * discount / 100.0);
                boolean canOrder = available && stock > 0;

                javax.swing.JPanel card = new javax.swing.JPanel();
                card.setLayout(new javax.swing.BoxLayout(
                    card, javax.swing.BoxLayout.Y_AXIS
                ));
                card.setBorder(
                    javax.swing.BorderFactory.createCompoundBorder(
                        javax.swing.BorderFactory.createLineBorder(
                            new java.awt.Color(220, 220, 220)
                        ),
                        javax.swing.BorderFactory.createEmptyBorder(
                            12, 12, 12, 12
                        )
                    )
                );

                card.setBackground(
                    canOrder
                        ? java.awt.Color.WHITE
                        : new java.awt.Color(230, 230, 230)
                );

                javax.swing.JLabel lblName =
                    new javax.swing.JLabel(name);
                lblName.setFont(
                    new java.awt.Font("Arial", java.awt.Font.BOLD, 14)
                );

                javax.swing.JLabel lblPrice;

                if (discount > 0) {
                    lblPrice = new javax.swing.JLabel(
                        String.format(
                            "<html><strike>₱%.2f</strike> "
                            + "<font color='red'>₱%.2f "
                            + "(%.0f%% OFF)</font></html>",
                            price, finalPrice, discount
                        )
                    );
                } else {
                    lblPrice = new javax.swing.JLabel(
                        String.format("₱%.2f", price)
                    );
                }

                javax.swing.JLabel lblStock =
                    new javax.swing.JLabel(
                        canOrder ? "Stock: " + stock : "UNAVAILABLE"
                    );

                javax.swing.JButton btnSelect =
                    new javax.swing.JButton("SELECT PRODUCT");

                btnSelect.setEnabled(canOrder);

                if (!canOrder) {
                    lblStock.setForeground(java.awt.Color.RED);
                }

                btnSelect.addActionListener(e -> {
                    selectedProduct = name;
                    selectedPrice = finalPrice;

                    javax.swing.JOptionPane.showMessageDialog(
                        this,
                        name + " selected.\nPrice: ₱"
                        + String.format("%.2f", finalPrice)
                    );
                });

                card.add(lblName);
                card.add(javax.swing.Box.createVerticalStrut(8));
                card.add(lblPrice);
                card.add(javax.swing.Box.createVerticalStrut(5));
                card.add(lblStock);
                card.add(javax.swing.Box.createVerticalStrut(8));
                card.add(btnSelect);

                pnlProducts.add(card);
            }

            if (!found) {
                pnlProducts.add(
                    new javax.swing.JLabel(
                        "No products found in " + category + "."
                    )
                );
            }
        }

    } catch (java.sql.SQLException e) {
        javax.swing.JOptionPane.showMessageDialog(
            this,
            "Unable to load products: " + e.getMessage()
        );
    }

    pnlProducts.revalidate();
    pnlProducts.repaint();
}



private boolean isProductAvailable(String productName) {
    String sql = "SELECT available, stock FROM products "
               + "WHERE product_name = ?";

    try (Connection con =
             dunkin.database.DatabaseConnection.getConnection();
         PreparedStatement pst = con.prepareStatement(sql)) {

        pst.setString(1, productName);

        try (ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("available") == 1
                    && rs.getInt("stock") > 0;
            }
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(
            this,
            "Database error: " + e.getMessage()
        );
    }

    return false;
}


private void loadMyOrders() {
    DefaultTableModel model =
        (DefaultTableModel) tblMyOrders.getModel();

    model.setRowCount(0);

    String sql = "SELECT order_id, order_date, subtotal, " +
                 "discount_amount, total_amount, payment_method, " +
                 "payment_status, order_status " +
                 "FROM orders WHERE customer_id = ? " +
                 "ORDER BY order_date DESC";

    try (Connection con =
             dunkin.database.DatabaseConnection.getConnection();
         PreparedStatement pst = con.prepareStatement(sql)) {

        pst.setInt(1, UserSession.userId);

        try (ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("order_id"),
                    rs.getTimestamp("order_date"),
                    rs.getBigDecimal("subtotal"),
                    rs.getBigDecimal("discount_amount"),
                    rs.getBigDecimal("total_amount"),
                    rs.getString("payment_method"),
                    rs.getString("payment_status"),
                    rs.getString("order_status")
                });
            }
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(
            this,
            "Unable to load your orders: " + e.getMessage()
        );
    }
}





private void updateCartCount() {
    lblTotalCartItem.setText(
        "Total cart item: " + OrderData.getTotalItems()
    );
}

private void showMenuView() {
    pnlProducts.setVisible(true);
    pnlMyOrders.setVisible(false);

    pnlProducts.revalidate();
    pnlProducts.repaint();
}

private void showMyOrdersView() {
    pnlProducts.setVisible(false);
    pnlMyOrders.setVisible(true);

    pnlMyOrders.revalidate();
    pnlMyOrders.repaint();
}



    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        pnlProducts = new javax.swing.JPanel();
        btnDonut = new javax.swing.JButton();
        btnMunchkins = new javax.swing.JButton();
        btnCoffee = new javax.swing.JButton();
        btnViewCart = new javax.swing.JButton();
        btnAddToCart = new javax.swing.JButton();
        lblTotalCartItem = new javax.swing.JLabel();
        pnlsidebar = new javax.swing.JPanel();
        btnLogout = new javax.swing.JButton();
        btnHome = new javax.swing.JButton();
        btnMenu = new javax.swing.JButton();
        btnMyOrders = new javax.swing.JButton();
        btnMyCart = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        btnDrinks = new javax.swing.JButton();
        pnlMyOrders = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblMyOrders = new javax.swing.JTable();
        btnCancelOrder = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 153, 255));

        pnlProducts.setBackground(new java.awt.Color(255, 204, 255));

        javax.swing.GroupLayout pnlProductsLayout = new javax.swing.GroupLayout(pnlProducts);
        pnlProducts.setLayout(pnlProductsLayout);
        pnlProductsLayout.setHorizontalGroup(
            pnlProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        pnlProductsLayout.setVerticalGroup(
            pnlProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 203, Short.MAX_VALUE)
        );

        btnDonut.setBackground(new java.awt.Color(214, 105, 45));
        btnDonut.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnDonut.setForeground(new java.awt.Color(255, 255, 255));
        btnDonut.setText("DONUT");
        btnDonut.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnDonut.addActionListener(this::btnDonutActionPerformed);

        btnMunchkins.setBackground(new java.awt.Color(214, 105, 45));
        btnMunchkins.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnMunchkins.setForeground(new java.awt.Color(255, 255, 255));
        btnMunchkins.setText("MUNCHKINS");
        btnMunchkins.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnMunchkins.addActionListener(this::btnMunchkinsActionPerformed);

        btnCoffee.setBackground(new java.awt.Color(214, 105, 45));
        btnCoffee.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnCoffee.setForeground(new java.awt.Color(255, 255, 255));
        btnCoffee.setText("COFFEE");
        btnCoffee.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnCoffee.addActionListener(this::btnCoffeeActionPerformed);

        btnViewCart.setBackground(new java.awt.Color(214, 105, 45));
        btnViewCart.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnViewCart.setForeground(new java.awt.Color(255, 255, 255));
        btnViewCart.setText("VIEW CART");
        btnViewCart.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnViewCart.addActionListener(this::btnViewCartActionPerformed);

        btnAddToCart.setBackground(new java.awt.Color(214, 105, 45));
        btnAddToCart.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnAddToCart.setForeground(new java.awt.Color(255, 255, 255));
        btnAddToCart.setText("ADD TO CART");
        btnAddToCart.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnAddToCart.addActionListener(this::btnAddToCartActionPerformed);

        lblTotalCartItem.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTotalCartItem.setText("Total Cart Items: 0");

        pnlsidebar.setBackground(new java.awt.Color(255, 204, 255));

        btnLogout.setBackground(new java.awt.Color(255, 102, 204));
        btnLogout.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnLogout.setForeground(new java.awt.Color(255, 255, 255));
        btnLogout.setText("LOGOUT");
        btnLogout.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnLogout.addActionListener(this::btnLogoutActionPerformed);

        btnHome.setBackground(new java.awt.Color(255, 102, 204));
        btnHome.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnHome.setForeground(new java.awt.Color(255, 255, 255));
        btnHome.setText("HOME");
        btnHome.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnHome.addActionListener(this::btnHomeActionPerformed);

        btnMenu.setBackground(new java.awt.Color(255, 102, 204));
        btnMenu.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnMenu.setForeground(new java.awt.Color(255, 255, 255));
        btnMenu.setText("MENU");
        btnMenu.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnMenu.addActionListener(this::btnMenuActionPerformed);

        btnMyOrders.setBackground(new java.awt.Color(255, 102, 204));
        btnMyOrders.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnMyOrders.setForeground(new java.awt.Color(255, 255, 255));
        btnMyOrders.setText("MY ORDERS");
        btnMyOrders.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnMyOrders.addActionListener(this::btnMyOrdersActionPerformed);

        btnMyCart.setBackground(new java.awt.Color(255, 102, 204));
        btnMyCart.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnMyCart.setForeground(new java.awt.Color(255, 255, 255));
        btnMyCart.setText("MY CART");
        btnMyCart.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnMyCart.addActionListener(this::btnMyCartActionPerformed);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 32)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(214, 105, 45));
        jLabel1.setText("DUNKIN'");

        javax.swing.GroupLayout pnlsidebarLayout = new javax.swing.GroupLayout(pnlsidebar);
        pnlsidebar.setLayout(pnlsidebarLayout);
        pnlsidebarLayout.setHorizontalGroup(
            pnlsidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlsidebarLayout.createSequentialGroup()
                .addGroup(pnlsidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlsidebarLayout.createSequentialGroup()
                        .addGap(70, 70, 70)
                        .addGroup(pnlsidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnLogout, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnMyCart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnMyOrders, javax.swing.GroupLayout.DEFAULT_SIZE, 104, Short.MAX_VALUE)
                            .addComponent(btnMenu, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnHome, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(pnlsidebarLayout.createSequentialGroup()
                        .addGap(55, 55, 55)
                        .addComponent(jLabel1)))
                .addContainerGap(59, Short.MAX_VALUE))
        );
        pnlsidebarLayout.setVerticalGroup(
            pnlsidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlsidebarLayout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(btnHome, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(37, 37, 37)
                .addComponent(btnMenu, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addComponent(btnMyOrders, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29)
                .addComponent(btnMyCart, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(39, 39, 39)
                .addComponent(btnLogout, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(214, 105, 45));
        jLabel3.setText("WELCOME, CUSTOMER!");

        btnDrinks.setBackground(new java.awt.Color(214, 105, 45));
        btnDrinks.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnDrinks.setForeground(new java.awt.Color(255, 255, 255));
        btnDrinks.setText("DRINKS");
        btnDrinks.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnDrinks.addActionListener(this::btnDrinksActionPerformed);

        pnlMyOrders.setBackground(new java.awt.Color(255, 204, 255));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(214, 105, 45));
        jLabel2.setText("MY ORDERS");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(214, 105, 45));
        jLabel4.setText("View your order history and status");

        tblMyOrders.setBackground(java.awt.Color.lightGray);
        tblMyOrders.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Order ID", "Order Date", "Subtotal", "DIscount Amount ", "Total Amount", "Payment Method", "Payment Status", "Order Status"
            }
        ));
        jScrollPane1.setViewportView(tblMyOrders);
        if (tblMyOrders.getColumnModel().getColumnCount() > 0) {
            tblMyOrders.getColumnModel().getColumn(0).setResizable(false);
        }

        javax.swing.GroupLayout pnlMyOrdersLayout = new javax.swing.GroupLayout(pnlMyOrders);
        pnlMyOrders.setLayout(pnlMyOrdersLayout);
        pnlMyOrdersLayout.setHorizontalGroup(
            pnlMyOrdersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMyOrdersLayout.createSequentialGroup()
                .addGroup(pnlMyOrdersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMyOrdersLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1))
                    .addGroup(pnlMyOrdersLayout.createSequentialGroup()
                        .addGroup(pnlMyOrdersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlMyOrdersLayout.createSequentialGroup()
                                .addGap(405, 405, 405)
                                .addComponent(jLabel2))
                            .addGroup(pnlMyOrdersLayout.createSequentialGroup()
                                .addGap(310, 310, 310)
                                .addComponent(jLabel4)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pnlMyOrdersLayout.setVerticalGroup(
            pnlMyOrdersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMyOrdersLayout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE)
                .addContainerGap())
        );

        btnCancelOrder.setBackground(new java.awt.Color(214, 105, 45));
        btnCancelOrder.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnCancelOrder.setForeground(new java.awt.Color(255, 255, 255));
        btnCancelOrder.setText("CANCEL ORDER");
        btnCancelOrder.addActionListener(this::btnCancelOrderActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlsidebar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(32, 32, 32)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel3)
                                        .addGap(503, 503, 503))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                        .addComponent(btnDonut, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(49, 49, 49)
                                        .addComponent(btnMunchkins, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(51, 51, 51)
                                        .addComponent(btnCoffee, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(50, 50, 50)
                                        .addComponent(btnDrinks, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(26, 26, 26)
                                .addComponent(lblTotalCartItem)
                                .addGap(339, 339, 339)
                                .addComponent(btnCancelOrder)
                                .addGap(58, 58, 58)
                                .addComponent(btnAddToCart, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(80, 80, 80)
                                .addComponent(btnViewCart, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 120, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(pnlMyOrders, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(pnlProducts, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlsidebar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel3)
                .addGap(35, 35, 35)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnMunchkins, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCoffee, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDonut, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDrinks, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(pnlProducts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlMyOrders, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddToCart, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnViewCart, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTotalCartItem)
                    .addComponent(btnCancelOrder, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnMunchkinsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMunchkinsActionPerformed
        showProducts("MUNCHKINS");
    }//GEN-LAST:event_btnMunchkinsActionPerformed

    private void btnAddToCartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddToCartActionPerformed
       
       
if (selectedProduct.isEmpty()) {
    javax.swing.JOptionPane.showMessageDialog(
        this, "Please select a product first."
    );
    return;
}

String input = javax.swing.JOptionPane.showInputDialog(
    this, "Enter quantity:"
);

if (input == null) return;

try {
    int quantity = Integer.parseInt(input);
    
    if (quantity > 20) {
    javax.swing.JOptionPane.showMessageDialog(
        this,
        "Maximum quantity per product is 20."
    );
    return;
}

if (OrderData.getTotalItems() + quantity > 50) {
    javax.swing.JOptionPane.showMessageDialog(
        this,
        "Your cart cannot contain more than 50 items."
    );
    return;
}

    if (quantity <= 0) {
        javax.swing.JOptionPane.showMessageDialog(
            this, "Quantity must be greater than zero."
        );
        return;
    }

    OrderData.addToCart(selectedProduct, selectedPrice, quantity);
    updateCartCount();

    javax.swing.JOptionPane.showMessageDialog(
        this, selectedProduct + " added to cart!"
    );

    selectedProduct = "";
    selectedPrice = 0.0;

} catch (NumberFormatException e) {
    javax.swing.JOptionPane.showMessageDialog(
        this, "Please enter a valid quantity."
    );
}

    }//GEN-LAST:event_btnAddToCartActionPerformed

    private void btnViewCartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnViewCartActionPerformed
        new CartFrame().setVisible(true);
    }//GEN-LAST:event_btnViewCartActionPerformed

    private void btnDonutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDonutActionPerformed
        showProducts("DONUT");
    }//GEN-LAST:event_btnDonutActionPerformed

    private void btnCoffeeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCoffeeActionPerformed
        showProducts("COFFEE");
    }//GEN-LAST:event_btnCoffeeActionPerformed

    private void btnDrinksActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDrinksActionPerformed
        showProducts("DRINKS");
    }//GEN-LAST:event_btnDrinksActionPerformed

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

    private void btnHomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHomeActionPerformed
        showMenuView();
showProducts("DONUT");
    }//GEN-LAST:event_btnHomeActionPerformed

    private void btnMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMenuActionPerformed
        showMenuView();
showProducts("DONUT"); 
    }//GEN-LAST:event_btnMenuActionPerformed

    private void btnMyCartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMyCartActionPerformed
        CartFrame cart = new CartFrame();
    cart.setLocationRelativeTo(this);
    cart.setVisible(true);

    }//GEN-LAST:event_btnMyCartActionPerformed

    private void btnMyOrdersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMyOrdersActionPerformed
          showMyOrdersView();
loadMyOrders();
    }//GEN-LAST:event_btnMyOrdersActionPerformed

    private void btnCancelOrderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelOrderActionPerformed

    int selectedRow = tblMyOrders.getSelectedRow();

    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(
            this,
            "Please select an order from My Orders first."
        );
        return;
    }

    int modelRow =
        tblMyOrders.convertRowIndexToModel(selectedRow);

    int orderId;

    try {
        orderId = Integer.parseInt(
            tblMyOrders.getModel()
                .getValueAt(modelRow, 0).toString()
        );
    } catch (Exception e) {
        JOptionPane.showMessageDialog(
            this, "Unable to identify the selected order."
        );
        return;
    }

    int confirm = JOptionPane.showConfirmDialog(
        this,
        "Are you sure you want to cancel Order #" + orderId + "?",
        "Confirm Cancellation",
        JOptionPane.YES_NO_OPTION
    );

    if (confirm != JOptionPane.YES_OPTION) {
        return;
    }

    try (Connection con =
             dunkin.database.DatabaseConnection.getConnection()) {

        con.setAutoCommit(false);

        try {
            // Lock and verify that this order belongs to the customer.
            String checkOrder =
                "SELECT order_status FROM orders " +
                "WHERE order_id = ? AND customer_id = ? FOR UPDATE";

            String status;

            try (PreparedStatement pst =
                     con.prepareStatement(checkOrder)) {

                pst.setInt(1, orderId);
                pst.setInt(2, UserSession.userId);

                try (ResultSet rs = pst.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException(
                            "Order not found or does not belong to you."
                        );
                    }

                    status = rs.getString("order_status");
                }
            }

            if (status == null ||
                    !"PENDING".equalsIgnoreCase(status)) {
                throw new SQLException(
                    "This order can no longer be cancelled. " +
                    "Only pending orders can be cancelled."
                );
            }

            // Change status first, within the same transaction.
            String cancelSql =
                "UPDATE orders SET order_status = 'CANCELLED' " +
                "WHERE order_id = ? AND customer_id = ? " +
                "AND order_status = 'PENDING'";

            try (PreparedStatement pst =
                     con.prepareStatement(cancelSql)) {

                pst.setInt(1, orderId);
                pst.setInt(2, UserSession.userId);

                if (pst.executeUpdate() != 1) {
                    throw new SQLException(
                        "The order status changed. Please refresh My Orders."
                    );
                }
            }

            // Read the quantities that were ordered.
            String itemsSql =
                "SELECT product_id, quantity FROM order_items " +
                "WHERE order_id = ?";

            int itemCount = 0;

            try (PreparedStatement items =
                     con.prepareStatement(itemsSql)) {

                items.setInt(1, orderId);

                try (ResultSet rs = items.executeQuery()) {

                    while (rs.next()) {
                        int productId = rs.getInt("product_id");
                        int quantity = rs.getInt("quantity");

                        if (quantity <= 0) {
                            throw new SQLException(
                                "Invalid quantity in order items."
                            );
                        }

                        String restoreSql =
                            "UPDATE products SET stock = stock + ? " +
                            "WHERE product_id = ?";

                        try (PreparedStatement restore =
                                 con.prepareStatement(restoreSql)) {

                            restore.setInt(1, quantity);
                            restore.setInt(2, productId);

                            if (restore.executeUpdate() != 1) {
                                throw new SQLException(
                                    "Unable to restore product stock."
                                );
                            }
                        }

                        itemCount++;
                    }
                }
            }

            if (itemCount == 0) {
                throw new SQLException(
                    "No order items were found. Cancellation was not saved."
                );
            }

            con.commit();
            
            OrderData.clearCart();

            JOptionPane.showMessageDialog(
                this,
                "Order #" + orderId +
                " was cancelled successfully.\n" +
                "The product stock has been restored."
            );

            loadMyOrders();

        } catch (Exception e) {
            try {
                con.rollback();
            } catch (SQLException rollbackError) {
                e.addSuppressed(rollbackError);
            }

            JOptionPane.showMessageDialog(
                this,
                "Cancellation failed: " + e.getMessage(),
                "Cancellation Error",
                JOptionPane.ERROR_MESSAGE
            );

        } finally {
            try {
                con.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(
            this,
            "Database error: " + e.getMessage(),
            "Database Error",
            JOptionPane.ERROR_MESSAGE
        );
    }

    }//GEN-LAST:event_btnCancelOrderActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
    java.awt.EventQueue.invokeLater(new Runnable() {
        @Override
        public void run() {
            new CustomerFrame().setVisible(true);
        }
    });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddToCart;
    private javax.swing.JButton btnCancelOrder;
    private javax.swing.JButton btnCoffee;
    private javax.swing.JButton btnDonut;
    private javax.swing.JButton btnDrinks;
    private javax.swing.JButton btnHome;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnMenu;
    private javax.swing.JButton btnMunchkins;
    private javax.swing.JButton btnMyCart;
    private javax.swing.JButton btnMyOrders;
    private javax.swing.JButton btnViewCart;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblTotalCartItem;
    private javax.swing.JPanel pnlMyOrders;
    private javax.swing.JPanel pnlProducts;
    private javax.swing.JPanel pnlsidebar;
    private javax.swing.JTable tblMyOrders;
    // End of variables declaration//GEN-END:variables

}