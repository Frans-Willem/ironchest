package cpw.mods.ironchest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.src.NetworkManager;
import net.minecraft.src.Packet;
import net.minecraft.src.Packet1Login;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.mod_IronChest;
import net.minecraft.src.forge.IConnectionHandler;
import net.minecraft.src.forge.IPacketHandler;
import net.minecraft.src.forge.MessageManager;

public class PacketHandler implements IPacketHandler, IConnectionHandler {
	@Override
	public void onConnect(NetworkManager network) {
    MessageManager.getInstance().registerChannel(network, this, "IronChest");
	}

	@Override
	public void onLogin(NetworkManager network, Packet1Login login) {
	}

	@Override
	public void onDisconnect(NetworkManager network, String message, Object[] args) {
	  MessageManager.getInstance().removeConnection(network);
	}

	@Override
	public void onPacketData(NetworkManager network, String channel, byte[] data) {
		DataInputStream dis=new DataInputStream(new ByteArrayInputStream(data));
		int x;
		int y;
		int z;
		int typ;
		boolean hasStacks;
		int[] items=null;
		try {
			x = dis.readInt();
			y = dis.readInt();
			z = dis.readInt();
			typ=dis.readByte();
			hasStacks=dis.readByte()!=0;
			if (hasStacks) {
				items = new int[24];
				for (int i=0; i<items.length; i++) {
					items[i]=dis.readInt();
				}
			}
		} catch (IOException e) {
			return;
		}
		World world=mod_IronChest.proxy.getCurrentWorld();
		TileEntity te=world.getBlockTileEntity(x, y, z);
		if (te instanceof TileEntityIronChest) {
			TileEntityIronChest icte = (TileEntityIronChest)te;
			icte.handlePacketData(typ, items);
		}
	}

	public static Packet getPacket(TileEntityIronChest tileEntityIronChest) {
	  ByteArrayOutputStream bos=new ByteArrayOutputStream(140);
		DataOutputStream dos=new DataOutputStream(bos);
		int x=tileEntityIronChest.xCoord;
		int y=tileEntityIronChest.yCoord;
		int z=tileEntityIronChest.zCoord;
		int typ=tileEntityIronChest.getType().ordinal();
		int[] items=tileEntityIronChest.buildIntDataList();
		boolean hasStacks=(items!=null);
	  try {
      dos.writeInt(x);
      dos.writeInt(y);
      dos.writeInt(z);
      dos.writeByte(typ);
      dos.writeByte(hasStacks? 1 : 0);
      if (hasStacks) {
        for (int i=0; i<24; i++) {
          dos.writeInt(items[i]);
        }
      }
    } catch (IOException e) {
      // UNPOSSIBLE?
    }
	  Packet250CustomPayload pkt=new Packet250CustomPayload();
	  pkt.channel="IronChest";
	  pkt.data=bos.toByteArray();
	  pkt.length=bos.size();
	  pkt.isChunkDataPacket=true;
		return pkt;
	}
}