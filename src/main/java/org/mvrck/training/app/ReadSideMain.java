package org.mvrck.training.app;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;
import akka.persistence.jdbc.query.javadsl.*;
import akka.persistence.query.*;
import org.mvrck.training.actor.*;
import org.mvrck.training.config.*;
import org.mvrck.training.dao.OrderDaoImpl;
import org.mvrck.training.dao.TicketStockDaoImpl;
import org.mvrck.training.entity.*;
import org.seasar.doma.jdbc.tx.*;

public class ReadSideMain {
  public static void main(String[] array) {
    var system = ActorSystem.create(Behaviors.empty(), "readside-guardian");
    var journal = PersistenceQuery.get(system).getReadJournalFor(JdbcReadJournal.class, JdbcReadJournal.Identifier());

    TransactionManager transactionManager = AppConfig.singleton().getTransactionManager();
    var orderDao = new OrderDaoImpl();
    var ticketStockDao = new TicketStockDaoImpl();

    journal
      .eventsByTag("ticket-stock", Offset.noOffset())
      .runForeach(
        envelope -> {
          if(envelope.event() instanceof TicketStockActor.TicketStockCreated) {
            var event = (TicketStockActor.TicketStockCreated) envelope.event();
            var entity = new TicketStock();
            entity.setTickeId(event.ticketId);
            entity.setQuantity(event.quantity);

            transactionManager.required(() -> {
              ticketStockDao.insert(entity);
            });

          } else if (envelope.event() instanceof TicketStockActor.OrderProcessed) {
            var event = (TicketStockActor.OrderProcessed) envelope.event();
            var entity = new TicketStock();
            entity.setTickeId(event.ticketId);
            entity.setQuantity(event.newQuantity);
            transactionManager.required(() -> {
              ticketStockDao.update(entity);
            });

          } else {
            System.out.println("Something is wrong!!! unhandled TicketStockActor Event type");
          }
        },
        system
      );

    journal
      .eventsByTag("order", Offset.noOffset())
      .runForeach(
        envelope -> {
          if(envelope.event() instanceof OrderActor.OrderCreated) {
            var event = (OrderActor.OrderCreated) envelope.event();
            var entity = new Order();
            entity.setTicketId(event.ticketId);
            entity.setUserId(event.userId);
            entity.setQuantity(event.quantity);
            transactionManager.required(() -> {
              orderDao.insert(entity);
            });

          } else {
            System.out.println("Something is wrong!!! unhandled OrderActor Event type");
          }
        },
        system
      );
  }
}

