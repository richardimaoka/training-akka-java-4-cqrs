package org.mvrck.training.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderPutRequest {
  private final int ticketId;
  private final int userId;
  private final int quantity;

  @JsonCreator
  public OrderPutRequest(
    @JsonProperty("ticket_id") int ticketId,
    @JsonProperty("user_id") int userId,
    @JsonProperty("quantity") int quantity) {

    this.ticketId = ticketId;
    this.userId = userId;
    this.quantity = quantity;
  }

  public int getTicketId() {
    return ticketId;
  }

  public int getUserId() {
    return userId;
  }

  public int getQuantity() {
    return quantity;
  }
}
