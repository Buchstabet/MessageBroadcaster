package dev.buchstabet.messagebroadcast.messages;

import com.zaxxer.hikari.HikariDataSource;
import dev.buchstabet.messagebroadcast.MessageBroadcast;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessageHolder extends ArrayList<Message>
{

  private static final ExecutorService ex;

  static {
    ex = Executors.newSingleThreadExecutor();
  }

  public CompletableFuture<Void> update()
  {
    return CompletableFuture.allOf(stream().map(MessageHolder::update).toArray(CompletableFuture[]::new));
  }

  public static CompletableFuture<MessageHolder> load()
  {
    MessageHolder messages = new MessageHolder();
    return CompletableFuture.supplyAsync(() -> {
      HikariDataSource dataSource = MessageBroadcast.getInstance().get(HikariDataSource.class);
      try (Connection connection = dataSource.getConnection(); PreparedStatement stm = connection.prepareStatement(
          "SELECT * FROM message_broadcast ORDER BY sortId;")) {
        ResultSet resultSet = stm.executeQuery();
        while (resultSet.next()) {
          UUID uuid = UUID.fromString(resultSet.getString("uuid"));
          int sortId = resultSet.getInt("sortId");
          String content = resultSet.getString("content");
          String author = resultSet.getString("author");
          long createdAt = (long) resultSet.getFloat("createdAt");
          MessageType messageType = MessageType.valueOf(resultSet.getString("type"));
          String permission = resultSet.getString("permission");

          Message message = new Message(uuid, sortId, content, author, permission, createdAt,
              messageType);
          messages.add(message);
        }
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }

      return messages;
    }, ex);
  }

  public CompletableFuture<Void> addNewMessage(Message message)
  {
    return CompletableFuture.runAsync(() -> {
      HikariDataSource dataSource = MessageBroadcast.getInstance().get(HikariDataSource.class);
      try (Connection connection = dataSource.getConnection(); PreparedStatement stm = connection.prepareStatement(
          "INSERT INTO message_broadcast (uuid, sortId, content, author, createdAt, type, permission) VALUES (?,?,?,?,?,?,?);")) {
        stm.setString(1, message.getUuid().toString());
        stm.setInt(2, message.getSortId());
        stm.setString(3, message.getContent());
        stm.setString(4, message.getAuthor());
        stm.setFloat(5, message.getCreatedAt());
        stm.setString(6, message.getType().name());
        stm.setString(7, message.getPermission());
        stm.execute();

        add(message);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }, ex);
  }

  public static CompletableFuture<Void> update(Message message)
  {
    return CompletableFuture.runAsync(() -> {
      HikariDataSource dataSource = MessageBroadcast.getInstance().get(HikariDataSource.class);
      try (Connection connection = dataSource.getConnection(); PreparedStatement stm = connection.prepareStatement(
          "UPDATE message_broadcast SET sortId=?,content=? WHERE uuid=?;")) {
        stm.setInt(1, message.getSortId());
        stm.setString(2, message.getContent());
        stm.setString(3, message.getUuid().toString());
        stm.execute();
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }, ex);
  }

  public Optional<Message> find(String uuid)
  {
    return stream().filter(message -> message.getUuid().toString().equals(uuid)).findAny();
  }

  public CompletableFuture<Void> delete(Message message)
  {
    remove(message);
    return CompletableFuture.runAsync(() -> {
      HikariDataSource dataSource = MessageBroadcast.getInstance().get(HikariDataSource.class);
      try (Connection connection = dataSource.getConnection(); PreparedStatement stm = connection.prepareStatement(
          "DELETE FROM message_broadcast WHERE uuid=?;")) {
        stm.setString(1, message.getUuid().toString());
        stm.execute();
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }, ex);
  }

  public Optional<Message> findById(int id)
  {
    return stream().filter(message -> message.getSortId() == id).findAny();
  }
}
