package com.github.hardbyte.warfarin;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class PharmaManager extends AbstractLoggingActor {
  private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

  static public Props props() {
    return Props.create(PharmaManager.class);
  }

  private void onCreateSession(Messages.CreateSession msg) {
    log().info("Pharmaceutical Manager starting a new warfarin session");
    ActorRef sessionActor = getContext().actorOf(WarfarinSession.props(), "session-" + msg.clientId);
    getContext().watch(sessionActor);
    sessionActor.forward(msg, getContext());
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(Messages.CreateSession.class, this::onCreateSession)
        .build();
  }

}
