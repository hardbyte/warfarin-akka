package com.github.hardbyte.warfarin;

import akka.actor.AbstractLoggingActor;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.n1analytics.paillier.EncryptedNumber;
import com.n1analytics.paillier.PaillierContext;
import com.n1analytics.paillier.PaillierPrivateKey;
import com.n1analytics.paillier.PaillierPublicKey;
import scala.concurrent.duration.FiniteDuration;

import java.util.ArrayList;

public class WarfarinSession extends AbstractLoggingActor {
  private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
  private final double[] weights = {4.0376, 5.6044, -0.2546, -0.2614, 0.0118, 0.0087, 0.0134, 0.0128, -0.6752, -0.1092, 0.406, -0.276, 0.0443, -0.1032, 1.2799, 1.1816, -0.5695, -0.5503, -1.6974, -0.8677, -0.4854, -0.5211, -0.9357, -1.0616, -1.9206, -2.3312, -0.2188, -1.2948, 1.0409, 7.5016};

  private Cancellable sessionTimeoutTimer;

  private long sessionID;
  private PaillierPrivateKey sk;
  private PaillierPublicKey pk;

  WarfarinSession(FiniteDuration timeout) {
    // Schedule a message to give up on this session
    sessionTimeoutTimer = getContext()
        .getSystem()
        .scheduler()
        .scheduleOnce(
            timeout,
            getSelf(),
            new Messages.SessionTimeout(),
            getContext().dispatcher(),
            getSelf()
        );
  }

  static Props props(FiniteDuration timeout) {
    return Props.create(WarfarinSession.class, timeout);
  }

  private void onCreateSession(Messages.CreateSession msg) {
    log().info("Warfarin Session at Pharmaceutical company starting");
    this.sessionID = msg.clientId;
    this.sk = PaillierPrivateKey.create(4096);
    this.pk = sk.getPublicKey();
    PaillierContext paillierContext = pk.createSignedContext();

    log().info("Session keypair generated");

    ArrayList<EncryptedNumber> encryptedWeights = new ArrayList<>();

    for (double weight : weights) {
      EncryptedNumber ciphertext = paillierContext.encrypt(weight);
      encryptedWeights.add(ciphertext);
    }

    log().info("Weights have been encrypted");

    getSender().tell(new Messages.EncryptedCoefficients(msg.clientId, pk, encryptedWeights), getSelf());
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(Messages.CreateSession.class, this::onCreateSession)
        .match(Messages.DecryptionRequest.class, this::onDecrypt)
        .match(Messages.SessionTimeout.class, this::onSessionTimeout)
        .build();
  }

  private void onSessionTimeout(Messages.SessionTimeout msg) {
    log().warning("Shutting down as we ran out of time");
    getContext().stop(getSelf());
  }

  private void onDecrypt(Messages.DecryptionRequest msg) {
    log().info("Decryption step");
    double result = this.sk.decrypt(msg.ciphertext).decodeDouble();
    log().info("Result: {}", String.format("%.2f", result));
    getSender().tell(new Messages.ObfuscatedDosage(result), getSelf());

    log().info("Shutting down session");
    sessionTimeoutTimer.cancel();
    getContext().stop(getSelf());
  }

}
