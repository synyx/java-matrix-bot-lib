package org.synyx.matrix.bot.internal.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class ReactionRelatesToDto {

  public ReactionRelatesToDto() {

  }

  public ReactionRelatesToDto(String eventId, String key) {

    this.eventId = eventId;
    this.key = key;
  }

  @JsonProperty("event_id")
  private String eventId;

  @JsonProperty("key")
  private String key;

  @JsonProperty("rel_type")
  public String getRelType() {

    return "m.annotation";
  }

  public String getEventId() {

    return eventId;
  }

  public void setEventId(String eventId) {

    this.eventId = eventId;
  }

  public String getKey() {

    return key;
  }

  public void setKey(String key) {

    this.key = key;
  }

  @Override
  public boolean equals(Object o) {

    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ReactionRelatesToDto that = (ReactionRelatesToDto) o;
    return Objects.equals(eventId, that.eventId) && Objects.equals(key, that.key);
  }

  @Override
  public int hashCode() {

    return Objects.hash(eventId, key);
  }

  @Override
  public String toString() {

    return "ReactionRelatesToDto{" +
        "eventId='" + eventId + '\'' +
        ", key='" + key + '\'' +
        '}';
  }
}
