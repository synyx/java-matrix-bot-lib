package org.synyx.matrix.bot;

/**
 * An exception that was not recoverable from by the matrix client itself occurred while communicating with the matrix server.
 */
public class MatrixCommunicationException extends RuntimeException {

  public MatrixCommunicationException(String message) {

    super(message);
  }

  public MatrixCommunicationException(String message, Throwable cause) {

    super(message, cause);
  }
}
