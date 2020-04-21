package org.mvrck.training.actor;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;

import java.util.*;

public class OrderParentActor {
  /********************************************************************************
   *  Actor Behaviors
   *******************************************************************************/
  // public: the only Behavior factory method accessed from outside the actor
  public static Behavior<Command> create(){
    return Behaviors.setup(context -> behavior(context, new State()));
  }

  // private: never accessed from outside the actor
  private static Behavior<Command> behavior(ActorContext<Command> context, State state) {
    return Behaviors.receive(Command.class)
      .onMessage(CreateOrder.class, command -> behavior(context, spawnOrderChild(context, state, command)))
      .build();
  }

  //side effects and return new state
  private static State spawnOrderChild(ActorContext<Command> context, State state, CreateOrder command) {
    var orderId = UUID.randomUUID();
    var child = context.spawn(OrderActor.create(orderId.toString()), orderId.toString());

    // upon restart, this CreateOrder command is ignored by the child, as the child state will be Available
    child.tell(new OrderActor.CreateOrder(command.ticketId, command.userId, command.quantity, command.sender));
    return state.put(command.ticketId, child);
  }

  /********************************************************************************
   * Command
   *******************************************************************************/
  public interface Command {}

  public static final class CreateOrder implements Command {
    public final int ticketId;
    public final int userId;
    public final int quantity;
    public final ActorRef<OrderActor.Response> sender;

    public CreateOrder(int ticketId, int userId, int quantity, ActorRef<OrderActor.Response> sender) {
      this.ticketId = ticketId;
      this.userId = userId;
      this.quantity = quantity;
      this.sender = sender;
    }
  }

  /********************************************************************************
   * State
   *******************************************************************************/
  public static final class State {
    // There is no Java Map interface which represents immutability.
    // So you have to be careful yourself, to only assign immutable map to `children`.
    public Map<Integer, ActorRef<OrderActor.Command>> children;

    public State() {
      children = Map.of(); //immutable
    }

    public State put(int ticketId, ActorRef<OrderActor.Command> actorRef) {
      //Need to create a temporary mutable map, as Java has no copy-on-write immutable map unlike Scala
      var copiedMap = new HashMap<>(this.children);
      copiedMap.put(ticketId, actorRef);

      var state = new State();
      state.children = Map.copyOf(copiedMap); //immutable!
      return state;
    }
  }
}
