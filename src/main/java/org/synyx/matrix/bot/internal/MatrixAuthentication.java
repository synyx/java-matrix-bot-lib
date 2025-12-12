package org.synyx.matrix.bot.internal;

import org.synyx.matrix.bot.domain.MatrixUserId;

import java.util.Optional;

public class MatrixAuthentication {

  private final String username;
  private final String password;

  private MatrixUserId userId;
  private String bearerToken;

  public MatrixAuthentication(String username, String password) {

    this.username = username;
    this.password = password;
    this.bearerToken = null;
  }

  public boolean isAuthenticated() {

    return bearerToken != null;
  }

  public void clear() {

    bearerToken = null;
    userId = null;
  }

  public String getUsername() {

    return username;
  }

  public String getPassword() {

    return password;
  }

  public Optional<String> getBearerToken() {

    return Optional.ofNullable(bearerToken);
  }

  public Optional<MatrixUserId> getUserId() {

    return Optional.ofNullable(userId);
  }

  public void setUserId(MatrixUserId userId) {

    this.userId = userId;
  }

  public void setBearerToken(String bearerToken) {

    this.bearerToken = bearerToken;
  }
}
