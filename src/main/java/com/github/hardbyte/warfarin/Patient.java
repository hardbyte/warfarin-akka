package com.github.hardbyte.warfarin;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.n1analytics.paillier.EncryptedNumber;

import java.util.ArrayList;
import java.util.Random;

public class Patient extends AbstractLoggingActor {
  private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
  private final long patientId;
  private final double random;

  static public Props props(Long patientId) {
    return Props.create(Patient.class, patientId);
  }

  public Patient(long id) {
    this.patientId = id;
    Random r = new Random();
    this.random = r.nextDouble();
  }

  private void onBeginComputation(Messages.Begin msg) {
    log().info("The Patient is starting the protocol");
    ActorRef pharmaActor = msg.pharma;
    pharmaActor.tell(new Messages.CreateSession(this.patientId), getSelf());
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(Messages.Begin.class, this::onBeginComputation)
        .match(Messages.EncryptedCoefficients.class, this::onReceivingWeights)
        .match(Messages.ObfuscatedDosage.class, this::onDosage)
        .build();
  }

  private void onDosage(Messages.ObfuscatedDosage msg) {
    double dosage = msg.result - this.random;
    log().info("Patient saved! Dosage will be {}", dosage);
  }

  private void onReceivingWeights(Messages.EncryptedCoefficients msg) {
    log().info("Patient {} received {} encrypted weights", patientId, msg.encryptedWeights.size());

    EncryptedNumber encryptedDosage = computeEncryptedDosage(msg.encryptedWeights);
    EncryptedNumber obfuscatedEncryptedDosage = encryptedDosage.add(this.random);

    getSender().tell(new Messages.DecryptionRequest(obfuscatedEncryptedDosage), getSelf());
  }

  private EncryptedNumber computeEncryptedDosage(ArrayList<EncryptedNumber> encryptedWeights) {

    return encryptedWeights.stream()
        // TODO: for now we just use a single constant instead of local data...
        .map(e -> e.multiply(patientId))
        .reduce(EncryptedNumber::add)
        .get();
  }
}
