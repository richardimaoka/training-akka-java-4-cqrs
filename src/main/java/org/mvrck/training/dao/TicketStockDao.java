package org.mvrck.training.dao;

import org.mvrck.training.config.*;
import org.mvrck.training.entity.*;
import org.seasar.doma.*;

@Dao(config = AppConfig.class)
public interface TicketStockDao {
  @Update
  int update(TicketStock ticket);

  @Insert
  int insert(TicketStock ticket);
}
