package application;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.time.LocalDate;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Controller {
	

	@FXML
    private ListView<String> menuListView;

    private ObservableList<String> menuItems = FXCollections.observableArrayList();
    
    @FXML
    private ListView<String> customerListView;

    private ObservableList<String> customerItems = FXCollections.observableArrayList();
    
    @FXML
    private ListView<String> checkOutList;
    
    private ObservableList<String> checkOutItems = FXCollections.observableArrayList();
    
    @FXML
    private Button Customer, Menu, info, exit;
 
    @FXML
    private Text MenuInfo, MenutxtArea, MenuName, MenuPrice, CustomerInfo, totalPrice, menutxt, OrderInfo; 

    @FXML
    private TextField MenutxtName, MenutxtPrice, name, seat, surname;
    
    @FXML
    private RadioButton rd1, rd2, rd3;

    @FXML
    private DatePicker date;  
    
    @FXML
    private Spinner<Integer> menuCount;
    
    
    @FXML
    private DBConnect object;
    private Connection objectConnection;
    
    // Closes the current stage
    @FXML
    void exit(ActionEvent event) {
    	Stage stage = (Stage) exit.getScene().getWindow();
    	stage.close();
    }
    
    
    
    /*------------------------------------------------------------------------------------------------------------------------
                                                           Customer:
    ------------------------------------------------------------------------------------------------------------------------*/

    
    
    // Load the Customer.fxml and display it
    @FXML
    void AddCustomer(ActionEvent event) throws IOException {
    	 	FXMLLoader loader = new FXMLLoader(getClass().getResource("Customer.fxml"));
    	    Parent root = loader.load();
    	    
    	    Controller customerController = loader.getController();
    	    customerController.populateCustomerListView();
    	    
    	    Stage stage = new Stage();
    	    stage.setTitle("Customer Page");
    	    stage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png")));
    	    Scene scene = new Scene(root);
    	    stage.setScene(scene);
    	    stage.show();
    }

    // Populate the customer list view from the database
    private void populateCustomerListView() {
        object = new DBConnect();
        objectConnection = object.connect();
        String query = "SELECT name, surname, seat, date FROM customer";
        try {
            PreparedStatement statement = objectConnection.prepareStatement(query);
            ResultSet customerSet = statement.executeQuery();
            while (customerSet.next()) {
                String customerEntry = customerSet.getString("name") + " " + customerSet.getString("surname") + "\nSeats taken: " + customerSet.getString("seat") + "\nReservation date: " + customerSet.getObject("date", LocalDate.class);
                customerItems.add(customerEntry);
            }
            customerListView.setItems(customerItems);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Load customers from the database
    private void loadCustomers() {
        customerItems.clear();
        String query = "SELECT * FROM customer";
        try {
            Statement statement = objectConnection.createStatement();
            ResultSet customerSet = statement.executeQuery(query);
            while (customerSet.next()) {
                String customerEntry = customerSet.getString("name") + " " +
                					   customerSet.getString("surname") + "\nSeats taken: " +
                					   customerSet.getString("seat") + "\nReservation date: " +
                					   customerSet.getString("date");
                customerItems.add(customerEntry);
            }
            customerListView.setItems(customerItems);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Add new customers to the database and validate inputs
    @FXML
    void CustomerAdd(ActionEvent event) {
    	    String customerName = name.getText().trim();
    	    String customerSurname = surname.getText().trim();
    	    String seatText = seat.getText().trim();
    	    LocalDate reservationDate = date.getValue();


    	    if (customerName.isEmpty()) {
    	        CustomerInfo.setText("Name cannot be blank.");
    	        return;
    	    }

    	    if (customerSurname.isEmpty()) {
    	        CustomerInfo.setText("Surname cannot be blank.");
    	        return;
    	    }

    	    try {
    	        Integer.parseInt(seatText);
    	    } catch (NumberFormatException e) {
    	        CustomerInfo.setText("Seat number must be a valid integer.");
    	        return;
    	    }

    	    if (reservationDate == null) {
    	        CustomerInfo.setText("Please pick a valid date.");
    	        return;
    	    }
    	   String query = "INSERT INTO customer (name, surname, seat, date) VALUES (?, ?, ?, ?)";
    	    try {
    	        PreparedStatement addQuery = objectConnection.prepareStatement(query);
    	        addQuery.setString(1, name.getText());
    	        addQuery.setString(2, surname.getText());
    	        addQuery.setString(3, seat.getText());
    	        LocalDate localDate = date.getValue();
    	        addQuery.setObject(4, localDate);
    	        addQuery.execute();
    	    } catch (SQLException e) {
    	        e.printStackTrace();
    	    }
    	    loadCustomers();
        }
    
    // Deletes customers from the database
    @FXML
    void CustomerDelete(ActionEvent event) {
    	int selectedIndex = customerListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            String selectedCustomer = customerListView.getSelectionModel().getSelectedItem();
            String[] parts = selectedCustomer.split("\n");
            String[] nameParts = parts[0].split(" ");
            String firstName = nameParts[0];
            String lastName = nameParts[1];
            String query = "DELETE FROM customer WHERE name = ? AND surname = ?";
            try {
                PreparedStatement deleteQuery = objectConnection.prepareStatement(query);
                deleteQuery.setString(1, firstName);
                deleteQuery.setString(2, lastName);
                deleteQuery.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            loadCustomers();
        } else {
            CustomerInfo.setText("Please select a customer to delete.");
        }
    }

    // Updates customers from the database
    @FXML
    void CustomerUpdate(ActionEvent event) {
    	   int selectedIndex = customerListView.getSelectionModel().getSelectedIndex();
    	    if (selectedIndex >= 0) {
    	        String selectedCustomer = customerListView.getSelectionModel().getSelectedItem();
    	        String[] parts = selectedCustomer.split("\n");
    	        String[] nameParts = parts[0].split(" ");
    	        String firstName = nameParts[0];
    	        String lastName = nameParts[1];

    	        String query = "UPDATE customer SET name = ?, surname = ?, seat = ?, date = ? WHERE name = ? AND surname = ?";
    	        try {
    	            PreparedStatement updateQuery = objectConnection.prepareStatement(query);
    	            updateQuery.setString(1, name.getText());
    	            updateQuery.setString(2, surname.getText());
    	            updateQuery.setString(3, seat.getText());
    	            LocalDate localDate = date.getValue();
    	            updateQuery.setObject(4, localDate);
    	            updateQuery.setString(5, firstName);
    	            updateQuery.setString(6, lastName);
    	            updateQuery.execute();
    	        } catch (SQLException e) {
    	            e.printStackTrace();
    	        }
    	        loadCustomers();
    	    } else {
    	        CustomerInfo.setText("Please select a customer to update.");
    	    }
    }
    
    
    
    /*------------------------------------------------------------------------------------------------------------------------
                                                           Menu:
    ------------------------------------------------------------------------------------------------------------------------*/
    
    
    
    // Load the Menu.fxml and display it
    @FXML
    void AddToMenu(ActionEvent event) throws IOException {
    	FXMLLoader loader = new FXMLLoader(getClass().getResource("Menu.fxml"));
 	    Parent root = loader.load();
 	    
 	    Controller menuController = loader.getController();
 	    menuController.populateMenuListView();
 	    
 	    Stage stage = new Stage();
 	    stage.setTitle("Menu Page");
	    stage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png")));
 	    Scene scene = new Scene(root);
 	    stage.setScene(scene);
 	    stage.show();
    }
    
    // Populate the menu list view from the database
    private void populateMenuListView() {
        object = new DBConnect();
        objectConnection = object.connect();
        String query = "SELECT type, menu_name, price FROM menu";
        try {
            PreparedStatement statement = objectConnection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String menuEntry = resultSet.getString("type") + ": " + resultSet.getString("menu_name") + ", Price: " + resultSet.getString("price") + " €";
                menuItems.add(menuEntry); 
            }
            menuListView.setItems(menuItems);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Load menu items from the database
    private void loadMenuItems() {
        menuItems.clear();
        String query = "SELECT * FROM menu";
        try {
            Statement statement = objectConnection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                String menuEntry = resultSet.getString("type") + ": " +
                				   resultSet.getString("menu_name") + ", Price: " +                                   
                                   resultSet.getDouble("price") + " €";
                menuItems.add(menuEntry);
            }
            menuListView.setItems(menuItems);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Return the selected menu type
    private String getMenuType() {
        if (rd1.isSelected()) {
            return "Drink";
        } else if (rd2.isSelected()) {
            return "Meal";
        } else if (rd3.isSelected()) {
            return "Dessert";
        }
        return "";
    }
 
    // Dessert is selected
    @FXML
    void Desert(ActionEvent event) {
        MenuName.setText("Dessert Name:");
        MenuPrice.setText("Dessert Price:");
        rd1.setSelected(false);
        rd2.setSelected(false);
        rd3.setSelected(true);
    }

    // Drink is selected
    @FXML
    void Drink(ActionEvent event) {
    	MenuName.setText("Drink Name:");
        MenuPrice.setText("Drink Price:");
        rd1.setSelected(true);
        rd2.setSelected(false);
        rd3.setSelected(false);
    }

    // Meal is selected
    @FXML
    void Meal(ActionEvent event) {
    	MenuName.setText("Meal Name:");
        MenuPrice.setText("Meal Price:");
        rd1.setSelected(false);
        rd2.setSelected(true);
        rd3.setSelected(false);
    }
  
    // Add new items to the menu and validate inputs
    @FXML
    void MenuAdd(ActionEvent event) {
    	String itemName = MenutxtName.getText();
        String itemPrice = MenutxtPrice.getText();
        String itemType = getMenuType();
        
        if (itemType.isEmpty()) {
            MenuInfo.setText("Please select a type.");
            return;
        }
        if (itemName.isEmpty()) {
            MenuInfo.setText("Item name cannot be blank.");
            return;
        }

        try {
            Double.parseDouble(itemPrice);
        } catch (NumberFormatException e) {
            MenuInfo.setText("Price must be a valid number.");
            return;
        }

        
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        String roundedPrice = decimalFormat.format(Double.parseDouble(itemPrice));

        String query = "INSERT INTO menu (type, menu_name, price) VALUES (?, ?, ?)";
        try {
            PreparedStatement addQuery = objectConnection.prepareStatement(query);
            addQuery.setString(1, itemType);
            addQuery.setString(2, itemName);
            addQuery.setDouble(3, Double.parseDouble(roundedPrice));
            addQuery.execute();
            loadMenuItems();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Delete menu items
    @FXML
    void Menubtndelete(ActionEvent event) {
    	int selectedIndex = menuListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            String selectedMenuItem = menuListView.getSelectionModel().getSelectedItem();
            String menuName = selectedMenuItem.split(": ")[1].split(", Price")[0].trim();
            
            String query = "DELETE FROM menu WHERE menu_name = ?";
            try (PreparedStatement deleteQuery = objectConnection.prepareStatement(query)) {
                deleteQuery.setString(1, menuName);
                deleteQuery.execute();
                loadMenuItems();
            } catch (SQLException e) {
                e.printStackTrace();
                MenuInfo.setText("Error deleting menu item.");
            }
        } else {
            MenuInfo.setText("Please select a menu item to delete.");
        }
    }

    // Update menu items
    @FXML
    void Menubtnupdate(ActionEvent event) {
    	 int selectedIndex = menuListView.getSelectionModel().getSelectedIndex();
    	    if (selectedIndex >= 0) {
    	        String selectedMenuItem = menuListView.getSelectionModel().getSelectedItem();
    	        String oldMenuName = selectedMenuItem.split(": ")[1].split(", Price")[0].trim();

    	        String newItemName = MenutxtName.getText().trim();
    	        String newItemPrice = MenutxtPrice.getText().trim();
    	        String newItemType = getMenuType();

    	        if (newItemName.isEmpty() || newItemPrice.isEmpty() || newItemType.isEmpty()) {
    	            MenuInfo.setText("Please enter item name, price, and select a type.");
    	            return;
    	        }

    	        double price;
    	        try {
    	            price = Double.parseDouble(newItemPrice);
    	        } catch (NumberFormatException e) {
    	            MenuInfo.setText("Price must be a valid number.");
    	            return;
    	        }

    	        String query = "UPDATE menu SET type = ?, menu_name = ?, price = ? WHERE menu_name = ?";
    	        try (PreparedStatement updateQuery = objectConnection.prepareStatement(query)) {
    	            updateQuery.setString(1, newItemType);
    	            updateQuery.setString(2, newItemName);
    	            updateQuery.setDouble(3, price);
    	            updateQuery.setString(4, oldMenuName);
    	            updateQuery.execute();
    	            loadMenuItems();
    	        } catch (SQLException e) {
    	            e.printStackTrace();
    	            MenuInfo.setText("Error updating menu item.");
    	        }
    	    } else {
    	        MenuInfo.setText("Please select a menu item to update.");
    	    }
     }
    
   
    
    /*------------------------------------------------------------------------------------------------------------------------
                                                             Order:
    ------------------------------------------------------------------------------------------------------------------------*/
   
    
    // Initialize the spinner for selecting quantity of menu items
    public void initializeSpinner() {
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, 1);
        menuCount.setValueFactory(valueFactory);
    }
    
    // Load the Order.fxml and display it
    @FXML
    void OrderFromMenu(ActionEvent event) throws IOException {
    	FXMLLoader loader = new FXMLLoader(getClass().getResource("Order.fxml"));
 	    Parent root = loader.load();
 	    
 	    Controller menuController = loader.getController();
 	    menuController.initializeSpinner();
 	    menuController.populateMenuListView();
 	    
 	    Stage stage = new Stage();
 	    stage.setTitle("Order Page");
	    stage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png")));
 	    Scene scene = new Scene(root);
 	    stage.setScene(scene);
 	    stage.show();
    }
    
    // Add the selected menu item to the checkout list
    @FXML
    void addtolist(ActionEvent event) {
    	String selectedItem = menuListView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            int quantity = menuCount.getValue();            
            String[] parts = selectedItem.split("\\s+");         
            double price = Double.parseDouble(parts[parts.length - 2]);           
            double fullPrice = price * quantity;
            
            StringBuilder itemWithPriceAndQuantity = new StringBuilder(selectedItem);
            itemWithPriceAndQuantity.append(" x").append(quantity).append(" = ").append(String.format("%.2f", fullPrice)).append(" €");
            
            checkOutItems.add(itemWithPriceAndQuantity.toString());            
            checkOutList.setItems(checkOutItems);
            updateTotalPrice();
            OrderInfo.setText("");
        } else {
            OrderInfo.setText("No item selected");
        }
    }
    
    // Update the total price of items in the checkout list
    private void updateTotalPrice() {
        double totalPriceValue = 0;
        for (String item : checkOutItems) {
            String[] parts = item.split("\\s+");
            double price = Double.parseDouble(parts[parts.length - 2]);
            totalPriceValue += price;
        }
        totalPrice.setText("Total price: " + String.format("%.2f", totalPriceValue) + " €");
    }
    
    // Remove selected items from the checkout list
    @FXML
    void removeOrder(ActionEvent event) {
        int selectedIndex = checkOutList.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            checkOutItems.remove(selectedIndex);
            checkOutList.setItems(checkOutItems);
            
            updateTotalPrice();
            OrderInfo.setText("");
        } else {
            OrderInfo.setText("No item selected");
        }
    }
    
    // Purchases the items
    @FXML
    void checkOut(ActionEvent event) {
        if (checkOutItems.isEmpty()) {
            OrderInfo.setText("No item selected");
        } else {
            checkOutItems.clear();
            checkOutList.setItems(checkOutItems);
            
            totalPrice.setText("Purchase successful");
            OrderInfo.setText("");
        }
    }
}