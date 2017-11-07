package com.ownimage.perception.view.fx;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class MyDemo extends Application {

	public static class Person {

		private final SimpleStringProperty firstName;
		private final SimpleStringProperty lastName;
		private final SimpleStringProperty email;

		private Person(final String fName, final String lName, final String email) {
			this.firstName = new SimpleStringProperty(fName);
			this.lastName = new SimpleStringProperty(lName);
			this.email = new SimpleStringProperty(email);
		}

		public String getEmail() {
			return email.get();
		}

		public String getFirstName() {
			return firstName.get();
		}

		public String getLastName() {
			return lastName.get();
		}

		public void setEmail(final String fName) {
			email.set(fName);
		}

		public void setFirstName(final String fName) {
			firstName.set(fName);
		}

		public void setLastName(final String fName) {
			lastName.set(fName);
		}
	}

	private final TableView<Person> tableview = new TableView<Person>();
	// Suppose your preferred height values for those 2 component are as follows:
	private final double TABLE_MIN_HEIGHT = 30.0;
	private final double TABLE_MAX_HEIGHT = 500.0;

	private double TITLED_PANE_HEIGHT; // will be determined

	private final ObservableList<Person> data = FXCollections.observableArrayList(
			new Person("Jacob", "Smith", "jacob.smith@example.com"),
			new Person("Isabella", "Johnson", "isabella.johnson@example.com"),
			new Person("Ethan", "Williams", "ethan.williams@example.com"),
			new Person("Emma", "Jones", "emma.jones@example.com"),
			new Person("Michael", "Brown", "michael.brown@example.com"));

	public static void main(final String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(final Stage stage) {
		TableColumn firstNameCol = new TableColumn("First Name");
		firstNameCol.setMinWidth(100);
		firstNameCol.setCellValueFactory(new PropertyValueFactory<Person, String>("firstName"));

		TableColumn lastNameCol = new TableColumn("Last Name");
		lastNameCol.setMinWidth(100);
		lastNameCol.setCellValueFactory(new PropertyValueFactory<Person, String>("lastName"));

		TableColumn emailCol = new TableColumn("Email");
		emailCol.setMinWidth(200);
		emailCol.setCellValueFactory(new PropertyValueFactory<Person, String>("email"));

		tableview.setItems(data);
		tableview.getColumns().addAll(firstNameCol, lastNameCol, emailCol);

		final TitledPane titledPane = new TitledPane("TitledPane", new Text("Content\n\n\n\n"));
		titledPane.setAnimated(false); // we need to temporarily disable
		// animation to get the titledpanes computed height correctly.

		// Force to min height of table view
		tableview.setMaxHeight(TABLE_MIN_HEIGHT);
		tableview.setMinHeight(TABLE_MIN_HEIGHT);

		// Here you have 2 options
		int option = 1;

		if (option == 1) {
			// 1st simply force the table view height to its preferred max value
			// when the titled pane's expanded property changed:
			titledPane.expandedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(final ObservableValue<? extends Boolean> observable, final Boolean oldValue, final Boolean newValue) {
					tableview.setMaxHeight(newValue ? TABLE_MIN_HEIGHT : TABLE_MAX_HEIGHT);
					tableview.setMinHeight(newValue ? TABLE_MIN_HEIGHT : TABLE_MAX_HEIGHT);
				}
			});
		} else if (option == 2) {
			// 2nd. Similar to first but with "animation". Here observe height changes of titled pane:
			titledPane.heightProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(final ObservableValue<? extends Number> observable, final Number oldValue, final Number newValue) {
					tableview.setMaxHeight(TABLE_MAX_HEIGHT - (TABLE_MAX_HEIGHT * (newValue.doubleValue() / TITLED_PANE_HEIGHT)));
					tableview.setMinHeight(TABLE_MAX_HEIGHT - (TABLE_MAX_HEIGHT * (newValue.doubleValue() / TITLED_PANE_HEIGHT)));
				}
			});
		}

		HBox hBox = new HBox(10);
		hBox.getChildren().addAll(tableview, titledPane);

		Scene scene = new Scene(hBox);
		stage.setTitle("Table View Sample");
		stage.setWidth(650);
		stage.setHeight(700);
		stage.setScene(scene);

		TITLED_PANE_HEIGHT = titledPane.getHeight();
		System.out.println("TITLED_PANE_HEIGHT = " + TITLED_PANE_HEIGHT);

		stage.show();

		// Determine the titledPane computed height value after stage has been shown.
		TITLED_PANE_HEIGHT = titledPane.getHeight();
		System.out.println("TITLED_PANE_HEIGHT = " + TITLED_PANE_HEIGHT);
		// .. then enable animation
		titledPane.setAnimated(true);
	}
}