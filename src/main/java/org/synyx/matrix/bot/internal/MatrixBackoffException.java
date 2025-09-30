package org.synyx.matrix.bot.internal;

public class MatrixBackoffException extends RuntimeException {

  public MatrixBackoffException(String message, Throwable e) {

    super(message, e);
  }
}
