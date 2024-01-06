package dev.buchstabet.messagebroadcast;

import dev.buchstabet.messagebroadcast.inventory.SortInventory;
import dev.buchstabet.messagebroadcast.messages.Message;
import dev.buchstabet.messagebroadcast.messages.MessageHolder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MessageBroadcastCommand implements CommandExecutor, TabExecutor
{

  @Nullable
  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
      @NotNull String alias, @NotNull String[] args)
  {

    List<String> strings = new ArrayList<>();
    if (args.length == 1) {
      strings.add("list");
      strings.add("edit");
      strings.add("add");
      strings.add("delete");
      strings.add("sort");
    }

    if (args.length == 2 && (args[0].equals("delete") || args[0].equals("edit")))
      MessageBroadcast.getInstance().get(MessageHolder.class).stream().map(Message::getUuid)
          .map(UUID::toString).forEach(strings::add);
    return strings;
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
      @NotNull String label, @NotNull String[] args)
  {
    if (args.length > 0) {
      MessageHolder messages = MessageBroadcast.getInstance().get(MessageHolder.class);
      if (args[0].equals("list")) {
        if (messages.isEmpty()) {
          sender.sendMessage("§cEs existiert keine Broadcast-Nachricht.");
        } else {
          sender.sendMessage(
              "§eHier eine Liste der geladenen Nachrichten (Anzahl: " + messages.size() + "):");
          messages.forEach(message -> {
            sender.sendMessage("§8§m-------------------------------------------");
            sender.sendMessage("§UUID: " + message.getUuid());
            sender.sendMessage("Author: " + message.getAuthor());
            sender.sendMessage("Permission: " + message.getPermission());
            sender.sendMessage("SortId: " + message.getSortId());
            sender.sendMessage(
                "Timestamp: " + MessageBroadcast.SIMPLE_DATE_FORMAT.format(message.getCreatedAt()));
            sender.sendMessage("\n" + message.getStyledContent() + "\n");
          });
        }
      } else if (args[0].equals("sort") && sender instanceof Player player) {
        SortInventory.open(player, messages);
      } else if (args[0].equals("delete") && args.length == 2) {
        messages.find(args[1]).map(messages::delete).ifPresent(
            voidCompletableFuture -> voidCompletableFuture.whenComplete((unused, throwable) -> {
              if (throwable != null) {
                throwable.printStackTrace();
                sender.sendMessage("Error: " + throwable.getMessage());
                return;
              }

              sender.sendMessage("§aDie Message wurde gelöscht.");
            }));
      } else if (args[0].equals("add") && args.length >= 3) {
        sendUseConsole(sender);

        String permission = args[1];
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
          stringBuilder.append(args[i]).append(" ");
        }

        Message message = new Message(UUID.randomUUID(), messages.size(), stringBuilder.toString(),
            sender.getName(), permission, System.currentTimeMillis());
        messages.addNewMessage(message).whenComplete((unused, throwable) -> {
          if (throwable != null) {
            throwable.printStackTrace();
            sender.sendMessage("§4Error: " + throwable.getMessage());
            return;
          }

          sender.sendMessage("§aDie Nachricht wurde erstellt.");
        });
      } else if (args[0].equals("edit") && args.length >= 3) {
        sendUseConsole(sender);

        String uuid = args[1];
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
          stringBuilder.append(args[i]).append(" ");
        }

        messages.find(uuid).ifPresent(message -> {
          String string = stringBuilder.toString();
          if (message.getContent().equals(string)) {
            sender.sendMessage("§eEs wurde keine Änderung festgestellt.");
            return;
          }

          message.setContent(string);
          MessageHolder.update(message).whenComplete((unused, throwable) -> {
            if (throwable != null) {
              throwable.printStackTrace();
              sender.sendMessage("§4Error: " + throwable.getMessage());
              return;
            }

            sender.sendMessage("§aDie Nachricht wurde verändert.");
          });
        });
      }

    } else {
      sender.sendMessage("§e/" + label + " add <permission> <content>");
      sender.sendMessage("§e/" + label + " edit <uuid> <content>");
      sender.sendMessage("§e/" + label + " delete <uuid>");
      sender.sendMessage("§e/" + label + " list");
      if (sender instanceof Player)
        sender.sendMessage("§e/" + label + " sort");
      sender.sendMessage(
          "§bFarbcodes können, mit '&' eingeleitet, verwendet werden. Die neuen Hex Codes werden supportet z.B. '&#FF99CF'");
    }

    return false;
  }

  private static void sendUseConsole(@NotNull CommandSender sender)
  {
    if (sender instanceof Player) {
      sender.sendMessage(
          "§4§oEs wird empfohlen diesen Befehl in der Konsole zu verwenden. In der Konsole können länger Nachrichten eingegeben werden.");
    }
  }

}
