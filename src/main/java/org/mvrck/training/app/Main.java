package org.mvrck.training.app;

import akka.actor.typed.*;

public class Main {
  public static void main(String[] args) throws Exception {
    // boot up server using the route as defined below
    var system = ActorSystem.create(GuardianActor.create(), "MaverickTraining");

    System.out.println("Server online at http://localhost:8080/\nPress RETURN to stop...");
    System.in.read(); // let it run until user presses return

    system.tell(new GuardianActor.TerminateHttp());
  }
}