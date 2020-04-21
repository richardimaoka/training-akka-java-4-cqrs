package org.mvrck.training.http;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;
import akka.http.javadsl.*;
import akka.stream.*;
import org.mvrck.training.actor.*;

public class GuardianActor {
  /********************************************************************************
   *  Actor Behaviors
   *******************************************************************************/
  public static Behavior<Message> create() {
    return Behaviors.setup(context -> {
      /*********************************************************************************
       * Set up actor hierarchy on startup
       *********************************************************************************/
      var orderParent = context.spawn(OrderParentActor.create(), "order-parent");
      var ticketStockParent = context.spawn(TicketStockParentActor.create(orderParent), "ticket-stock-parent");

      ticketStockParent.tell(new TicketStockParentActor.CreateTicketStock(1, 5000));
      ticketStockParent.tell(new TicketStockParentActor.CreateTicketStock(2, 2000));

      /*********************************************************************************
       * Set up HTTP server
       *********************************************************************************/
      var materializer = Materializer.createMaterializer(context.getSystem());

      var http = Http.get(context.getSystem().classicSystem());
      var allRoute = new AllRoute(context.getSystem(), ticketStockParent);
      var routeFlow = allRoute.route().flow(context.getSystem().classicSystem(), materializer);
      var binding = http.bindAndHandle(routeFlow, ConnectHttp.toHost("localhost", 8080), materializer);

      // Shutdown behavior
      return Behaviors.receive(Message.class)
        .onMessage(TerminateHttp.class, message -> {
            binding
              .thenCompose(ServerBinding::unbind)
              .thenAccept(unbound -> context.getSystem().terminate());
            return Behaviors.empty();
          }
        ).build();
    });
  }

  /********************************************************************************
   * Actor Messages
   ********************************************************************************/
  public interface Message {}
  public static class TerminateHttp implements Message {}

  // Actor Messages are in GuardianActor
}
