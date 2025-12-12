package org.synyx.matrix.bot.internal.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public class ReactionDto {

  public ReactionDto() {}

  public ReactionDto(ReactionRelatesToDto relatesTo) {

    this.relatesTo = relatesTo;
  }

  @JsonProperty("m.relates_to")
  private ReactionRelatesToDto relatesTo;

  public ReactionRelatesToDto getRelatesTo() {

    return relatesTo;
  }

  public void setRelatesTo(ReactionRelatesToDto relatesTo) {

    this.relatesTo = relatesTo;
  }

  @Override
  public boolean equals(Object o) {

    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ReactionDto that = (ReactionDto) o;
    return Objects.equals(relatesTo, that.relatesTo);
  }

  @Override
  public int hashCode() {

    return Objects.hashCode(relatesTo);
  }

  @Override
  public String toString() {

    return "ReactionDto{" + "relatesTo=" + relatesTo + '}';
  }
}
