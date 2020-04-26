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

public class ReadSideMain {
  public static void main(String[] array) {
    var system = ActorSystem.create(Behaviors.empty(), "MaverickTraining");
    var journal = PersistenceQuery.get(system).getReadJournalFor(JdbcReadJournal.class, JdbcReadJournal.Identifier());

    var transactionManager = AppConfig.singleton().getTransactionManager();
    var orderDao = new OrderDaoImpl();
    var ticketStockDao = new TicketStockDaoImpl();

    journal
      .eventsByTag("ticket-stock", Offset.noOffset())
      .runForeach(
        envelope -> {
          if(envelope.event() instanceof TicketStockActor.TicketStockCreated) {
            var event = (TicketStockActor.TicketStockCreated) envelope.event();
            var entity = new TicketStock();
            System.out.println("TicketStockCreated: " + event.ticketId + ", " + event.quantity);
            entity.setTickeId(event.ticketId);
            entity.setQuantity(event.quantity);
            transactionManager.required(() -> {
              ticketStockDao.insert(entity);
            });

          } else if (envelope.event() instanceof TicketStockActor.OrderProcessed) {
            var event = (TicketStockActor.OrderProcessed) envelope.event();
            System.out.println("OrderProcessed:     " + event.ticketId + ", " + event.newQuantity);
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

    System.out.println("----------------------------------------------");
    System.out.println("----------------------------------------------");

    journal
      .eventsByTag("order", Offset.noOffset())
      .runForeach(
        envelope -> {
          if(envelope.event() instanceof OrderActor.OrderCreated) {
            var event = (OrderActor.OrderCreated) envelope.event();
            System.out.println("OrderCreated:       " + event.id + ", " + event.ticketId + ", " + event.userId + ", " + event.quantity);
            var entity = new Order();
            entity.setId(event.id);
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

