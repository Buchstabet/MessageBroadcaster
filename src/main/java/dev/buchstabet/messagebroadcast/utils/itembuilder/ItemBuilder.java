package dev.buchstabet.messagebroadcast.utils.itembuilder;

import dev.buchstabet.messagebroadcast.MessageBroadcast;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

@Getter
public class ItemBuilder
{

  private final ItemStack itemStack;
  private final ItemMeta itemMeta;

  private Consumer<InventoryClickEvent> onClick;
  private Consumer<PlayerInteractEvent> onInteract;

  private ItemBuilder(final ItemStack itemStack)
  {
    this.itemStack = itemStack;
    this.itemMeta = itemStack.getItemMeta();
  }

  public ItemBuilder(Color color)
  {
    itemStack = new ItemStack(Material.LEATHER_CHESTPLATE);
    itemMeta = itemStack.getItemMeta();
    ((LeatherArmorMeta) itemMeta).setColor(color);
  }

  public ItemBuilder(Material material)
  {
    this(new ItemStack(material));
  }

  public ItemBuilder(Material material, int subID)
  {
    this(new ItemStack(material, 1, (short) subID));
  }

  public ItemBuilder(final String skullOwner)
  {
    this.itemStack = new ItemStack(Material.PLAYER_HEAD);
    this.itemMeta = itemStack.getItemMeta();
    ((SkullMeta) itemMeta).setOwner(skullOwner);
  }

  public ItemBuilder registerItemBuilder()
  {
    MessageBroadcast.getInstance().get(ItemBuilderManager.class).getItemBuilders().add(this);
    return this;
  }

  public ItemBuilder setDisplayname(final String name)
  {
    this.itemMeta.setDisplayName(name);
    return this;
  }

  public ItemBuilder setAmount(final int amount)
  {
    this.itemStack.setAmount(amount);
    return this;
  }

  public ItemBuilder setDurability(final short durability)
  {
    this.itemStack.setDurability(durability);
    return this;
  }

  public ItemBuilder addEnchantments(final Enchantment... enchantments)
  {
    for (Enchantment e : enchantments) {
      itemMeta.addEnchant(e.enchantment, e.strength, true);
    }
    return this;
  }


  public ItemBuilder hideEnchants()
  {
    itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
    return this;
  }

  public ItemBuilder addItemFlag(final ItemFlag itemFlag)
  {
    itemMeta.addItemFlags(itemFlag);
    return this;
  }

  public ItemBuilder setLore(final String... strings)
  {
    itemMeta.setLore(Arrays.asList(strings));
    return this;
  }

  public ItemBuilder setLore(final List<String> strings)
  {
    itemMeta.setLore(strings);
    return this;
  }

  public ItemBuilder setOnClick(Consumer<InventoryClickEvent> onClick)
  {
    this.onClick = onClick;
    return this;
  }

  public ItemBuilder setOnInteract(Consumer<PlayerInteractEvent> onInteract)
  {
    this.onInteract = onInteract;
    return this;
  }

  public ItemStack build()
  {
    itemStack.setItemMeta(itemMeta);
    return itemStack;
  }

  @RequiredArgsConstructor
  public static class Enchantment
  {

    private final org.bukkit.enchantments.Enchantment enchantment;
    private final int strength;
  }
}
