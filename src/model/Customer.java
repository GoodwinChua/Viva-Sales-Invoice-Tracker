package model;

import javafx.beans.property.SimpleStringProperty;

/**
 * Created by Goodwin Chua on 8 Sep 2016.
 */
public class Customer {

    private final SimpleStringProperty name;
    private final SimpleStringProperty tel;
    private final SimpleStringProperty address;

    public Customer(String name, String tel, String address) {
        this.name = new SimpleStringProperty(name);
        this.tel = new SimpleStringProperty(tel);
        this.address = new SimpleStringProperty(address);
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public String getTel() {
        return tel.get();
    }

    public SimpleStringProperty telProperty() {
        return tel;
    }

    public String getAddress() {
        return address.get();
    }

    public SimpleStringProperty addressProperty() {
        return address;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public void setAddress(String address) {
        this.address.set(address);
    }

    public void setTel(String tel) {
        this.tel.set(tel);
    }

    @Override
    public String toString() {
        return getName();
    }

}
