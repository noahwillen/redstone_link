package net.noah.redstonelink;

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

    public static void putData(String channelId, BlockPos blockPos, int signalStrength, ServerLevel serverLevel) {
        ChannelManager cm = serverLevel.getDataStorage().computeIfAbsent(ChannelManager::new, ChannelManager::new, RedstoneLink.MOD_ID);
        cm.channelMap.add(new Channel(channelId,blockPos, signalStrength));
        cm.setDirty();
    }

    public static List<Channel> getData(ServerLevel level) {
        return level.getDataStorage().get(ChannelManager::new, RedstoneLink.MOD_ID).channelMap;
    }

    public static void updateByBlockPos(String channelId, BlockPos blockPos, int signalStrength, ServerLevel serverLevel) {
        ChannelManager cm = serverLevel.getDataStorage().computeIfAbsent(ChannelManager::new, ChannelManager::new, RedstoneLink.MOD_ID);
        //update entry that matches blockPos, if not exists -> put new Channel
        AtomicBoolean found = new AtomicBoolean(false);
        cm.channelMap.forEach(channel -> {
            if (channel.getBlockPos().equals(blockPos)) {
                channel.setSignalStrength(signalStrength);
                channel.setChannelId(channelId);
                found.set(true);
            }
        });
        if (!found.get()) {
            putData(channelId, blockPos, signalStrength, serverLevel);
        }
        cm.setDirty();
    }

    public static void removeByBlockPos(BlockPos blockPos, ServerLevel serverLevel) {
        ChannelManager cm = serverLevel.getDataStorage().computeIfAbsent(ChannelManager::new, ChannelManager::new, RedstoneLink.MOD_ID);
        Iterator<Channel> iterator = cm.channelMap.iterator();
        while (iterator.hasNext()) {
            Channel c= iterator.next();
            if (c.getBlockPos().equals(blockPos)) {
                iterator.remove();
            }
        }
        cm.setDirty();
    }

    public static int getHighestSignalStrength(String channelId, ServerLevel serverLevel) {
        ChannelManager cm = serverLevel.getDataStorage().computeIfAbsent(ChannelManager::new, ChannelManager::new, RedstoneLink.MOD_ID);
        int signalStrength = 0;
        for (Channel c : cm.channelMap) {
            if (c.getChannelId().equals(channelId) && c.getSignalStrength()>signalStrength) {
                signalStrength=c.getSignalStrength();
            }
        }
        return signalStrength;
    }

    public static void clearChannels(ServerLevel serverLevel) {
        ChannelManager cm = serverLevel.getDataStorage().computeIfAbsent(ChannelManager::new, ChannelManager::new, RedstoneLink.MOD_ID);
        cm.channelMap.clear();
        cm.setDirty();
    }

    static class Channel {
        private String channelId;
        private BlockPos blockPos;
        private int signalStrength;

        public Channel(String channelId, BlockPos blockPos, int signalStrength){
            this.channelId=channelId;
            this.blockPos=blockPos;
            this.signalStrength=signalStrength;
        }

        public CompoundTag deserialize() {
            CompoundTag nbt = new CompoundTag();
            nbt.putString("channelId", channelId);
            nbt.putLong("blockPos", blockPos.asLong());
            nbt.putInt("signalStrength", signalStrength);
            return nbt;
        }
        public static Channel serialize(CompoundTag nbt) {
            return new Channel(nbt.getString("channelId"), BlockPos.of(nbt.getLong("blockPos")), nbt.getInt("signalStrength"));
        }

        public void setChannelId(String channelId) {
            this.channelId = channelId;
        }
        public void setSignalStrength(int signalStrength) {
            this.signalStrength = signalStrength;
        }

        public BlockPos getBlockPos() {
            return blockPos;
        }

        public int getSignalStrength() {
            return signalStrength;
        }

        public String getChannelId() {
            return channelId;
        }

        @Override
        public String toString() {
            return "channelId: "+channelId+" blockPos: "+blockPos.toString()+" signalStrength: "+signalStrength + "\n";
        }
    }
}
