package com.manyl.mod.menu;

import com.manyl.mod.entity.BaseGolemEntity;
import com.manyl.mod.registry.ModMenus;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Контейнер встроенного инвентаря прислужника:
 * - Слот 0 (x=64, y=39): еда для автоподдержания сытости;
 * - Слоты 1..6 (3x2 с x=92, y=39): переносимый груз;
 * - Слоты 7..42: стандартный инвентарь игрока (3x9 + хотбар).
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

        // Слот 0: только еда (соответствует координатам golem_inventory.png)
        this.addSlot(new Slot(golemInv, BaseGolemEntity.FOOD_SLOT, 64, 39) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.has(DataComponents.FOOD);
            }
        });

        // Слоты 1..6: груз (3 столбца x 2 ряда)
        int index = 1;
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 3; col++) {
                this.addSlot(new Slot(golemInv, index++, 92 + col * 18, 39 + row * 18));
            }
        }

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
                if (stack.has(DataComponents.FOOD)) {
                    if (!this.moveItemStackTo(stack, 0, 1, false)
                            && !this.moveItemStackTo(stack, 1, golemSlots, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(stack, 1, golemSlots, false)) {
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
