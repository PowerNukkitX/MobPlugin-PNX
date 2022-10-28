package nukkitcoders.mobplugin.entities.monster.flying;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.event.entity.ProjectileLaunchEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Location;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.LevelEventPacket;
import nukkitcoders.mobplugin.entities.monster.FlyingMonster;
import nukkitcoders.mobplugin.entities.projectile.EntityGhastFireBall;
import nukkitcoders.mobplugin.utils.FastMathLite;
import nukkitcoders.mobplugin.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class Ghast extends FlyingMonster {

    public static final int NETWORK_ID = 41;

    public Ghast(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 4;
    }

    @Override
    public float getHeight() {
        return 4;
    }

    @Override
    public double getSpeed() {
        return 1.2;
    }

    @Override
    public void initEntity() {
        super.initEntity();

        this.fireProof = true;
        this.setMaxHealth(10);
        this.setDataFlag(DATA_FLAGS, DATA_FLAG_FIRE_IMMUNE, true);
    }

    @Override
    public boolean targetOption(EntityCreature creature, double distance) {
        if (creature instanceof Player) {
            Player player = (Player) creature;
            return !player.closed && player.spawned && player.isAlive() && (player.isSurvival() || player.isAdventure()) && distance <= 4096;
        }
        return false;
    }

    @Override
    public void attackEntity(Entity player) {
        if (this.distanceSquared(player) <= 4096) { // 64 blocks)
            if (this.attackDelay == 50) {
                this.level.addLevelEvent(this, LevelEventPacket.EVENT_SOUND_GHAST);
            }
            if (this.attackDelay > 60 && Utils.rand(1, 32) < 4) {
                this.attackDelay = 0;

                double f = 1;
                double yaw = this.yaw + Utils.rand(-4.0, 4.0);
                double pitch = this.pitch + Utils.rand(-4.0, 4.0);
                Location pos = new Location(this.x - Math.sin(FastMathLite.toRadians(yaw)) * Math.cos(FastMathLite.toRadians(pitch)) * 0.5, this.y + this.getEyeHeight() - 1, // below eyes
                        this.z + Math.cos(FastMathLite.toRadians(yaw)) * Math.cos(FastMathLite.toRadians(pitch)) * 0.5, yaw, pitch, this.level);

                if (this.getLevel().getBlockIdAt(pos.getFloorX(), pos.getFloorY(), pos.getFloorZ()) != Block.AIR) {
                    return;
                }

                EntityGhastFireBall fireball = (EntityGhastFireBall) Entity.createEntity("GhastFireBall", pos, this);
                fireball.setExplode(true);
                fireball.setMotion(new Vector3(-Math.sin(FastMathLite.toRadians(yaw)) * Math.cos(FastMathLite.toRadians(pitch)) * f * f, -Math.sin(FastMathLite.toRadians(pitch)) * f * f,
                        Math.cos(FastMathLite.toRadians(yaw)) * Math.cos(FastMathLite.toRadians(pitch)) * f * f));

                ProjectileLaunchEvent launch = new ProjectileLaunchEvent(fireball);
                this.server.getPluginManager().callEvent(launch);
                if (launch.isCancelled()) {
                    fireball.close();
                } else {
                    fireball.spawnToAll();
                    this.level.addLevelEvent(this, LevelEventPacket.EVENT_SOUND_GHAST_SHOOT);
                }
            }
        }
    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();

        for (int i = 0; i < Utils.rand(0, 2); i++) {
            drops.add(Item.get(Item.GUNPOWDER, 0, 1));
        }

        drops.add(Item.get(Item.GHAST_TEAR, 0, Utils.rand(0, 1)));

        return drops.toArray(new Item[0]);
    }

    @Override
    public int getKillExperience() {
        return 5;
    }

    @Override
    public int nearbyDistanceMultiplier() {
        return 30;
    }
}
