package org.mvrck.training.entity;

import org.seasar.doma.*;

@Entity
@Table(name = "orders")
public class Order {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Integer id;

  @Column(name = "ticket_id")
  Integer ticketId;

  @Column(name = "user_id")
  Integer userId;

  @Column(name = "quantity")
  Integer quantity;

  public int getTicketId() {
    return this.ticketId;
  }

  public void setTicketId(int ticketId) {
    this.ticketId = ticketId;
  }

  public int getUserId() {
    return this.ticketId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public int getQuantity() {
    return this.quantity;
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }
}
