package org.synyx.matrix.bot.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.synyx.matrix.bot.MatrixState;
import org.synyx.matrix.bot.domain.MatrixRoom;
import org.synyx.matrix.bot.domain.MatrixRoomAlias;
import org.synyx.matrix.bot.domain.MatrixRoomId;
import org.synyx.matrix.bot.domain.MatrixUser;
import org.synyx.matrix.bot.domain.MatrixUserId;
import org.synyx.matrix.bot.internal.api.dto.ClientEventDto;
import org.synyx.matrix.bot.internal.api.dto.StrippedStateEventDto;
import org.synyx.matrix.bot.internal.api.dto.SyncResponseDto;
import org.synyx.matrix.bot.internal.api.dto.event.CanonicalAliasEventContentDto;
import org.synyx.matrix.bot.internal.api.dto.event.MemberEventContentDto;
import org.synyx.matrix.bot.internal.api.dto.event.RoomNameEventContentDto;

public class MatrixStateSynchronizer {

  private final MatrixState state;
  private final ObjectMapper objectMapper;

  public MatrixStateSynchronizer(MatrixState state, ObjectMapper objectMapper) {

    this.state = state;
    this.objectMapper = objectMapper;
  }

  public void synchronizeState(SyncResponseDto syncResponse) {

    final var maybeRooms = Optional.ofNullable(syncResponse.rooms());
    final var invitedRooms =
        maybeRooms
            .flatMap(syncRoomsDto -> Optional.ofNullable(syncRoomsDto.invite()))
            .orElseGet(HashMap::new);

    for (var entry : invitedRooms.entrySet()) {

      final var roomId = MatrixRoomId.from(entry.getKey()).orElseThrow(IllegalStateException::new);
      final var room = getOrCreateRoom(state.getInvitedRooms(), roomId);

      Optional.ofNullable(entry.getValue())
          .flatMap(roomDto -> Optional.ofNullable(roomDto.inviteState()))
          .flatMap(inviteStateDto -> Optional.ofNullable(inviteStateDto.events()))
          .orElseGet(List::of)
          .forEach(eventDto -> synchronizeStrippedEvent(room, eventDto));
    }

    final var joinedRooms =
        maybeRooms
            .flatMap(syncRoomsDto -> Optional.ofNullable(syncRoomsDto.join()))
            .orElseGet(HashMap::new);

    for (var entry : joinedRooms.entrySet()) {

      final var roomId = MatrixRoomId.from(entry.getKey()).orElseThrow(IllegalStateException::new);
      removeFromInvitedRoomsIfExisting(roomId);
      final var room = getOrCreateRoom(state.getJoinedRooms(), roomId);

      Optional.ofNullable(entry.getValue())
          .flatMap(roomDto -> Optional.ofNullable(roomDto.state()))
          .flatMap(roomStateDto -> Optional.ofNullable(roomStateDto.events()))
          .orElseGet(List::of)
          .forEach(eventDto -> synchronizeClientEvent(room, eventDto));

      Optional.ofNullable(entry.getValue())
          .flatMap(roomDto -> Optional.ofNullable(roomDto.timeline()))
          .flatMap(timelineDto -> Optional.ofNullable(timelineDto.events()))
          .orElseGet(List::of)
          .forEach(eventDto -> synchronizeClientEvent(room, eventDto));
    }

    final var leftRooms =
        maybeRooms
            .flatMap(syncRoomsDto -> Optional.ofNullable(syncRoomsDto.leave()))
            .orElseGet(HashMap::new);

    for (var entry : leftRooms.entrySet()) {

      final var roomId = MatrixRoomId.from(entry.getKey()).orElseThrow(IllegalStateException::new);
      removeFromJoinedRoomsIfExisting(roomId);
    }
  }

  private void synchronizeClientEvent(MatrixRoom room, ClientEventDto event) {

    final var sender = MatrixUserId.from(event.sender()).orElseThrow(IllegalStateException::new);

    try {
      switch (event.type()) {
        case RoomNameEventContentDto.TYPE ->
            handleRoomNameEvent(
                room, objectMapper.treeToValue(event.content(), RoomNameEventContentDto.class));
        case CanonicalAliasEventContentDto.TYPE ->
            handleCanonicalAliasEvent(
                room,
                objectMapper.treeToValue(event.content(), CanonicalAliasEventContentDto.class));
        case MemberEventContentDto.TYPE ->
            handleMemberEvent(
                room,
                sender,
                objectMapper.treeToValue(event.content(), MemberEventContentDto.class));
        default -> {
          // Ignore other events
        }
      }
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  private void synchronizeStrippedEvent(MatrixRoom room, StrippedStateEventDto event) {

    final var sender = MatrixUserId.from(event.sender()).orElseThrow(IllegalStateException::new);

    try {
      switch (event.type()) {
        case RoomNameEventContentDto.TYPE ->
            handleRoomNameEvent(
                room, objectMapper.treeToValue(event.content(), RoomNameEventContentDto.class));
        case CanonicalAliasEventContentDto.TYPE ->
            handleCanonicalAliasEvent(
                room,
                objectMapper.treeToValue(event.content(), CanonicalAliasEventContentDto.class));
        case MemberEventContentDto.TYPE ->
            handleMemberEvent(
                room,
                sender,
                objectMapper.treeToValue(event.content(), MemberEventContentDto.class));
        default -> {
          // Ignore other events
        }
      }
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  private void handleRoomNameEvent(MatrixRoom room, RoomNameEventContentDto content) {

    room.setName(content.name());
  }

  private void handleCanonicalAliasEvent(MatrixRoom room, CanonicalAliasEventContentDto content) {

    if (content.alias() != null) {
      final var newCanonicalAlias =
          MatrixRoomAlias.from(content.alias()).orElseThrow(IllegalStateException::new);
      room.setCanonicalAlias(newCanonicalAlias);
    }
  }

  private void handleMemberEvent(
      MatrixRoom room, MatrixUserId sender, MemberEventContentDto content) {

    if (content.membership() == MemberEventContentDto.MembershipState.JOIN) {
      final var user = getOrCreateUserInRoom(room, sender);
      if (content.displayName() != null) {
        user.setDisplayName(content.displayName());
      }
    } else if (content.membership() == MemberEventContentDto.MembershipState.LEAVE
        || content.membership() == MemberEventContentDto.MembershipState.BAN) {
      room.getRoomUsers().removeIf(user -> user.getId().equals(sender));
    }
  }

  private static MatrixRoom getOrCreateRoom(List<MatrixRoom> rooms, MatrixRoomId roomId) {

    final var maybeExistingRoom =
        rooms.stream().filter(room -> room.getId().equals(roomId)).findAny();

    if (maybeExistingRoom.isPresent()) {
      return maybeExistingRoom.get();
    }

    final var newRoom = MatrixRoom.from(roomId).orElseThrow(IllegalStateException::new);
    rooms.add(newRoom);

    return newRoom;
  }

  private static MatrixUser getOrCreateUserInRoom(MatrixRoom room, MatrixUserId userId) {

    final var maybeExistingUser =
        room.getRoomUsers().stream().filter(user -> user.getId().equals(userId)).findAny();

    if (maybeExistingUser.isPresent()) {
      return maybeExistingUser.get();
    }

    final var newUser = MatrixUser.from(userId).orElseThrow(IllegalStateException::new);
    room.getRoomUsers().add(newUser);

    return newUser;
  }

  private void removeFromInvitedRoomsIfExisting(MatrixRoomId roomId) {

    state.getInvitedRooms().removeIf(room -> room.getId().equals(roomId));
  }

  private void removeFromJoinedRoomsIfExisting(MatrixRoomId roomId) {

    state.getJoinedRooms().removeIf(room -> room.getId().equals(roomId));
  }
}
