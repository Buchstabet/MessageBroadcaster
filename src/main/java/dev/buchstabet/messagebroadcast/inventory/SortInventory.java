package dev.buchstabet.messagebroadcast.inventory;

import dev.buchstabet.messagebroadcast.MessageBroadcast;
import dev.buchstabet.messagebroadcast.messages.Message;
import dev.buchstabet.messagebroadcast.messages.MessageHolder;
import dev.buchstabet.messagebroadcast.utils.itembuilder.ItemBuilder;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SortInventory implements Listener
{

  public static void open(Player player, MessageHolder holder)
  {
    Inventory inventory = Bukkit.createInventory(null, 9 * 6, "§5§lSortierung der Messages");
    for (Message message : holder) {
      ItemStack itemStack = new ItemBuilder(Material.PAPER).setDisplayname(
          message.getUuid().toString()).setLore("Autor: " + message.getAuthor(),
          "Timestamp: " + MessageBroadcast.SIMPLE_DATE_FORMAT.format(message.getCreatedAt()), " ",
          message.getStyledContent()).build();
      inventory.setItem(message.getSortId(), itemStack);
    }

    player.openInventory(inventory);
  }

  @EventHandler(ignoreCancelled = true)
  public void onInventoryClose(InventoryCloseEvent event)
  {
    if (!event.getView().getTitle().equals("§5§lSortierung der Messages")) {
      return;
    }

    MessageHolder messages = MessageBroadcast.getInstance().get(MessageHolder.class);
    Arrays.stream(event.getInventory().getContents()).filter(Objects::nonNull)
        .map(ItemStack::getItemMeta).filter(Objects::nonNull).map(ItemMeta::getDisplayName)
        .map(messages::find).filter(Optional::isPresent).map(Optional::get);

    AtomicInteger sortId = new AtomicInteger();
    AtomicBoolean anyChanges = new AtomicBoolean();
    for (int i = 0; i < event.getInventory().getContents().length; i++) {
      ItemStack itemStack = event.getInventory().getContents()[i];
      if (itemStack == null || itemStack.getItemMeta() == null || itemStack.getItemMeta()
          .getDisplayName().isBlank())
        continue;
      messages.find(itemStack.getItemMeta().getDisplayName())
          .ifPresent(message -> {
            int increment = sortId.getAndIncrement();
            if (message.getSortId() == increment) return;
            anyChanges.set(true);
            message.setSortId(increment);
          });
    }

    if (!anyChanges.get()) {
      event.getPlayer().sendMessage("§eEs wurde keine Änderung festgestellt.");
      return;
    }

    if (messages.size() != sortId.get()) {
      event.getPlayer().sendMessage(
          "§4Ein Fehler ist aufgetreten. Es wurden nicht alle Messages im Inventar gefunden.");
      return;
    }

    messages.update().whenComplete((unused, throwable) -> {
      if (throwable != null) {
        throwable.printStackTrace();
        event.getPlayer().sendMessage("§4Error: " + throwable.getMessage());
        return;
      }

      event.getPlayer().sendMessage("§2Sortierung gespeichert.");
    });
  }
}
