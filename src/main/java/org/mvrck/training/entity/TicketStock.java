package org.mvrck.training.entity;

import org.seasar.doma.*;

@Entity
@Table(name = "ticket_stocks")
public class TicketStock {
  @Id
  @Column(name = "ticket_id")
  Integer ticketId;

  @Column(name = "quantity")
  int quantity;

  public int getTicketId() {
    return this.ticketId;
  }

  public int getQuantity() {
    return this.quantity;
  }

  public void setTickeId(int ticketId) {
    this.ticketId = ticketId;
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }
}
