package com.github.hardbyte.warfarin;

import akka.actor.ActorRef;
import com.n1analytics.paillier.EncryptedNumber;
import com.n1analytics.paillier.PaillierPublicKey;

import java.util.ArrayList;

public class Messages {

  static final class CreateSession {
    long clientId;
    CreateSession(long clientId){this.clientId = clientId;}
  }

  static final class EncryptedCoefficients {
    long clientId;
    PaillierPublicKey publicKey;
    ArrayList<EncryptedNumber> encryptedWeights;

    EncryptedCoefficients(long clientId, PaillierPublicKey pk, ArrayList<EncryptedNumber> encryptedWeights) {
      this.clientId = clientId;
      this.publicKey = pk;
      this.encryptedWeights = encryptedWeights;
    }
  }

  public static final class DecryptionRequest {
    EncryptedNumber ciphertext;
    DecryptionRequest(EncryptedNumber ciphertext) {
      this.ciphertext = ciphertext;
    }
  }
  public static final class ObfuscatedDosage {
    double result;
    public ObfuscatedDosage(double result) {
      this.result = result;
    }
  }

  public static final class Begin {
    ActorRef pharma;
    Begin(ActorRef pharma){this.pharma=pharma;}
  }

}
