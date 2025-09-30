package org.synyx.matrix.bot;

public class MatrixCommunicationException extends RuntimeException {

  public MatrixCommunicationException(String message) {

    super(message);
  }

  public MatrixCommunicationException(String message, Throwable cause) {

    super(message, cause);
  }
}
