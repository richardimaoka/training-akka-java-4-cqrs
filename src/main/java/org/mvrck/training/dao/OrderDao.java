package org.mvrck.training.dao;

import org.mvrck.training.config.*;
import org.mvrck.training.entity.*;
import org.seasar.doma.*;

@Dao(config = AppConfig.class)
public interface OrderDao {

  @Insert(sqlFile = true)
  int insert(Order order);
}
