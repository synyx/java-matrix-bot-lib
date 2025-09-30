package org.synyx.matrix.bot.internal.api;

import java.io.IOException;
import java.net.http.HttpResponse;

public class MatrixApiException extends Exception {

  public MatrixApiException(String performedAction, HttpResponse<?> response) {

    super("%s failed - %d".formatted(performedAction, response.statusCode()));
  }

  public MatrixApiException(String performedAction, IOException ioException) {

    super("%s failed - %s".formatted(performedAction, ioException.getClass().getName()), ioException);
  }
}
