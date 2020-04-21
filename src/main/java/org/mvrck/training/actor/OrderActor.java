package org.mvrck.training.actor;

import akka.actor.typed.*;
import akka.persistence.typed.*;
import akka.persistence.typed.javadsl.*;
import org.mvrck.training.actor.OrderActor.*;

import java.util.*;

public class OrderActor extends EventSourcedBehavior<Command, Event, State> {
  /********************************************************************************
   *  Actor Behaviors
   *******************************************************************************/
  // public: the only Behavior factory method accessed from outside the actor
  public static Behavior<Command> create(String orderId){
    return new OrderActor(PersistenceId.of("OrderActor", orderId));
  }

  private OrderActor(PersistenceId persistenceId){
    super(persistenceId);
  }

  @Override
  public State emptyState() {
    return new Initialized();
  }

  /********************************************************************************
   * Persistence
   *******************************************************************************/
  @Override
  public CommandHandler<Command, Event, State> commandHandler(){
    var builder = newCommandHandlerBuilder();

    builder
      .forStateType(Initialized.class)
      .onCommand(CreateOrder.class,
        command -> Effect()
          .persist(new OrderCreated(command.ticketId, command.userId, command.quantity))
          .thenReply(command.sender, state -> new GetOrderResponse(command.ticketId, command.userId, command.quantity)));

    builder
      .forStateType(Created.class)
      .onCommand(GetOrder.class,
        (state, command) -> Effect()
          .none()
          .thenReply(command.sender, (s) -> new GetOrderResponse(state.ticketId, state.userId, state.quantity))
      );

    return builder.build();
  }

  @Override
  public EventHandler<State, Event> eventHandler() {
    var builder = newEventHandlerBuilder();

    builder
      .forStateType(Initialized.class)
      .onEvent(OrderCreated.class, (state, event) -> new Created(event.ticketId, event.userId, event.quantity));

    return builder.build();
  }

  @Override
  public Set<String> tagsFor(Event event) {
    return Set.of("order");
  }

  /********************************************************************************
   *  Actor Messages
   *******************************************************************************/
  public interface Command {}

  public static final class CreateOrder implements Command {
    public final int ticketId;
    public final int userId;
    public final int quantity;
    public final ActorRef<Response> sender;

    public CreateOrder(int ticketId, int userId, int quantity, ActorRef<Response> sender) {
      this.ticketId = ticketId;
      this.userId = userId;
      this.quantity = quantity;
      this.sender = sender;
    }
  }

  public static final class GetOrder implements Command {
    public final UUID orderId;
    public final ActorRef<Response> sender;

    public GetOrder(UUID orderId, ActorRef<Response> sender) {
      this.orderId = orderId;
      this.sender = sender;
    }
  }

  /********************************************************************************
   * Event
   *******************************************************************************/
  public interface Event {}

  public static final class OrderCreated implements Event {
    public int ticketId;
    public int userId;
    public int quantity;

    public OrderCreated(int ticketId, int userId, int quantity) {
      this.ticketId = ticketId;
      this.userId = userId;
      this.quantity = quantity;
    }
  }

  /********************************************************************************
   * State
   *******************************************************************************/
  public interface State {}

  private final class Initialized implements State {}

  private final class Created implements State {
    public int ticketId;
    public int userId;
    public int quantity;

    public Created(int ticketId, int userId, int quantity) {
      this.ticketId = ticketId;
      this.userId = userId;
      this.quantity = quantity;
    }
  }

  /********************************************************************************
   *  Actor Response
   *******************************************************************************/
  public interface Response {}

  public static final class ErrorResponse implements Response {
    public final String errorMessage;

    public ErrorResponse(String errorMessage) {
      this.errorMessage = errorMessage;
    }
  }

  public static final class GetOrderResponse implements Response {
    public final int ticketId;
    public final int userId;
    public final int quantity;

    public GetOrderResponse(int ticketId, int userId, int quantity) {
      this.ticketId = ticketId;
      this.userId = userId ;
      this.quantity = quantity;
    }
  }
}
