package org.mvrck.training.config;

import org.seasar.doma.*;
import org.seasar.doma.jdbc.*;
import org.seasar.doma.jdbc.dialect.*;
import org.seasar.doma.jdbc.tx.*;

import javax.sql.*;

@SingletonConfig
public class AppConfig implements Config {
  private static final AppConfig CONFIG = new AppConfig();

  private final Dialect dialect;

  private final LocalTransactionDataSource dataSource;

  private final TransactionManager transactionManager;

  private AppConfig() {
    try {
      var typeSafeConfig = com.typesafe.config.ConfigFactory.load();
      var url = typeSafeConfig.getString("slick.db.url");
      var user = typeSafeConfig.getString("slick.db.user");
      var password = typeSafeConfig.getString("slick.db.password");
      dialect = new MysqlDialect();
      dataSource = new LocalTransactionDataSource(url, user, password);
      transactionManager = new LocalTransactionManager(dataSource.getLocalTransaction(getJdbcLogger()));
    } catch (com.typesafe.config.ConfigException e) {
      throw new RuntimeException("Missing config values: one or more of 'database.url', 'database.user', 'database.password' is not found.", e);
    }
  }

  @Override
  public JdbcLogger getJdbcLogger() {
    return new JdbcNoLogging();
  }

  @Override
  public Dialect getDialect() {
    return dialect;
  }

  @Override
  public DataSource getDataSource() {
    return dataSource;
  }

  @Override
  public TransactionManager getTransactionManager() {
    return transactionManager;
  }

  public static AppConfig singleton() {
    return CONFIG;
  }
}
