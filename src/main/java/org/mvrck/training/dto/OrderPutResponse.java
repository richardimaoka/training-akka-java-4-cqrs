package org.mvrck.training.dto;

import org.mvrck.training.actor.*;

public class OrderPutResponse {

  public interface Result {
    boolean isSuccess();
  }

  public static class Success implements Result {
    public final int ticketId;
    public final int userId;
    public final int quantity;

    public Success(int ticketId, int userId, int quantity) {
      this.ticketId = ticketId;
      this.userId = userId;
      this.quantity = quantity;
    }

    @Override
    public boolean isSuccess() {
      return true;
    }
  }

  public static class Error implements Result {
    public final String errorMessage;

    public Error(String errorMessage) {
      this.errorMessage = errorMessage;
    }

    @Override
    public boolean isSuccess() {
      return false;
    }
  }

  public static Result convert(OrderActor.Response actorResponse) {
    if ( actorResponse instanceof OrderActor.GetOrderResponse ) {
      var getOrderResponse = (OrderActor.GetOrderResponse) actorResponse;
      return new Success(getOrderResponse.ticketId, getOrderResponse.userId, getOrderResponse.quantity);
    } else if (actorResponse instanceof OrderActor.ErrorResponse) {
      var errorResponse = (OrderActor.ErrorResponse) actorResponse;
      return new Error(errorResponse.errorMessage);
    } else {
      throw new RuntimeException("unexpected response from OrderActor");
    }
  }
}
