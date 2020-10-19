package com.poc.springbatch.partitioning.config;

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.SqlServerPagingQueryProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import com.poc.springbatch.partitioning.mapper.CustomerRowMapper;
import com.poc.springbatch.partitioning.model.Customer;
import com.poc.springbatch.partitioning.model.NewCustomer;
import com.poc.springbatch.partitioning.partitioner.ColumnRangePartitioner;

@Configuration
public class BatchConfig {

  private static final Logger LOGGER = LogManager.getLogger(BatchConfig.class);

  @Autowired
  private JobBuilderFactory jobBuilderFactory;
  @Autowired
  private StepBuilderFactory stepBuilderFactory;
  @Autowired
  private DataSource dataSource;

  @Bean
  public Job job() {
    return jobBuilderFactory.get("part-job").start(step()).build();
  }

  @Bean
  public Step step() {

    return stepBuilderFactory.get("part-step").partitioner(slaveStep().getName(), partitioner())
        .step(slaveStep()).taskExecutor(new SimpleAsyncTaskExecutor()).build();
  }

  @Bean
  public Partitioner partitioner() {
    ColumnRangePartitioner partitioner = new ColumnRangePartitioner();
    partitioner.setDataSource(dataSource);
    partitioner.setColumn("id");
    partitioner.setTable("customer");
    return partitioner;
  }

  @Bean
  public Step slaveStep() {
    return stepBuilderFactory.get("slaveStep").<Customer, NewCustomer>chunk(1000)
        .reader(pagingItemReader(null, null)).writer(customerItemWriter()).build();
  }

  @Bean
  @StepScope
  public ItemWriter<NewCustomer> customerItemWriter() {

    JdbcBatchItemWriter<NewCustomer> itemWriter = new JdbcBatchItemWriter<>();
    itemWriter.setDataSource(dataSource);
    itemWriter.setSql("INSERT INTO NEW_CUSTOMER VALUES (:id, :dob, :firstname, :lastname)");
    itemWriter
        .setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
    itemWriter.afterPropertiesSet();
    return itemWriter;
  }

  @Bean
  @StepScope
  public ItemReader<? extends Customer> pagingItemReader(
      @Value("#{stepExecutionContext['minValue']}") Long minValue,
      @Value("#{stepExecutionContext['maxValue']}") Long maxValue) {
    LOGGER.info("reading from " + minValue + " to max " + maxValue);

    SqlServerPagingQueryProvider queryProvider = new SqlServerPagingQueryProvider();

    Map<String, Order> sortKeys = new HashMap<>();
    sortKeys.put("id", Order.ASCENDING);
    queryProvider.setFromClause("from customer");
    queryProvider.setSelectClause("id, firstname, lastname, dob");

    queryProvider.setWhereClause("where id>=" + minValue + " and id <= " + maxValue);
    queryProvider.setSortKeys(sortKeys);

    JdbcPagingItemReader<Customer> reader = new JdbcPagingItemReader<>();
    reader.setDataSource(dataSource);
    reader.setFetchSize(10);
    reader.setRowMapper(new CustomerRowMapper());
    reader.setQueryProvider(queryProvider);
    return reader;
  }

}
