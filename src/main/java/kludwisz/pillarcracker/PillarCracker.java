package kludwisz.pillarcracker;

import java.util.ArrayList;
import java.util.List;

import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mcmath.util.Mth;

public class PillarCracker {
	// cracks a given pillar arrangement; returns all possible pillar seeds that generate that arrangement
	public static List<Integer> getPillarSeeds(PillarArrangement target) {
		ArrayList<Integer> pillarSeeds = new ArrayList<>();
		
		for (int pillarseed = 0; pillarseed < (1L<<16); pillarseed++) {
			PillarArrangement arrangementFromSeed = new PillarArrangement(pillarseed);
			if (target.matches(arrangementFromSeed)) {
				pillarSeeds.add(pillarseed);
			}
		}

		// System.out.println("returned " + pillarSeeds.size() + " pillar seeds.");
		return pillarSeeds;
	}
	
	// returns a structure seed that has the given pillarseed (useful for in-game testing, not optimised)
	public static long getFirstWorldSeed(long pillarseed) {
		ChunkRand r = new ChunkRand();
		for (int w=0; w<(1L<<48); w++) {
			r.setSeed(w);
			if ((r.nextLong() & Mth.MASK_16) == (pillarseed & Mth.MASK_16))
				return w;
		}
		return 0;
	}
	
	public static class PillarArrangement {
		private List<Pillar> pillars;
		
		public PillarArrangement (Pillar... pillars) {
			this.pillars = List.of(pillars);		
		}
		
		public PillarArrangement (List<Pillar> pillars) {
			this.pillars = pillars;
		}
		
		public PillarArrangement (int pillarseed) {
			ChunkRand rand = new ChunkRand();
			rand.setSeed(pillarseed);
			List<Pillar> p = Pillar.getAll();
			rand.shuffle(p);
			this.pillars = p;
		}
		
		public boolean matches(PillarArrangement other) {
			Pillar a,b;
			for (int it=0; it<10; it++) {
				a = this.pillars.get(it);
				b = other.pillars.get(it);
				
				if (!a.matches(b))
					return false;
			}
			return true;
		}
		
		public List<Pillar> getPillars() {
			return this.pillars;
		}
	}
	
	public static enum Pillar {
		SMALLBOY,
		SMALLCAGE,
		TALLCAGE,
		M85,
		M88,
		M91,
		T94,
		T97,
		T100,
		BIGBOY,
		ANY;
		
		public static ArrayList<Pillar> getAll() {
			ArrayList<Pillar> result = new ArrayList<>();
			for (Pillar p : Pillar.values()) {
				if (p != Pillar.ANY)
					result.add(p);
			}
			return result;
		}
		
		public boolean matches(Pillar other) {
			return this.name() == Pillar.ANY.name() || other.name() == Pillar.ANY.name() || this.name() == other.name();
		}
	}
}
