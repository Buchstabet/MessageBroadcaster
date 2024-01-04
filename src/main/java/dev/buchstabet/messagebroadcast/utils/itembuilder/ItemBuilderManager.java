package dev.buchstabet.messagebroadcast.utils.itembuilder;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;

@Getter
public class ItemBuilderManager implements Listener
{

  private final List<ItemBuilder> itemBuilders = Lists.newArrayList();

  @EventHandler
  public void onClick(final InventoryClickEvent e)
  {
    if (!(e.getWhoClicked() instanceof Player)) {
      return;
    }
    if (e.getCurrentItem() == null) {
      return;
    }

    itemBuilders.stream().filter(itemBuilder -> e.getCurrentItem().equals(itemBuilder.build()))
        .map(ItemBuilder::getOnClick).filter(Objects::nonNull).forEach(consumer -> {
          consumer.accept(e);
          e.setCancelled(true);
        });

  }

  @EventHandler
  public void onInteract(final PlayerInteractEvent e)
  {

    if (e.getAction().equals(Action.LEFT_CLICK_AIR) || e.getAction()
        .equals(Action.LEFT_CLICK_BLOCK)) {
      return;
    }

    if (e.getItem() == null) {
      return;
    }

    itemBuilders.stream().filter(itemBuilder -> e.getItem().equals(itemBuilder.build()))
        .map(ItemBuilder::getOnInteract).filter(Objects::nonNull).forEach(consumer -> {
          consumer.accept(e);
          e.setCancelled(true);
        });
  }

}
