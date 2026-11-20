package org.synyx.matrix.bot.internal;

import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.synyx.matrix.bot.domain.MatrixUserId;

public class MatrixAuthentication {

  private final String username;
  private final String password;

  @Nullable private MatrixUserId userId;
  @Nullable private String bearerToken;

  public MatrixAuthentication(String username, String password) {

    this.username = username;
    this.password = password;
    this.userId = null;
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

  public Optional<MatrixUserId> getUserId() {
    return Optional.ofNullable(userId);
  }

  public void setUserId(@Nullable MatrixUserId userId) {
    this.userId = userId;
  }

  public Optional<String> getBearerToken() {
    return Optional.ofNullable(bearerToken);
  }

  public void setBearerToken(@Nullable String bearerToken) {
    this.bearerToken = bearerToken;
  }
}
