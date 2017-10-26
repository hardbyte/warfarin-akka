package com.github.hardbyte.warfarin;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class WarfarinDemo {

  public static void main(String[] args) {
    final ActorSystem system = ActorSystem.create("warfarin-system");

    final ActorRef pharma = system.actorOf(PharmaManager.props(), "WarfarinSession");

    for (int i = 0; i < 10; i++) {
      ActorRef patient = system.actorOf(Patient.props((long)i), "p-" + i);
      patient.tell(new Messages.Begin(pharma), ActorRef.noSender());
    }

  }
}
