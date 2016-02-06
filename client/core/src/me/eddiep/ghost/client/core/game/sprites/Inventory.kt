package me.eddiep.ghost.client.core.game.sprites

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import me.eddiep.ghost.client.core.game.Entity
import me.eddiep.ghost.client.core.game.Item
import me.eddiep.ghost.client.core.render.Text

class Inventory : Entity("sprites/inv.png", 0) {
    val inventory: Array<ItemHolder?> = arrayOfNulls(2);


    public fun setSlot1(id: Short) {
        val item = Item.getItem(id);
        val entity = item.createEntity(-5);
        val text = Text(16, Color.WHITE, Gdx.files.internal("fonts/INFO56_0.ttf"))
        text.x = 854f
        text.y = 100f - (entity.height * 1.3f)
        text.text = item.name()

        entity.setScale(2f)
        entity.setCenter(852f, 100f)

        parentScene.addEntity(entity)
        parentScene.addEntity(text)

        inventory[0] = ItemHolder(entity, text, item);
    }

    public fun clearSlot1() {
        clearSlot(0)
    }

    public fun setSlot2(id: Short) {
        val item = Item.getItem(id);
        val entity = item.createEntity(-5);
        val text = Text(16, Color.WHITE, Gdx.files.internal("fonts/INFO56_0.ttf"))
        text.x = 947f
        text.y = 100f - (entity.height * 1.3f)
        text.text = item.name()

        entity.setScale(2f)
        entity.setCenter(950f, 100f)

        parentScene.addEntity(entity)
        parentScene.addEntity(text)

        inventory[1] = ItemHolder(entity, text, item);
    }

    public fun clearSlot2() {
        clearSlot(1)
    }

    public fun clearSlot(slot: Int) {
        if (inventory[slot] != null) {
            val entity = inventory[slot]?.entity;
            if (entity != null)
                parentScene.removeEntity(entity)

            val text = inventory[slot]?.text
            if (text != null)
                parentScene.removeEntity(text)

            inventory[slot] = null
        }
    }

    public fun hasItem(slot: Int): Boolean {
        return inventory[slot] != null
    }

    public fun getItem(slot: Int): Short {
        if (!hasItem(slot))
            return -1

        return inventory[slot]?.item?.id as Short
    }

    class ItemHolder(val entity: Entity, val text: Text, val item: Item) { }
}
