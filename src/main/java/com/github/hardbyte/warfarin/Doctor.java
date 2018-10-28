package com.github.hardbyte.warfarin;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.n1analytics.paillier.EncryptedNumber;

import java.util.ArrayList;
import java.util.Random;

public class Doctor extends AbstractLoggingActor {
  private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
  private final long patientId;
  private final double random;

  static Props props(Long patientId) {
    return Props.create(Doctor.class, patientId);
  }

  public Doctor(long id) {
    this.patientId = id;
    Random r = new Random();
    this.random = r.nextDouble();
  }

  private void onBeginComputation(Messages.Begin msg) {
    log().info("Doctor {} is starting the protocol", patientId);
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
    double dosage = Math.pow(msg.result - this.random, 2);
    log().info("Doctor {} gets dosage as: {} [mg/week]",
        this.patientId,
        String.format("%.2f", dosage));
  }

  private void onReceivingWeights(Messages.EncryptedCoefficients msg) {
    log().info("Doctor {} received {} encrypted weights",
        patientId,
        msg.encryptedWeights.size());

    EncryptedNumber encryptedDosage = computeEncryptedDosage(msg.encryptedWeights);
    EncryptedNumber obfuscatedEncryptedDosage = encryptedDosage.add(this.random);

    getSender().tell(new Messages.DecryptionRequest(obfuscatedEncryptedDosage), getSelf());
  }

  private EncryptedNumber computeEncryptedDosage(ArrayList<EncryptedNumber> encryptedWeights) {

    return encryptedWeights.stream()
        .map(e -> e.multiply(patientId))
        .reduce(EncryptedNumber::add)
        .get();
  }
}
