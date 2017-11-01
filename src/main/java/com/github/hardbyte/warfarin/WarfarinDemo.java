package com.github.hardbyte.warfarin;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

public class WarfarinDemo {

  public static void main(String[] args) {
    final ActorSystem system = ActorSystem.create("warfarin-system");
    FiniteDuration timeout = FiniteDuration.create(10, TimeUnit.SECONDS);
    final ActorRef pharma = system.actorOf(PharmaManager.props(timeout), "WarfarinSession");

    for (int i = 0; i < 10; i++) {
      ActorRef patient = system.actorOf(Doctor.props((long)i), "p-" + i);
      patient.tell(new Messages.Begin(pharma), ActorRef.noSender());
    }

  }
}
