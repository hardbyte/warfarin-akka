package com.github.hardbyte.warfarin;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import scala.concurrent.duration.FiniteDuration;


public class PharmaManager extends AbstractLoggingActor {
  private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
  private FiniteDuration sessionTimeout;

  static public Props props(FiniteDuration timeout) {
    return Props.create(PharmaManager.class, timeout);
  }

  PharmaManager(FiniteDuration timeout) {
    this.sessionTimeout = timeout;
  }


  private void onCreateSession(Messages.CreateSession msg) {
    log().info("Pharmaceutical Manager starting a new warfarin session");
    ActorRef sessionActor = getContext().actorOf(WarfarinSession.props(this.sessionTimeout), "session-" + msg.clientId);
    getContext().watch(sessionActor);
    sessionActor.forward(msg, getContext());


  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(Messages.CreateSession.class, this::onCreateSession)
        .match(Terminated.class, this::onSessionFinish)
        .build();
  }

  private void onSessionFinish(Terminated msg) {
    log().debug("Session actor has finished {}", msg.getActor());
  }

}
