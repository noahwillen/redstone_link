package net.noah.redstoneplus;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChannelManager extends SavedData {

    private final List<Channel> channelMap = new ArrayList<>();

    //create
    public ChannelManager() {
    }

    //load
    public ChannelManager(CompoundTag pCompoundTag) {
        CompoundTag saveData = pCompoundTag.getCompound("saveData");
        for (int i =0;saveData.contains("data"+i);i++) {
            channelMap.add(Channel.serialize(saveData.getCompound("data"+i)));
        }
    }

    //save
    @Override
    public CompoundTag save(CompoundTag pCompoundTag) {
        CompoundTag saveData = new CompoundTag();
        for (ListIterator<Channel> iterator = channelMap.listIterator(); iterator.hasNext();) {
            saveData.put("data"+iterator.nextIndex(), iterator.next().deserialize());
        }
        pCompoundTag.put("saveData", saveData);
        return pCompoundTag;
    }

    public static void putData(String channelId, BlockPos blockPos, boolean powered, ServerLevel serverLevel) {
        ChannelManager cm = serverLevel.getServer().overworld().getDataStorage().computeIfAbsent(ChannelManager::new, ChannelManager::new, RedstonePlus.MOD_ID);
        cm.channelMap.add(new Channel(channelId,blockPos, powered));
        cm.setDirty();
    }

    public static void updateByBlockPos(String channelId, BlockPos blockPos, boolean powered, ServerLevel serverLevel) {
        ChannelManager cm = serverLevel.getServer().overworld().getDataStorage().computeIfAbsent(ChannelManager::new, ChannelManager::new, RedstonePlus.MOD_ID);
        //update entry that matches blockPos, if not exists -> put new Channel
        AtomicBoolean found = new AtomicBoolean(false);
        cm.channelMap.forEach(channel -> {
            if (channel.getBlockPos().equals(blockPos)) {
                channel.setPowered(powered);
                channel.setChannelId(channelId);
                found.set(true);
            }
        });
        if (!found.get()) {
            putData(channelId, blockPos, powered, serverLevel);
        }
        cm.setDirty();
    }

    public static void removeByBlockPos(BlockPos blockPos, ServerLevel serverLevel) {
        ChannelManager cm = serverLevel.getServer().overworld().getDataStorage().computeIfAbsent(ChannelManager::new, ChannelManager::new, RedstonePlus.MOD_ID);
        Iterator<Channel> iterator = cm.channelMap.iterator();
        while (iterator.hasNext()) {
            Channel c= iterator.next();
            if (c.getBlockPos().equals(blockPos)) {
                iterator.remove();
            }
        }
        cm.setDirty();
    }

    public static boolean getPower(String channelId, ServerLevel serverLevel) {
        ChannelManager cm = serverLevel.getServer().overworld().getDataStorage().computeIfAbsent(ChannelManager::new, ChannelManager::new, RedstonePlus.MOD_ID);
        for (Channel c : cm.channelMap) {
            if (c.getChannelId().equals(channelId) && c.getPowered()) {
                return true;
            }
        }
        return false;
    }

    static class Channel {
        private String channelId;
        private BlockPos blockPos;
        private boolean powered;

        public Channel(String channelId, BlockPos blockPos, boolean powered){
            this.channelId=channelId;
            this.blockPos=blockPos;
            this.powered=powered;
        }

        public CompoundTag deserialize() {
            CompoundTag nbt = new CompoundTag();
            nbt.putString("channelId", channelId);
            nbt.putLong("blockPos", blockPos.asLong());
            nbt.putBoolean("powered", powered);
            return nbt;
        }
        public static Channel serialize(CompoundTag nbt) {
            return new Channel(nbt.getString("channelId"), BlockPos.of(nbt.getLong("blockPos")), nbt.getBoolean("powered"));
        }

        public void setChannelId(String channelId) {
            this.channelId = channelId;
        }
        public void setPowered(boolean powered) {
            this.powered = powered;
        }

        public BlockPos getBlockPos() {
            return blockPos;
        }

        public boolean getPowered() {
            return powered;
        }

        public String getChannelId() {
            return channelId;
        }

        @Override
        public String toString() {
            return "channelId: "+channelId+" blockPos: "+blockPos.toString()+" powered: "+powered + "\n";
        }
    }
}
