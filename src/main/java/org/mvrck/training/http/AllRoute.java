package org.mvrck.training.http;

import akka.actor.typed.*;
import akka.http.javadsl.server.*;
import org.mvrck.training.actor.*;

public class AllRoute extends AllDirectives {
  ActorSystem<Void> system;
  OrderRoute orderRoute;

  public AllRoute(
    ActorSystem<Void> system,
    ActorRef<TicketStockParentActor.Command> ticketStockParent
  ){
    this.system = system;
    this.orderRoute = new OrderRoute(system, ticketStockParent);
  }

  public Route route(){
    return concat(
      orderRoute.route()
    );
  }
}
