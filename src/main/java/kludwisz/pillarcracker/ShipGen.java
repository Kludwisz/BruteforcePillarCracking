package kludwisz.pillarcracker;

import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.util.block.BlockBox;
import com.seedfinding.mccore.util.block.BlockMirror;
import com.seedfinding.mccore.util.block.BlockRotation;
import com.seedfinding.mccore.util.data.Pair;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcfeature.loot.ChestContent;
import com.seedfinding.mcfeature.loot.LootContext;
import com.seedfinding.mcfeature.loot.LootTable;
import com.seedfinding.mcfeature.loot.MCLootTables;
import com.seedfinding.mcfeature.loot.item.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

// shortcut for shipwreck generation (copied from ShipwreckGenerator and optimised for seed filtering)
public class ShipGen {
	private BlockRotation rotation = null;
	private String type = null;
	private BlockBox piece = null;

	public boolean generate(long structseed, int chunkX, int chunkZ, ChunkRand rand) {
		rand.setCarverSeed(structseed, chunkX, chunkZ, MCVersion.v1_16_1);
		rotation = BlockRotation.getRandom(rand);
		String[] arr = STRUCTURE_LOCATION_OCEAN;
		type = arr[rand.nextInt(arr.length)];
		BPos size = STRUCTURE_SIZE.get(type);
		BPos anchor = new CPos(chunkX, chunkZ).toBlockPos(90);
		BPos pivot = new BPos(4, 0, 15); // this is fixed for shipwreck
		BlockMirror mirror = BlockMirror.NONE; // this is fixed for shipwreck
		BlockBox blockBox = BlockBox.getBoundingBox(anchor, rotation, pivot, mirror, size);
		piece = blockBox.getRotated(rotation);
		//System.out.println(type);
		return true;
	}

	public boolean goodLootOfTreasureChest(long structseed, ChunkRand rand) {
		HashMap<LootType, BPos> lootPos = STRUCTURE_TO_LOOT.get(type);
		List<Pair<LootType, BPos>> res = new ArrayList<>();
		
		for(LootType lootType : lootPos.keySet()) {
			BPos offset = lootPos.get(lootType);
			BPos chestPos = piece.getInside(offset, rotation);
			res.add(new Pair<>(lootType, chestPos));
		}
		
		HashMap<CPos, ChunkRand> randoms = new HashMap<>();
		HashMap<CPos, Integer> chestsInChunk = new HashMap<>();
		LootContext ctx = new LootContext(0L);

		for (Pair<LootType, BPos> para : res) {
			BPos pos = para.getSecond();
			CPos cPos = pos.toChunkPos();
			if (chestsInChunk.containsKey(cPos))
				chestsInChunk.put(cPos, chestsInChunk.get(cPos) + 1);
			else
				chestsInChunk.put(cPos, 1);
		}

		for (Pair<LootType, BPos> para : res) {
			BPos pos = para.getSecond();
			CPos cPos = pos.toChunkPos();
			
			if (!randoms.containsKey(cPos)) {
				ChunkRand c = new ChunkRand();
				c.setDecoratorSeed(structseed, cPos.getX()<<4, cPos.getZ()<<4, 40006, MCVersion.v1_16_1);
				for (int i=0; i<chestsInChunk.get(cPos); i++)
					c.nextLong();
				randoms.put(cPos, c);
			}
			
			//randoms.get(cPos).nextLong(); // this is here for an unknown reason
			long lootseed = randoms.get(cPos).nextLong();
			ctx.setSeed(lootseed);
			if (para.getFirst() == LootType.TREASURE_CHEST) {
				List<ItemStack> items =  MCLootTables.SHIPWRECK_TREASURE_CHEST.generate(ctx);
				int fitness=0;
				for (ItemStack is : items) {
					// please ignore the code below
					if (is.getItem().getName() == "iron_ingot") {
						if (is.getCount() != 14)
							break;
						fitness++;
					}
					else if (is.getItem().getName() == "iron_nugget") {
						if (is.getCount() != 11)
							break;
						fitness++;
					}
					else if (is.getItem().getName() == "gold_nugget") {
						if (is.getCount() != 2)
							break;
						fitness++;
					}
					else if (is.getItem().getName() == "diamond") {
						if (is.getCount() != 2)
							break;
						fitness++;
					}
					else if (is.getItem().getName() == "lapis_lazuli") {
						if (is.getCount() != 14)
							break;
						fitness++;
					}
					else
						break;
				}
				if (fitness == 5)
					System.out.println(items);
				return (fitness == 5);
			}
		}
		return false;
	}


	public enum LootType {
		SUPPLY_CHEST(MCLootTables.SHIPWRECK_SUPPLY_CHEST, ChestContent.ChestType.SINGLE_CHEST),
		TREASURE_CHEST(MCLootTables.SHIPWRECK_TREASURE_CHEST, ChestContent.ChestType.SINGLE_CHEST),
		MAP_CHEST(MCLootTables.SHIPWRECK_MAP_CHEST, ChestContent.ChestType.SINGLE_CHEST);

		public final LootTable lootTable;
		public final ChestContent.ChestType chestType;

		LootType(LootTable lootTable, ChestContent.ChestType chestType) {
			this.lootTable = lootTable;
			this.chestType = chestType;
		}
	}

	private static final String[] STRUCTURE_LOCATION_OCEAN = new String[] {
		"with_mast",
		"upsidedown_full",
		"upsidedown_fronthalf",
		"upsidedown_backhalf",
		"sideways_full",
		"sideways_fronthalf",
		"sideways_backhalf",
		"rightsideup_full",
		"rightsideup_fronthalf",
		"rightsideup_backhalf",
		"with_mast_degraded",
		"upsidedown_full_degraded",
		"upsidedown_fronthalf_degraded",
		"upsidedown_backhalf_degraded",
		"sideways_full_degraded",
		"sideways_fronthalf_degraded",
		"sideways_backhalf_degraded",
		"rightsideup_full_degraded",
		"rightsideup_fronthalf_degraded",
		"rightsideup_backhalf_degraded"
	};
	private static final HashMap<String, LinkedHashMap<LootType, BPos>> STRUCTURE_TO_LOOT = new HashMap<>();
	private static final HashMap<String, BPos> STRUCTURE_SIZE = new HashMap<>();

	static {
		// we are y+1
		STRUCTURE_TO_LOOT.put("rightsideup_backhalf", new LinkedHashMap<LootType, BPos>() {{
			put(LootType.MAP_CHEST, new BPos(5, 3, 6));
			put(LootType.TREASURE_CHEST, new BPos(6, 5, 12));
		}});
		STRUCTURE_SIZE.put("rightsideup_backhalf", new BPos(9, 9, 16));
		STRUCTURE_TO_LOOT.put("rightsideup_backhalf_degraded", new LinkedHashMap<LootType, BPos>() {{
			put(LootType.MAP_CHEST, new BPos(5, 3, 6));
			put(LootType.TREASURE_CHEST, new BPos(6, 5, 12));
		}});
		STRUCTURE_SIZE.put("rightsideup_backhalf_degraded", new BPos(9, 9, 16));
		STRUCTURE_TO_LOOT.put("rightsideup_fronthalf", new LinkedHashMap<LootType, BPos>() {{
			put(LootType.SUPPLY_CHEST, new BPos(4, 3, 8));
		}});
		STRUCTURE_SIZE.put("rightsideup_fronthalf", new BPos(9, 9, 24));
		STRUCTURE_TO_LOOT.put("rightsideup_fronthalf_degraded", new LinkedHashMap<LootType, BPos>() {{
			put(LootType.SUPPLY_CHEST, new BPos(4, 3, 8));
		}});
		STRUCTURE_SIZE.put("rightsideup_fronthalf_degraded", new BPos(9, 9, 24));
		STRUCTURE_TO_LOOT.put("rightsideup_full", new LinkedHashMap<LootType, BPos>() {{
			put(LootType.SUPPLY_CHEST, new BPos(4, 3, 8));
			put(LootType.MAP_CHEST, new BPos(5, 3, 18));
			put(LootType.TREASURE_CHEST, new BPos(6, 5, 24));
		}});
		STRUCTURE_SIZE.put("rightsideup_full", new BPos(9, 9, 28));
		STRUCTURE_TO_LOOT.put("rightsideup_full_degraded", new LinkedHashMap<LootType, BPos>() {{
			put(LootType.SUPPLY_CHEST, new BPos(4, 3, 8));
			put(LootType.MAP_CHEST, new BPos(5, 3, 18));
			put(LootType.TREASURE_CHEST, new BPos(6, 5, 24));
		}});
		STRUCTURE_SIZE.put("rightsideup_full_degraded", new BPos(9, 9, 28));
		STRUCTURE_TO_LOOT.put("sideways_backhalf", new LinkedHashMap<LootType, BPos>() {{
			put(LootType.TREASURE_CHEST, new BPos(3, 3, 13));
			put(LootType.MAP_CHEST, new BPos(6, 4, 8));
		}});
		STRUCTURE_SIZE.put("sideways_backhalf", new BPos(9, 9, 17));
		STRUCTURE_TO_LOOT.put("sideways_backhalf_degraded", new LinkedHashMap<LootType, BPos>() {{
			put(LootType.TREASURE_CHEST, new BPos(3, 3, 13));
			put(LootType.MAP_CHEST, new BPos(6, 4, 8));
		}});
		STRUCTURE_SIZE.put("sideways_backhalf_degraded", new BPos(9, 9, 17));
		STRUCTURE_TO_LOOT.put("sideways_fronthalf", new LinkedHashMap<LootType, BPos>() {{
			put(LootType.SUPPLY_CHEST, new BPos(5, 4, 8));
		}});
		STRUCTURE_SIZE.put("sideways_fronthalf", new BPos(9, 9, 24));
		STRUCTURE_TO_LOOT.put("sideways_fronthalf_degraded", new LinkedHashMap<LootType, BPos>() {{
			put(LootType.SUPPLY_CHEST, new BPos(5, 4, 8));
		}});
		STRUCTURE_SIZE.put("sideways_fronthalf_degraded", new BPos(9, 9, 24));
		STRUCTURE_TO_LOOT.put("sideways_full", new LinkedHashMap<LootType, BPos>() {{
			put(LootType.TREASURE_CHEST, new BPos(3, 3, 24));
			put(LootType.SUPPLY_CHEST, new BPos(5, 4, 8));
			put(LootType.MAP_CHEST, new BPos(6, 4, 19));
		}});
		STRUCTURE_SIZE.put("sideways_full", new BPos(9, 9, 28));
		STRUCTURE_TO_LOOT.put("sideways_full_degraded", new LinkedHashMap<LootType, BPos>() {{
			put(LootType.TREASURE_CHEST, new BPos(3, 3, 24));
			put(LootType.SUPPLY_CHEST, new BPos(5, 4, 8));
			put(LootType.MAP_CHEST, new BPos(6, 4, 19));
		}});
		STRUCTURE_SIZE.put("sideways_full_degraded", new BPos(9, 9, 28));
		STRUCTURE_TO_LOOT.put("upsidedown_backhalf", new LinkedHashMap<LootType, BPos>() {{
			put(LootType.TREASURE_CHEST, new BPos(2, 3, 12));
			put(LootType.MAP_CHEST, new BPos(3, 6, 5));
		}});
		STRUCTURE_SIZE.put("upsidedown_backhalf", new BPos(9, 9, 16));
		STRUCTURE_TO_LOOT.put("upsidedown_backhalf_degraded", new LinkedHashMap<LootType, BPos>() {{
			put(LootType.TREASURE_CHEST, new BPos(2, 3, 12));
			put(LootType.MAP_CHEST, new BPos(3, 6, 5));
		}});
		STRUCTURE_SIZE.put("upsidedown_backhalf_degraded", new BPos(9, 9, 16));
		STRUCTURE_TO_LOOT.put("upsidedown_fronthalf", new LinkedHashMap<LootType, BPos>() {{
			put(LootType.MAP_CHEST, new BPos(3, 6, 17));
			put(LootType.SUPPLY_CHEST, new BPos(4, 6, 8));
		}});
		STRUCTURE_SIZE.put("upsidedown_fronthalf", new BPos(9, 9, 22));
		STRUCTURE_TO_LOOT.put("upsidedown_fronthalf_degraded", new LinkedHashMap<LootType, BPos>() {{
			put(LootType.MAP_CHEST, new BPos(3, 6, 17));
			put(LootType.SUPPLY_CHEST, new BPos(4, 6, 8));
		}});
		STRUCTURE_SIZE.put("upsidedown_fronthalf_degraded", new BPos(9, 9, 22));
		STRUCTURE_TO_LOOT.put("upsidedown_full", new LinkedHashMap<LootType, BPos>() {{
			put(LootType.TREASURE_CHEST, new BPos(2, 3, 24));
			put(LootType.MAP_CHEST, new BPos(3, 6, 17));
			put(LootType.SUPPLY_CHEST, new BPos(4, 6, 8));
		}});
		STRUCTURE_SIZE.put("upsidedown_full", new BPos(9, 9, 28));
		STRUCTURE_TO_LOOT.put("upsidedown_full_degraded", new LinkedHashMap<LootType, BPos>() {{
			put(LootType.TREASURE_CHEST, new BPos(2, 3, 24));
			put(LootType.MAP_CHEST, new BPos(3, 6, 17));
			put(LootType.SUPPLY_CHEST, new BPos(4, 6, 8));
		}});
		STRUCTURE_SIZE.put("upsidedown_full_degraded", new BPos(9, 9, 28));
		STRUCTURE_TO_LOOT.put("with_mast", new LinkedHashMap<LootType, BPos>() {{
			put(LootType.SUPPLY_CHEST, new BPos(4, 3, 9));
			put(LootType.MAP_CHEST, new BPos(5, 3, 18));
			put(LootType.TREASURE_CHEST, new BPos(6, 5, 24));
		}});
		STRUCTURE_SIZE.put("with_mast", new BPos(9, 21, 28));
		STRUCTURE_TO_LOOT.put("with_mast_degraded", new LinkedHashMap<LootType, BPos>() {{
			put(LootType.SUPPLY_CHEST, new BPos(4, 3, 9));
			put(LootType.MAP_CHEST, new BPos(5, 3, 18));
			put(LootType.TREASURE_CHEST, new BPos(6, 5, 24));
		}});
		STRUCTURE_SIZE.put("with_mast_degraded", new BPos(9, 21, 28));
	}

}
