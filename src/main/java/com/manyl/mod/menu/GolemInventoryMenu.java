package com.manyl.mod.menu;

import com.manyl.mod.entity.BaseGolemEntity;
import com.manyl.mod.registry.ModMenus;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.Equippable;

/**
 * Контейнер встроенного инвентаря прислужника (12 слотов):
 * - Слот 0 (x=64, y=39): еда для автоподдержания сытости;
 * - Слоты 1..6 (3x2 с x=92, y=39): переносимый груз и оружие;
 * - Слоты 7..10 (x=64..118, y=18): броня (шлем, нагрудник, поножи, ботинки);
 * - Слот 11 (x=148, y=18): щит;
 * - Слоты 12..47: стандартный инвентарь игрока (3x9 + хотбар).
 */
public class GolemInventoryMenu extends AbstractContainerMenu {
    private final BaseGolemEntity golem;
    private final Container golemInv;

    public GolemInventoryMenu(int syncId, Inventory playerInv, int entityId) {
        this(syncId, playerInv, resolveGolem(playerInv, entityId));
    }

    private static BaseGolemEntity resolveGolem(Inventory playerInv, int entityId) {
        Entity e = playerInv.player.level().getEntity(entityId);
        return e instanceof BaseGolemEntity g ? g : null;
    }

    public GolemInventoryMenu(int syncId, Inventory playerInv, BaseGolemEntity golem) {
        super(ModMenus.GOLEM_INVENTORY, syncId);
        this.golem = golem;
        this.golemInv = golem != null ? golem.getGolemInventory() : new net.minecraft.world.SimpleContainer(BaseGolemEntity.INVENTORY_SIZE);

        // Слот 0: еда
        this.addSlot(new Slot(golemInv, BaseGolemEntity.FOOD_SLOT, 64, 39) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.has(DataComponents.FOOD);
            }
        });

        // Слоты 1..6: груз и оружие (3x2)
        int index = 1;
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 3; col++) {
                this.addSlot(new Slot(golemInv, index++, 92 + col * 18, 39 + row * 18));
            }
        }

        // Слоты 7..10: броня (верхний ряд y=18)
        this.addSlot(armorSlot(BaseGolemEntity.ARMOR_HEAD_SLOT, 64, 18, EquipmentSlot.HEAD, ItemTags.HEAD_ARMOR));
        this.addSlot(armorSlot(BaseGolemEntity.ARMOR_CHEST_SLOT, 82, 18, EquipmentSlot.CHEST, ItemTags.CHEST_ARMOR));
        this.addSlot(armorSlot(BaseGolemEntity.ARMOR_LEGS_SLOT, 100, 18, EquipmentSlot.LEGS, ItemTags.LEG_ARMOR));
        this.addSlot(armorSlot(BaseGolemEntity.ARMOR_FEET_SLOT, 118, 18, EquipmentSlot.FEET, ItemTags.FOOT_ARMOR));

        // Слот 11: щит (x=148, y=18)
        this.addSlot(new Slot(golemInv, BaseGolemEntity.SHIELD_SLOT, 148, 18) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return isShieldItem(stack);
            }
        });

        // Инвентарь игрока (3x9)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Хотбар игрока (1x9)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    private Slot armorSlot(int slotIndex, int x, int y, EquipmentSlot equipSlot, TagKey<Item> tag) {
        return new Slot(golemInv, slotIndex, x, y) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return isArmorForSlot(stack, equipSlot, tag);
            }
        };
    }

    public static boolean isArmorForSlot(ItemStack stack, EquipmentSlot slot, TagKey<Item> tag) {
        if (stack.is(tag)) return true;
        Equippable eq = stack.get(DataComponents.EQUIPPABLE);
        return eq != null && eq.slot() == slot;
    }

    public static boolean isShieldItem(ItemStack stack) {
        return stack.is(Items.SHIELD) || stack.has(DataComponents.BLOCKS_ATTACKS);
    }

    public BaseGolemEntity getGolem() {
        return golem;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            copy = stack.copy();
            int golemSlots = BaseGolemEntity.INVENTORY_SIZE;
            if (index < golemSlots) {
                if (!this.moveItemStackTo(stack, golemSlots, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (isArmorForSlot(stack, EquipmentSlot.HEAD, ItemTags.HEAD_ARMOR)) {
                    if (!this.moveItemStackTo(stack, BaseGolemEntity.ARMOR_HEAD_SLOT, BaseGolemEntity.ARMOR_HEAD_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (isArmorForSlot(stack, EquipmentSlot.CHEST, ItemTags.CHEST_ARMOR)) {
                    if (!this.moveItemStackTo(stack, BaseGolemEntity.ARMOR_CHEST_SLOT, BaseGolemEntity.ARMOR_CHEST_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (isArmorForSlot(stack, EquipmentSlot.LEGS, ItemTags.LEG_ARMOR)) {
                    if (!this.moveItemStackTo(stack, BaseGolemEntity.ARMOR_LEGS_SLOT, BaseGolemEntity.ARMOR_LEGS_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (isArmorForSlot(stack, EquipmentSlot.FEET, ItemTags.FOOT_ARMOR)) {
                    if (!this.moveItemStackTo(stack, BaseGolemEntity.ARMOR_FEET_SLOT, BaseGolemEntity.ARMOR_FEET_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (isShieldItem(stack)) {
                    if (!this.moveItemStackTo(stack, BaseGolemEntity.SHIELD_SLOT, BaseGolemEntity.SHIELD_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (stack.has(DataComponents.FOOD)) {
                    if (!this.moveItemStackTo(stack, 0, 1, false)
                            && !this.moveItemStackTo(stack, 1, BaseGolemEntity.CARGO_END, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(stack, 1, BaseGolemEntity.CARGO_END, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return golem != null && golem.isAlive() && player.distanceToSqr(golem) < 64.0D;
    }
}
