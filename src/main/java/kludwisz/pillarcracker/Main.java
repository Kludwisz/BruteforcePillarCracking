package kludwisz.pillarcracker;

import java.util.List;

import com.seedfinding.latticg.util.LCG;
import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.util.math.NextLongReverser;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcfeature.structure.Shipwreck;

import kludwisz.pillarcracker.PillarCracker.Pillar;
import kludwisz.pillarcracker.PillarCracker.PillarArrangement;


public class Main {
	public static void main(String[] args) {
		crackPillars();
		crackStructureSeed(25862); // takes about 4 hours (single thread)
	}
	
	// example: cracking the pillar arrangement in dream's speedrunner vs 5 hunters rematch video
	public static void crackPillars() {
		
		// each arrangement has to have 10 pillars, otherwise it will likely return false data
		// it is recommended to start with a few "known" pillars and add more data later on if needed
		PillarArrangement target = new PillarArrangement(
				Pillar.ANY, 
				Pillar.T100, 
				Pillar.ANY, 
				Pillar.SMALLBOY, 
				Pillar.BIGBOY, 
				Pillar.SMALLCAGE, 
				Pillar.T97, 
				Pillar.M88, 
				Pillar.TALLCAGE, 
				Pillar.ANY);
		
		List<Integer> pillarseeds = PillarCracker.getPillarSeeds(target);
		for (int ps : pillarseeds) {
			System.out.println(ps + " -> example worldseed: " + PillarCracker.getFirstWorldSeed(ps));
			System.out.println(new PillarArrangement(ps).getPillars());
		}
	}
	
	// example: using the pillar seed with NextLongReverser to crack the structure seed (bruteforce over 32 bits)
	public static void crackStructureSeed(long pillarseed) {
		long nextL;
		long structseed;
		Shipwreck SHIPWRECK = new Shipwreck(MCVersion.v1_16_5);
		ChunkRand rand = new ChunkRand();
		long indicator = (1L<<24)-1;
		
		for (long up32=0; up32<(1L<<32); up32++) {
			nextL = (up32<<16) | pillarseed;
			List<Long> seeds = NextLongReverser.getSeeds(nextL);
			
			for (long xoredStructseed : seeds) {
				structseed = xoredStructseed ^ LCG.JAVA.multiplier;
				
				// filtering the remaining seeds using shipwreck type and loot
				for (int rx=0; rx<=6; rx++) for (int rz=-6; rz<=0; rz++) {
					CPos ship = SHIPWRECK.getInRegion(structseed, rx, rz, rand);
					rand.setCarverSeed(structseed, ship.getX(), ship.getZ(), MCVersion.v1_16_1);
					rand.nextInt(); 			// rotation
					int r = rand.nextInt(20); 	// shipwreck type
					if (r != 9)
						continue;
					
					ShipGen gen = new ShipGen();
					gen.generate(structseed, ship.getX(), ship.getZ(), rand);
					boolean passedLootCondition = gen.goodLootOfTreasureChest(structseed, rand);
					if (passedLootCondition)
						System.out.println(structseed + " ship at " + ship);
				}
			}
			if ((up32 & indicator) == 0L)
				System.out.println((up32 >> 24) + " / 256... ");
		}
	}	
}
