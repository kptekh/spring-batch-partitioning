package com.poc.springbatch.partitioning.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import com.poc.springbatch.partitioning.model.Customer;

public class CustomerRowMapper implements RowMapper<Customer> {

  @Override
  public Customer mapRow(ResultSet resultSet, int rowNum) throws SQLException {
    Customer customer = new Customer();
    customer.setId(resultSet.getLong("id"));
    customer.setFirstname(resultSet.getString("firstname"));
    customer.setLastname(resultSet.getString("lastname"));
    customer.setDob(resultSet.getString("dob"));
    return customer;
  }

}
