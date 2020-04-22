package org.mvrck.training.config;

import org.seasar.doma.jdbc.*;
import org.slf4j.event.*;

import java.util.function.*;

public class JdbcNoLogging extends AbstractJdbcLogger<Level> {

  public JdbcNoLogging() {
    super(Level.INFO);
  }

  @Override
  protected void log(
    Level level,
    String callerClassName,
    String callerMethodName,
    Throwable throwable,
    Supplier<String> messageSupplier) {
    //no logging
  }
}
