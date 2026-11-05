/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dunkin.gui;
import java.util.ArrayList;
/**
 *
 * @author ava
 */
class OrderData {
    public static ArrayList<String> productNames = new ArrayList<>();
    public static ArrayList<Double> productPrices = new ArrayList<>();
    public static ArrayList<Integer> quantities = new ArrayList<>();

    public static void addToCart(String name, double price, int quantity) {
        productNames.add(name);
        productPrices.add(price);
        quantities.add(quantity);
    }

    public static double getTotal() {
        double total = 0;

        for (int i = 0; i < productNames.size(); i++) {
            total += productPrices.get(i) * quantities.get(i);
        }

        return total;
    }

    public static int getTotalItems() {
        int count = 0;

        for (int quantity : quantities) {
            count += quantity;
        }

        return count;
    }

    
public static void clearCart() {
    productNames.clear();
    productPrices.clear();
    quantities.clear();
}

}
