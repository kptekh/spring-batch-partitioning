package com.poc.springbatch.partitioning.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity(name = "NEW_CUSTOMER")
@Table
public class NewCustomer {

  @Id
  private Long id;

  @Column()
  private String firstname;
  @Column()
  private String lastname;
  @Column()
  private String dob;


  public NewCustomer() {
    super();
  }

  public NewCustomer(Long id, String firstName, String lastName, String dob) {
    super();
    this.id = id;
    this.firstname = firstName;
    this.lastname = lastName;
    this.dob = dob;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getFirstname() {
    return firstname;
  }

  public void setFirstname(String firstname) {
    this.firstname = firstname;
  }

  public String getLastname() {
    return lastname;
  }

  public void setLastname(String lastname) {
    this.lastname = lastname;
  }

  public String getDob() {
    return dob;
  }

  public void setDob(String dob) {
    this.dob = dob;
  }

  @Override
  public String toString() {
    return "Customer [id=" + id + ", firstname=" + firstname + ", lastname=" + lastname + ", dob="
        + dob + "]";
  }


}
