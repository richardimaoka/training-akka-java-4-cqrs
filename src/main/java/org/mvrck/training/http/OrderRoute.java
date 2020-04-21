package org.mvrck.training.http;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;
import akka.http.javadsl.marshallers.jackson.*;
import akka.http.javadsl.model.*;
import akka.http.javadsl.server.*;
import org.mvrck.training.actor.*;
import org.mvrck.training.dto.*;

import java.time.*;
import java.util.concurrent.*;

public class OrderRoute extends AllDirectives {
  ActorRef<TicketStockParentActor.Command> ticketStockParent;
  ActorSystem<Void> system;

  public OrderRoute(
    ActorSystem<Void> system,
    ActorRef<TicketStockParentActor.Command> ticketStockParent){
    this.system = system;
    this.ticketStockParent = ticketStockParent;
  }

  public Route route(){
    return pathPrefix("orders", () ->
      pathEndOrSingleSlash(() ->
        entity(Jackson.unmarshaller(OrderPutRequest.class), req -> {
          CompletionStage<OrderActor.Response> completionStage = AskPattern.ask(
            ticketStockParent,
            replyTo -> new TicketStockParentActor.ProcessOrder(req.getTicketId(),req.getUserId(), req.getQuantity(), replyTo),
            Duration.ofSeconds(3),
            system.scheduler()
          );
          return onSuccess(completionStage, response -> {
            var putResponse = OrderPutResponse.convert(response);
            if(putResponse.isSuccess()) {
              return complete(StatusCodes.OK, putResponse, Jackson.marshaller());
            } else {
              return complete(StatusCodes.INTERNAL_SERVER_ERROR, "internal server error");
            }
          });
        })
      )
    );
  }
}
