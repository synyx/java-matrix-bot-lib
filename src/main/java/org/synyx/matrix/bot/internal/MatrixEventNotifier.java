package org.synyx.matrix.bot.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.synyx.matrix.bot.MatrixEventConsumer;
import org.synyx.matrix.bot.domain.MatrixEventId;
import org.synyx.matrix.bot.domain.MatrixRoomId;
import org.synyx.matrix.bot.domain.MatrixUserId;
import org.synyx.matrix.bot.domain.event.MatrixMessageEvent;
import org.synyx.matrix.bot.domain.event.MatrixRoomInviteEvent;
import org.synyx.matrix.bot.domain.event.MatrixSelfLeaveRoomEvent;
import org.synyx.matrix.bot.domain.event.MatrixUserJoinRoomEvent;
import org.synyx.matrix.bot.domain.event.MatrixUserLeaveRoomEvent;
import org.synyx.matrix.bot.domain.message.MatrixEmoteMessage;
import org.synyx.matrix.bot.domain.message.MatrixMessage;
import org.synyx.matrix.bot.domain.message.MatrixMessageType;
import org.synyx.matrix.bot.domain.message.MatrixNoticeMessage;
import org.synyx.matrix.bot.domain.message.MatrixTextMessage;
import org.synyx.matrix.bot.internal.api.dto.ClientEventDto;
import org.synyx.matrix.bot.internal.api.dto.InvitedRoomDto;
import org.synyx.matrix.bot.internal.api.dto.JoinedRoomDto;
import org.synyx.matrix.bot.internal.api.dto.MembershipStateDto;
import org.synyx.matrix.bot.internal.api.dto.RoomTimelineDto;
import org.synyx.matrix.bot.internal.api.dto.StrippedStateEventDto;
import org.synyx.matrix.bot.internal.api.dto.SyncResponseDto;
import org.synyx.matrix.bot.internal.api.dto.event.MemberEventContentDto;
import org.synyx.matrix.bot.internal.api.dto.event.MessageEventContentDto;

public class MatrixEventNotifier {

  private static final Logger LOG = LoggerFactory.getLogger(MatrixEventNotifier.class);

  private final ObjectMapper objectMapper;
  private final MatrixEventConsumer consumer;

  private MatrixEventNotifier(ObjectMapper objectMapper, MatrixEventConsumer consumer) {

    this.objectMapper = objectMapper;
    this.consumer = consumer;
  }

  public static Optional<MatrixEventNotifier> from(
      ObjectMapper objectMapper, MatrixEventConsumer consumer) {

    if (consumer == null) {
      return Optional.empty();
    }

    return Optional.of(new MatrixEventNotifier(objectMapper, consumer));
  }

  public MatrixEventConsumer getConsumer() {
    return consumer;
  }

  public void notifyFromSynchronizationResponse(
      InternalMatrixState state, SyncResponseDto syncResponse) {

    final var maybeRooms = Optional.ofNullable(syncResponse.rooms());
    final var invitedRooms =
        maybeRooms
            .flatMap(syncRoomsDto -> Optional.ofNullable(syncRoomsDto.invite()))
            .orElseGet(HashMap::new);

    for (var entry : invitedRooms.entrySet()) {

      final var roomId = MatrixRoomId.from(entry.getKey()).orElseThrow(IllegalStateException::new);

      Optional.ofNullable(entry.getValue())
          .map(InvitedRoomDto::inviteState)
          .map(InvitedRoomDto.InviteStateDto::events)
          .orElseGet(List::of)
          .forEach(eventDto -> notifyAboutInviteEvent(state, roomId, eventDto));
    }

    final var joinedRooms =
        maybeRooms
            .flatMap(syncRoomsDto -> Optional.ofNullable(syncRoomsDto.join()))
            .orElseGet(HashMap::new);

    for (var entry : joinedRooms.entrySet()) {

      final var roomId = MatrixRoomId.from(entry.getKey()).orElseThrow(IllegalStateException::new);
      Optional.ofNullable(entry.getValue())
          .map(JoinedRoomDto::timeline)
          .map(RoomTimelineDto::events)
          .orElseGet(List::of)
          .forEach(eventDto -> notifyAboutTimelineEvent(state, roomId, eventDto));
    }

    final var leftRooms =
        maybeRooms
            .flatMap(syncRoomsDto -> Optional.ofNullable(syncRoomsDto.leave()))
            .orElseGet(HashMap::new);

    for (var entry : leftRooms.entrySet()) {

      final var roomId = MatrixRoomId.from(entry.getKey()).orElseThrow(IllegalStateException::new);
      final var domainEvent =
          MatrixSelfLeaveRoomEvent.create(roomId).orElseThrow(IllegalStateException::new);

      try {
        consumer.onSelfLeaveRoom(state, domainEvent);
      } catch (Exception e) {
        LOG.error("Uncaught exception when consuming room leave", e);
      }
    }
  }

  private void notifyAboutTimelineEvent(
      InternalMatrixState state, MatrixRoomId roomId, ClientEventDto event) {

    switch (event.type()) {
      case MessageEventContentDto.TYPE:
        notifyAboutMessageEvent(state, roomId, event);
        break;
      case MemberEventContentDto.TYPE:
        notifyAboutMemberEvent(state, roomId, event);
        break;
    }
  }

  private void notifyAboutMessageEvent(
      InternalMatrixState state, MatrixRoomId roomId, ClientEventDto event) {

    MessageEventContentDto content;
    try {
      content = objectMapper.treeToValue(event.content(), MessageEventContentDto.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    if (content.messageType() == null || content.body() == null) {
      LOG.error("Could not notify about invalid message: {}", event);
      return;
    }

    Optional<MatrixMessage> maybeMessage =
        switch (content.messageType()) {
          case TEXT -> MatrixTextMessage.create(content.body()).map(MatrixMessage.class::cast);
          case EMOTE -> MatrixEmoteMessage.create(content.body()).map(MatrixMessage.class::cast);
          case NOTICE -> MatrixNoticeMessage.create(content.body()).map(MatrixMessage.class::cast);
          default -> Optional.empty();
        };

    final var eventId = MatrixEventId.from(event.eventId()).orElseThrow(IllegalStateException::new);
    final var senderId = MatrixUserId.from(event.sender()).orElseThrow(IllegalStateException::new);

    // We should not handle notice messages as they should not be handled automatically
    if (maybeMessage.isPresent() && maybeMessage.get().getType() != MatrixMessageType.NOTICE) {
      final var domainEvent =
          MatrixMessageEvent.create(eventId, roomId, senderId, maybeMessage.get())
              .orElseThrow(IllegalStateException::new);

      try {
        consumer.onMessage(state, domainEvent);
      } catch (Exception e) {
        LOG.error("Uncaught exception when consuming message", e);
      }
    }
  }

  private void notifyAboutMemberEvent(
      InternalMatrixState state, MatrixRoomId roomId, ClientEventDto event) {

    MemberEventContentDto content;
    try {
      content = objectMapper.treeToValue(event.content(), MemberEventContentDto.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    final var maybePreviousContent = getPreviousContent(event, MemberEventContentDto.class);

    // > If not present, the user's previous membership must be assumed as leave.
    final var previousMembership =
        maybePreviousContent
            .flatMap(
                memberEventContentDto -> Optional.ofNullable(memberEventContentDto.membership()))
            .filter(membershipStateDto -> membershipStateDto != MembershipStateDto.UNKNOWN)
            .orElse(MembershipStateDto.LEAVE);

    final var sender = MatrixUserId.from(event.sender()).orElseThrow(IllegalStateException::new);

    try {
      if (content.membership() == MembershipStateDto.LEAVE
          || content.membership() == MembershipStateDto.BAN) {

        if (previousMembership == MembershipStateDto.JOIN) {

          final var domainEvent =
              MatrixUserLeaveRoomEvent.create(roomId, sender)
                  .orElseThrow(IllegalStateException::new);
          consumer.onUserLeaveRoom(state, domainEvent);
        }
      } else if (content.membership() == MembershipStateDto.JOIN
          && !sender.equals(state.getOwnUserId())) {

        if (previousMembership == MembershipStateDto.LEAVE) {

          final var domainEvent =
              MatrixUserJoinRoomEvent.create(roomId, sender)
                  .orElseThrow(IllegalStateException::new);
          consumer.onUserJoinRoom(state, domainEvent);
        }
      }
    } catch (Exception e) {
      LOG.error("Uncaught exception when consuming member event", e);
    }
  }

  private void notifyAboutInviteEvent(
      InternalMatrixState state, MatrixRoomId roomId, StrippedStateEventDto event) {

    if (!MemberEventContentDto.TYPE.equals(event.type())) {
      return;
    }

    MemberEventContentDto messageEventContent;
    try {
      messageEventContent = objectMapper.treeToValue(event.content(), MemberEventContentDto.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    if (messageEventContent.membership() != MembershipStateDto.INVITE) {
      return;
    }

    final var senderId = MatrixUserId.from(event.sender()).orElseThrow(IllegalStateException::new);

    final var domainEvent =
        MatrixRoomInviteEvent.create(roomId, senderId).orElseThrow(IllegalStateException::new);

    try {
      consumer.onInviteToRoom(state, domainEvent);
    } catch (Exception e) {
      LOG.error("Uncaught exception when consuming room invite", e);
    }
  }

  private <T> Optional<T> getPreviousContent(ClientEventDto event, Class<T> clazz) {

    final var maybeJson =
        Optional.ofNullable(event)
            .flatMap(clientEventDto -> Optional.ofNullable(clientEventDto.unsigned()))
            .flatMap(unsignedDataDto -> Optional.ofNullable(unsignedDataDto.prevContent()));

    if (maybeJson.isEmpty()) {
      return Optional.empty();
    }

    try {
      return Optional.ofNullable(objectMapper.treeToValue(maybeJson.get(), clazz));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
