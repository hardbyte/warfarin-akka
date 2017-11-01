package com.github.hardbyte.warfarin;

import static org.junit.Assert.*;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import akka.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class DoctorActorTest {

    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }


  @Test
  public void testReplyWithEmptyReadingIfNoTemperatureIsKnown() {
    TestKit probe = new TestKit(system);
    TestKit pharmaProbe = new TestKit(system);
    ActorRef deviceActor = system.actorOf(Doctor.props(42L));
    deviceActor.tell(new Messages.Begin(pharmaProbe.getRef()), probe.getRef());

    Messages.CreateSession m = pharmaProbe.expectMsgClass(Messages.CreateSession.class);
    assertEquals(42L, m.clientId);

  }
}