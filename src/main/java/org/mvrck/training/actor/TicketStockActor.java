package org.mvrck.training.actor;

import akka.actor.typed.*;
import akka.persistence.typed.*;
import akka.persistence.typed.javadsl.*;
import org.mvrck.training.actor.TicketStockActor.*;

import java.util.*;

public class TicketStockActor extends EventSourcedBehavior<Command, Event, State> {

  /********************************************************************************
   *  Actor factory
   *******************************************************************************/
  // public: the only Behavior factory method accessed from outside the actor
  public static Behavior<Command> create(int ticketId){
    return new TicketStockActor(PersistenceId.of("TicketStockActor", Integer.toString(ticketId)));
  }

  private TicketStockActor(PersistenceId persistenceId){
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
      .onCommand(CreateTicketStock.class, command -> Effect().persist(new TicketStockCreated(command.ticketId, command.quantity, command.orderParent)));

    builder
      .forStateType(Available.class)
      .onCommand(CreateTicketStock.class, (state, command) -> Effect().none().thenRun(() -> System.out.println("CreateTicketStock is not processed when state = Available")))
      .onCommand(ProcessOrder.class, (state, command) -> {
        if (state.ticketId != command.ticketId) {
          return Effect().none().thenRun(() -> System.out.println(String.format("wrong ticket id = %d, expected = %d", command.ticketId, state.ticketId)));
        } else {
          var newQuantity = state.quantity - command.quantityDecrementedBy;
          if (newQuantity > 0) {
            return Effect()
              .persist(new OrderProcessed(command.ticketId, newQuantity, command.quantityDecrementedBy))
              .thenRun(() -> state.orderParent.tell(new OrderParentActor.CreateOrder(command.ticketId, command.userId, command.quantityDecrementedBy, command.sender)));
          } else if (newQuantity == 0) {
            return Effect()
              .persist(new SoldOut(command.ticketId, command.quantityDecrementedBy))
              .thenRun(() -> state.orderParent.tell(new OrderParentActor.CreateOrder(command.ticketId, command.userId, command.quantityDecrementedBy, command.sender)));
          } else { //newQuantity < 0
            return Effect()
              .none()
              .thenRun(() -> System.out.println(String.format("you cannot purchase qty = %d, which is more than available qty = %d", command.quantityDecrementedBy, state.quantity)));
          }
        }
      });

    builder
      .forStateType(OutOfStock.class)
      .onCommand(ProcessOrder.class, command -> Effect().reply(null, "out of stock!!!"));

    return builder.build();
  }

  @Override
  public EventHandler<State, Event> eventHandler() {
    var builder = newEventHandlerBuilder();

    builder
      .forStateType(Initialized.class)
      .onEvent(TicketStockCreated.class, (state, event) -> new Available(event.ticketId, event.quantity, event.orderParent));

    builder
      .forStateType(Available.class)
      .onEvent(OrderProcessed.class, (state, event) -> new Available(state.ticketId, event.newQuantity, state.orderParent))
      .onEvent(SoldOut.class, (state, event) -> new OutOfStock(state.ticketId));

    return builder.build();
  }

  @Override
  public Set<String> tagsFor(Event event) {
    return Set.of("ticket-stock");
  }

  /********************************************************************************
   * Command
   *******************************************************************************/
  public interface Command {}
  public static final class CreateTicketStock implements Command {
    public int ticketId;
    public int quantity;
    public ActorRef<OrderParentActor.Command> orderParent;

    public CreateTicketStock(int ticketId, int quantity, ActorRef<OrderParentActor.Command> orderParent) {
      this.ticketId = ticketId;
      this.quantity = quantity;
      this.orderParent = orderParent;
    }
  }
  public static final class ProcessOrder implements Command {
    public int ticketId;
    public int userId;
    public int quantityDecrementedBy;
    public ActorRef<OrderActor.Response> sender;

    public ProcessOrder(int ticketId, int userId, int quantityDecrementedBy, ActorRef<OrderActor.Response> sender) {
      this.ticketId = ticketId;
      this.userId = userId;
      this.quantityDecrementedBy = quantityDecrementedBy;
      this.sender = sender;
    }
  }

  /********************************************************************************
   * Event
   *******************************************************************************/
  public interface Event {}

  public static final class TicketStockCreated implements Event {
    public int ticketId;
    public int quantity;

    // https://doc.akka.io/docs/akka/current/serialization.html#serializing-actorrefs
    // All ActorRefs are serializable when using Serialization with Jackson, but in case you are writing your own serializer, you might want to know how to serialize and deserialize them properly.
    public ActorRef<OrderParentActor.Command> orderParent;

    public TicketStockCreated(int ticketId, int quantity, ActorRef<OrderParentActor.Command> orderParent) {
      this.ticketId = ticketId;
      this.quantity = quantity;
      this.orderParent = orderParent;
    }
  }

  public static final class OrderProcessed implements Event {
    public int ticketId;
    public int newQuantity;
    public int quantityDecrementedBy;

    public OrderProcessed(int ticketId, int newQuantity, int quantityDecrementedBy) {
      this.ticketId = ticketId;
      this.newQuantity = newQuantity;
      this.quantityDecrementedBy = quantityDecrementedBy;
    }
  }
  public static final class SoldOut implements Event {
    public int ticketId;
    public int quantityDecrementedBy;

    public SoldOut(int ticketId, int quantityDecrementedBy) {
      this.ticketId = ticketId;
      this.quantityDecrementedBy = quantityDecrementedBy;
    }
  }

  /********************************************************************************
   * State
   *******************************************************************************/
  public interface State {}

  private final class Initialized implements State {}

  private final class Available implements State {
    public int ticketId;
    public int quantity;
    public ActorRef<OrderParentActor.Command> orderParent;

    public Available(int ticketId, int quantity, ActorRef<OrderParentActor.Command> orderParent) {
      this.ticketId = ticketId;
      this.quantity = quantity;
      this.orderParent = orderParent;
    }
  }

  private class OutOfStock implements State {
    public int ticketId;

    public OutOfStock(int ticketId) {
      this.ticketId = ticketId;
    }
  }

}
