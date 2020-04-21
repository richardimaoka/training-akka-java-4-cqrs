package org.mvrck.training.app;

import akka.actor.typed.*;
import akka.persistence.jdbc.query.javadsl.*;
import akka.persistence.query.*;


public class ReadSideMain {
  public static void main(String[] array) {
    ActorSystem<Void> system = null;
    var journal = PersistenceQuery.get(system).getReadJournalFor(JdbcReadJournal.class, JdbcReadJournal.Identifier());

    journal
      .eventsByTag("ticket-stock", Offset.noOffset())
      .runForeach(
        envelope -> System.out.println(envelope.event()),
        system
      );

    journal
      .eventsByTag("order", Offset.noOffset())
      .runForeach(
        envelope -> System.out.println(envelope.event()),
        system
      );
  }
}

