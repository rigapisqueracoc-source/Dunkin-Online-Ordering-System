/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package dunkin.gui;
import java.awt.CardLayout;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
/**
 *
 * @author ava
 */
public class StaffFrame extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(StaffFrame.class.getName());

    /**
     * Creates new form StaffFrame
     */
    private Connection getConnection() throws SQLException {
    return dunkin.database.DatabaseConnection.getConnection();
}

private void loadCustomerOrders() {
    String sql =
    "SELECT o.order_id, u.username, o.order_date, " +
    "o.total_amount, o.payment_method, o.reference_number, " +
    "o.payment_status, o.order_status " +
    "FROM orders o " +
    "JOIN users u ON o.customer_id = u.user_id " +
    "ORDER BY o.order_id DESC";

    try (Connection con = getConnection();
         PreparedStatement pst = con.prepareStatement(sql);
         ResultSet rs = pst.executeQuery()) {

        DefaultTableModel model =
            (DefaultTableModel) jTable1.getModel();

        model.setRowCount(0);

        while (rs.next()) {
    model.addRow(new Object[]{
        rs.getInt("order_id"),
        rs.getString("username"),
        rs.getTimestamp("order_date"),
        rs.getBigDecimal("total_amount"),
        rs.getString("payment_method"),
        rs.getString("reference_number"),
        rs.getString("payment_status"),
        rs.getString("order_status")
    });
}

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(
            this, "Could not load orders: " + e.getMessage());
    }
}


// LOAD TODAY'S STAFF DASHBOARD
private void loadStaffDashboard() {

    try (java.sql.Connection con =
            dunkin.database.DatabaseConnection.getConnection()) {

        // TOTAL ORDERS TODAY
        String sqlTotal =
                "SELECT COUNT(*) AS total FROM orders "
              + "WHERE DATE(order_date) = CURDATE()";

        try (java.sql.PreparedStatement pst =
                con.prepareStatement(sqlTotal);
             java.sql.ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                jLabel6.setText(
                        String.valueOf(rs.getInt("total")));
            }
        }

        // PENDING ORDERS TODAY
        String sqlPending =
                "SELECT COUNT(*) AS total FROM orders "
              + "WHERE DATE(order_date) = CURDATE() "
              + "AND order_status = 'PENDING'";

        try (java.sql.PreparedStatement pst =
                con.prepareStatement(sqlPending);
             java.sql.ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                jLabel7.setText(
                        String.valueOf(rs.getInt("total")));
            }
        }

        // ACCEPTED ORDERS TODAY
        String sqlAccepted =
                "SELECT COUNT(*) AS total FROM orders "
              + "WHERE DATE(order_date) = CURDATE() "
              + "AND order_status = 'ACCEPTED'";

        try (java.sql.PreparedStatement pst =
                con.prepareStatement(sqlAccepted);
             java.sql.ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                jLabel9.setText(
                        String.valueOf(rs.getInt("total")));
            }
        }

        // VERIFIED SALES TODAY
        String sqlSales =
                "SELECT COALESCE(SUM(total_amount), 0) AS total "
              + "FROM orders "
              + "WHERE DATE(order_date) = CURDATE() "
              + "AND payment_status = 'VERIFIED'";

        try (java.sql.PreparedStatement pst =
                con.prepareStatement(sqlSales);
             java.sql.ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                jLabel10.setText(
                        "₱" + String.format("%.2f",
                        rs.getDouble("total")));
            }
        }

    } catch (java.sql.SQLException e) {

        javax.swing.JOptionPane.showMessageDialog(
                this,
                "Dashboard error: " + e.getMessage()
        );
    }
}
    
    public StaffFrame() {
        initComponents();
        setupNavigation();
    loadCustomerOrders();
    loadStaffDashboard();
    loadTodayOrders();
    jTable1.setDefaultEditor(Object.class, null);
    }
    
    private void loadTodayOrders() {

    javax.swing.table.DefaultTableModel model =
            (javax.swing.table.DefaultTableModel) jTable1.getModel();

    model.setRowCount(0);

    String sql =
            "SELECT order_id, customer_id, total_amount, "
          + "reference_number, payment_status, order_status "
          + "FROM orders "
          + "WHERE DATE(order_date) = CURDATE() "
          + "ORDER BY order_id DESC";

    try (java.sql.Connection con =
            dunkin.database.DatabaseConnection.getConnection();
         java.sql.PreparedStatement pst =
            con.prepareStatement(sql);
         java.sql.ResultSet rs =
            pst.executeQuery()) {

        while (rs.next()) {

            model.addRow(new Object[]{
                rs.getInt("order_id"),
                rs.getInt("customer_id"),
                rs.getDouble("total_amount"),
                rs.getString("reference_number"),
                rs.getString("payment_status"),
                rs.getString("order_status")
            });
        }

    } catch (java.sql.SQLException e) {

        javax.swing.JOptionPane.showMessageDialog(
                this,
                "Unable to load orders: " + e.getMessage()
        );
    }
}
    
    private void fillPaymentFieldsFromSelection() {
    int row = jTable1.getSelectedRow();

    if (row == -1) {
        JOptionPane.showMessageDialog(
            this, "Please select an order first."
        );
        return;
    }

    int modelRow = jTable1.convertRowIndexToModel(row);

    jTextField1.setText(
        jTable1.getModel().getValueAt(modelRow, 0).toString()
    );

    Object reference =
        jTable1.getModel().getValueAt(modelRow, 5);

    jTextField2.setText(
        reference == null ? "" : reference.toString()
    );
}
    
    
private Connection getconnection() throws SQLException {
    return DriverManager.getConnection(
        "jdbc:mysql://localhost:3306/dunkin_ordering",
        "root",
        ""
    );
}

private void loadOrders() {
    DefaultTableModel model = new DefaultTableModel(
    new String[]{
        "Order ID", "Customer", "Order Date", "Total",
        "Payment Method", "Reference No.",
        "Payment Status", "Order Status"
    }, 0
);

    
String sql =
    "SELECT o.order_id, u.username, o.order_date, " +
    "o.total_amount, o.payment_method, o.reference_number, " +
    "o.payment_status, o.order_status " +
    "FROM orders o " +
    "LEFT JOIN users u ON o.customer_id = u.user_id " +
    "ORDER BY o.order_id DESC";


    try (Connection con = getConnection();
         PreparedStatement pst = con.prepareStatement(sql);
         ResultSet rs = pst.executeQuery()) {

        while (rs.next()) {
            
model.addRow(new Object[]{
    rs.getInt("order_id"),
    rs.getString("username"),
    rs.getTimestamp("order_date"),
    rs.getBigDecimal("total_amount"),
    rs.getString("payment_method"),
    rs.getString("reference_number"),
    rs.getString("payment_status"),
    rs.getString("order_status")
});

        }

        jTable1.setModel(model);

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(
            this,
            "Could not load orders: " + e.getMessage()
        );
    }
}


private void loaddashboard() {
    String sql =
        "SELECT COUNT(*) AS total_orders, " +
        "COALESCE(SUM(CASE WHEN order_status = 'PENDING' " +
        "THEN 1 ELSE 0 END), 0) AS pending_orders, " +
        "COALESCE(SUM(CASE WHEN order_status = 'ACCEPTED' " +
        "THEN 1 ELSE 0 END), 0) AS accepted_orders, " +
        "COALESCE(SUM(CASE WHEN payment_status = 'VERIFIED' " +
        "THEN total_amount ELSE 0 END), 0) AS total_sales " +
        "FROM orders";

    try (Connection con = getConnection();
         PreparedStatement pst = con.prepareStatement(sql);
         ResultSet rs = pst.executeQuery()) {

        if (rs.next()) {
            jLabel6.setText(
                String.valueOf(rs.getInt("total_orders"))
            );
            jLabel7.setText(
                String.valueOf(rs.getInt("pending_orders"))
            );
            jLabel9.setText(
                String.valueOf(rs.getInt("accepted_orders"))
            );
            jLabel10.setText(
                "₱" + rs.getBigDecimal("total_sales").toPlainString()
            );
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(
            this, "Could not load dashboard: " + e.getMessage()
        );
    }
}


    
    private void setupNavigation() {
    pnlContent.removeAll();
    pnlContent.setLayout(new CardLayout());

    pnlContent.add(pnlDashboard, "DASHBOARD");
    pnlContent.add(pnlCustomerOrders, "ORDERS");
    pnlContent.add(pnlPaymentVerification, "PAYMENT");
    pnlContent.add(pnlOrderstatus, "STATUS");

    showPanel("DASHBOARD");
}

private void showPanel(String panelName) {
    CardLayout cl = (CardLayout) pnlContent.getLayout();
    cl.show(pnlContent, panelName);

    pnlContent.revalidate();
    pnlContent.repaint();
}


private void showStaffPanel(String panelName) {
    java.awt.CardLayout cl =
        (java.awt.CardLayout) pnlContent.getLayout();

    cl.show(pnlContent, panelName);
    pnlContent.revalidate();
    pnlContent.repaint();
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
        pnlSidebar = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        btnDashboard = new javax.swing.JButton();
        btnCustomerOrders = new javax.swing.JButton();
        btnPaymentVerification = new javax.swing.JButton();
        btnOrderStatus = new javax.swing.JButton();
        btnLogout = new javax.swing.JButton();
        pnlContent = new javax.swing.JPanel();
        pnlDashboard = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        pnlCustomerOrders = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        pnlPaymentVerification = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        pnlOrderstatus = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jComboBox1 = new javax.swing.JComboBox<>();
        jLabel16 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 153, 255));

        pnlSidebar.setBackground(new java.awt.Color(255, 204, 255));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(214, 105, 45));
        jLabel8.setText("DUNKIN' STAFF");

        btnDashboard.setBackground(new java.awt.Color(255, 102, 204));
        btnDashboard.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnDashboard.setForeground(new java.awt.Color(255, 255, 255));
        btnDashboard.setText("DASHBOARD");
        btnDashboard.addActionListener(this::btnDashboardActionPerformed);

        btnCustomerOrders.setBackground(new java.awt.Color(255, 102, 204));
        btnCustomerOrders.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnCustomerOrders.setForeground(new java.awt.Color(255, 255, 255));
        btnCustomerOrders.setText("CUSTOMER ORDERS");
        btnCustomerOrders.addActionListener(this::btnCustomerOrdersActionPerformed);

        btnPaymentVerification.setBackground(new java.awt.Color(255, 102, 204));
        btnPaymentVerification.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnPaymentVerification.setForeground(new java.awt.Color(255, 255, 255));
        btnPaymentVerification.setText("PAYMENT VERIFICATION");
        btnPaymentVerification.addActionListener(this::btnPaymentVerificationActionPerformed);

        btnOrderStatus.setBackground(new java.awt.Color(255, 102, 204));
        btnOrderStatus.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnOrderStatus.setForeground(new java.awt.Color(255, 255, 255));
        btnOrderStatus.setText("ORDER STATUS");
        btnOrderStatus.addActionListener(this::btnOrderStatusActionPerformed);

        btnLogout.setBackground(new java.awt.Color(255, 102, 204));
        btnLogout.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnLogout.setForeground(new java.awt.Color(255, 255, 255));
        btnLogout.setText("LOGOUT");
        btnLogout.addActionListener(this::btnLogoutActionPerformed);

        javax.swing.GroupLayout pnlSidebarLayout = new javax.swing.GroupLayout(pnlSidebar);
        pnlSidebar.setLayout(pnlSidebarLayout);
        pnlSidebarLayout.setHorizontalGroup(
            pnlSidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSidebarLayout.createSequentialGroup()
                .addGap(49, 49, 49)
                .addGroup(pnlSidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnLogout, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDashboard, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8)
                    .addComponent(btnCustomerOrders, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnPaymentVerification)
                    .addComponent(btnOrderStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(54, Short.MAX_VALUE))
        );
        pnlSidebarLayout.setVerticalGroup(
            pnlSidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSidebarLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(jLabel8)
                .addGap(59, 59, 59)
                .addComponent(btnDashboard, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31)
                .addComponent(btnCustomerOrders, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31)
                .addComponent(btnPaymentVerification, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31)
                .addComponent(btnOrderStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(32, 32, 32)
                .addComponent(btnLogout, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlContent.setBackground(new java.awt.Color(255, 204, 255));

        pnlDashboard.setBackground(new java.awt.Color(204, 204, 204));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(214, 105, 45));
        jLabel1.setText("DASHBOARD");

        jPanel2.setBackground(new java.awt.Color(255, 153, 204));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel2.setText("TOTAL ORDERS");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel6.setText("0");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(48, 48, 48)
                        .addComponent(jLabel2))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(87, 87, 87)
                        .addComponent(jLabel6)))
                .addContainerGap(55, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addGap(26, 26, 26)
                .addComponent(jLabel6)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBackground(new java.awt.Color(255, 153, 204));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel3.setText("PENDING ORDERS");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel7.setText("0");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(43, 43, 43)
                        .addComponent(jLabel3))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(83, 83, 83)
                        .addComponent(jLabel7)))
                .addContainerGap(45, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addGap(26, 26, 26)
                .addComponent(jLabel7)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBackground(new java.awt.Color(255, 153, 204));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel4.setText("ACCEPTED ORDERS");

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel9.setText("0");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(40, 40, 40)
                        .addComponent(jLabel4))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(82, 82, 82)
                        .addComponent(jLabel9)))
                .addContainerGap(41, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addGap(28, 28, 28)
                .addComponent(jLabel9)
                .addContainerGap(26, Short.MAX_VALUE))
        );

        jPanel5.setBackground(new java.awt.Color(255, 153, 204));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel5.setText("TOTAL SALES");

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel10.setText("₱0.00");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(56, 56, 56)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10)
                    .addComponent(jLabel5))
                .addContainerGap(58, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5)
                .addGap(28, 28, 28)
                .addComponent(jLabel10)
                .addContainerGap(26, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlDashboardLayout = new javax.swing.GroupLayout(pnlDashboard);
        pnlDashboard.setLayout(pnlDashboardLayout);
        pnlDashboardLayout.setHorizontalGroup(
            pnlDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDashboardLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addGroup(pnlDashboardLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(113, Short.MAX_VALUE))
        );
        pnlDashboardLayout.setVerticalGroup(
            pnlDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDashboardLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlCustomerOrders.setBackground(new java.awt.Color(204, 204, 204));

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(214, 105, 45));
        jLabel11.setText("CUSTOMER ORDERS");

        jTable1.setBackground(new java.awt.Color(204, 204, 204));
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Order ID", "Customer", "Order Date", "Total", "Payment Method", "GCash Ref.", "Payment Status", "Order Status"
            }
        ));
        jScrollPane2.setViewportView(jTable1);

        jScrollPane3.setViewportView(jScrollPane2);

        javax.swing.GroupLayout pnlCustomerOrdersLayout = new javax.swing.GroupLayout(pnlCustomerOrders);
        pnlCustomerOrders.setLayout(pnlCustomerOrdersLayout);
        pnlCustomerOrdersLayout.setHorizontalGroup(
            pnlCustomerOrdersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCustomerOrdersLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlCustomerOrdersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlCustomerOrdersLayout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        pnlCustomerOrdersLayout.setVerticalGroup(
            pnlCustomerOrdersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCustomerOrdersLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(12, Short.MAX_VALUE))
        );

        pnlPaymentVerification.setBackground(new java.awt.Color(204, 204, 204));

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(214, 105, 45));
        jLabel12.setText("PAYMENT VERIFICATION");

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel13.setText("Order ID: ");

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel14.setText("GCash Reference No,:");

        jTextField1.setBackground(new java.awt.Color(255, 255, 255));
        jTextField1.addActionListener(this::jTextField1ActionPerformed);

        jButton1.setBackground(new java.awt.Color(214, 105, 45));
        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("VERIFY PAYMENT");
        jButton1.addActionListener(this::jButton1ActionPerformed);

        javax.swing.GroupLayout pnlPaymentVerificationLayout = new javax.swing.GroupLayout(pnlPaymentVerification);
        pnlPaymentVerification.setLayout(pnlPaymentVerificationLayout);
        pnlPaymentVerificationLayout.setHorizontalGroup(
            pnlPaymentVerificationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPaymentVerificationLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlPaymentVerificationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlPaymentVerificationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlPaymentVerificationLayout.createSequentialGroup()
                            .addGroup(pnlPaymentVerificationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel14)
                                .addComponent(jLabel13))
                            .addGap(39, 39, 39)
                            .addGroup(pnlPaymentVerificationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlPaymentVerificationLayout.createSequentialGroup()
                            .addComponent(jLabel12)
                            .addGap(308, 308, 308)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlPaymentVerificationLayout.createSequentialGroup()
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(380, 380, 380))))
        );
        pnlPaymentVerificationLayout.setVerticalGroup(
            pnlPaymentVerificationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPaymentVerificationLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel12)
                .addGap(24, 24, 24)
                .addGroup(pnlPaymentVerificationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(19, 19, 19)
                .addGroup(pnlPaymentVerificationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlOrderstatus.setBackground(new java.awt.Color(204, 204, 204));

        jButton2.setBackground(new java.awt.Color(214, 105, 45));
        jButton2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton2.setForeground(new java.awt.Color(255, 255, 255));
        jButton2.setText("UPDATE STATUS");
        jButton2.addActionListener(this::jButton2ActionPerformed);

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "PENDING", "ACCEPTED", "PREPARING", "READY FOR PICKUP", "COMPLETED", "CANCELLED" }));

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel16.setText("Order Status:");

        jLabel15.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(214, 105, 45));
        jLabel15.setText("ORDER STATUS");

        javax.swing.GroupLayout pnlOrderstatusLayout = new javax.swing.GroupLayout(pnlOrderstatus);
        pnlOrderstatus.setLayout(pnlOrderstatusLayout);
        pnlOrderstatusLayout.setHorizontalGroup(
            pnlOrderstatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlOrderstatusLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel16)
                .addGap(58, 58, 58)
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(317, 317, 317))
            .addGroup(pnlOrderstatusLayout.createSequentialGroup()
                .addGroup(pnlOrderstatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlOrderstatusLayout.createSequentialGroup()
                        .addGap(370, 370, 370)
                        .addComponent(jLabel15))
                    .addGroup(pnlOrderstatusLayout.createSequentialGroup()
                        .addGap(371, 371, 371)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlOrderstatusLayout.setVerticalGroup(
            pnlOrderstatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlOrderstatusLayout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(jLabel15)
                .addGap(18, 18, 18)
                .addGroup(pnlOrderstatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16))
                .addGap(18, 18, 18)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(7, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlContentLayout = new javax.swing.GroupLayout(pnlContent);
        pnlContent.setLayout(pnlContentLayout);
        pnlContentLayout.setHorizontalGroup(
            pnlContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlContentLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlDashboard, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlCustomerOrders, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlPaymentVerification, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlOrderstatus, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlContentLayout.setVerticalGroup(
            pnlContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlContentLayout.createSequentialGroup()
                .addComponent(pnlDashboard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(pnlCustomerOrders, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlPaymentVerification, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlOrderstatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(pnlSidebar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlContent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(pnlSidebar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlContent, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnDashboardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDashboardActionPerformed
 loadStaffDashboard();
    showPanel("DASHBOARD");    }//GEN-LAST:event_btnDashboardActionPerformed

    private void btnCustomerOrdersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCustomerOrdersActionPerformed
        loadCustomerOrders();
    showPanel("ORDERS");
    }//GEN-LAST:event_btnCustomerOrdersActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        String orderIdText = jTextField1.getText().trim();
String referenceNo = jTextField2.getText().trim();

if (orderIdText.isEmpty() || referenceNo.isEmpty()) {
    JOptionPane.showMessageDialog(
        this, "Please enter the Order ID and GCash Reference No.");
    return;
}

int orderId;

try {
    orderId = Integer.parseInt(orderIdText);
} catch (NumberFormatException e) {
    JOptionPane.showMessageDialog(this, "Order ID must be a number.");
    return;
}

String sql =
    "UPDATE orders SET payment_status = 'VERIFIED' " +
    "WHERE order_id = ? AND reference_number = ? " +
    "AND payment_status = 'PENDING VERIFICATION' " +
    "AND payment_method IN ('GCash', 'Maya')";

try (Connection con =
         dunkin.database.DatabaseConnection.getConnection();
     PreparedStatement pst = con.prepareStatement(sql)) {

    pst.setInt(1, orderId);
    pst.setString(2, referenceNo);

    int updated = pst.executeUpdate();
    
    if (updated == 1) {
    JOptionPane.showMessageDialog(this,
        "Payment verified successfully!");

    jTextField1.setText("");
    jTextField2.setText("");

    // Refresh the Customer Orders table
    loadCustomerOrders();

} else {
    JOptionPane.showMessageDialog(this,
        "Order ID or reference number is incorrect, "
        + "or payment is already verified.");
}

} catch (SQLException e) {
    JOptionPane.showMessageDialog(
        this, "Verification failed: " + e.getMessage());
}
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
String orderText = JOptionPane.showInputDialog(
        this, "Enter the Order ID to update:");

    if (orderText == null || orderText.trim().isEmpty()) {
        return;
    }

    int orderId;

    try {
        orderId = Integer.parseInt(orderText.trim());
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "Invalid Order ID.");
        return;
    }
    
    String status = jComboBox1.getSelectedItem().toString();

    String sql = "UPDATE orders SET order_status = ? " +
                 "WHERE order_id = ?";

    
try (Connection con = getConnection();
     PreparedStatement pst = con.prepareStatement(sql)) {

    pst.setString(1, status);
    pst.setInt(2, orderId);

    



int updated = pst.executeUpdate();

if (updated == 1) {
    JOptionPane.showMessageDialog(this,
        "Payment verified successfully!");
        

    jTextField1.setText("");
    jTextField2.setText("");

    loadCustomerOrders();
    loadStaffDashboard();

} else {
    JOptionPane.showMessageDialog(this,
        "Verification failed. Check the Order ID, " +
        "reference number, payment method, and payment status.");
}

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(
            this, "Status update failed: " + e.getMessage());
    }    

    }//GEN-LAST:event_jButton2ActionPerformed

    private void btnPaymentVerificationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPaymentVerificationActionPerformed
         loadCustomerOrders();
    showPanel("PAYMENT");
    }//GEN-LAST:event_btnPaymentVerificationActionPerformed

    private void btnOrderStatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOrderStatusActionPerformed
        loadCustomerOrders();
    showPanel("STATUS");
    }//GEN-LAST:event_btnOrderStatusActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new StaffFrame().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCustomerOrders;
    private javax.swing.JButton btnDashboard;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnOrderStatus;
    private javax.swing.JButton btnPaymentVerification;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JPanel pnlContent;
    private javax.swing.JPanel pnlCustomerOrders;
    private javax.swing.JPanel pnlDashboard;
    private javax.swing.JPanel pnlOrderstatus;
    private javax.swing.JPanel pnlPaymentVerification;
    private javax.swing.JPanel pnlSidebar;
    // End of variables declaration//GEN-END:variables
}
