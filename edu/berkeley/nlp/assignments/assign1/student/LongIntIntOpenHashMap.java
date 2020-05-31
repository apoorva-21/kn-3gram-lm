package edu.berkeley.nlp.assignments.assign1.student;

import java.util.Arrays;
import java.util.Iterator;

public class LongIntIntOpenHashMap {
	
	private long[] keys;
	
	private int[] values1;
	
	private int[] values2;
	
	public static final boolean ONE = true; //values1 is for count, 2 for fert
	
	private int size = 0;

	private final long EMPTY_KEY;//since 0 will have all zeros, whereas 0 indexed unigram will have 0001 at start of the long

	private final double EXPAND_FACTOR;

	private final double MAX_LOAD_FACTOR;

	public LongIntIntOpenHashMap() {
		this(10);
	}

	public LongIntIntOpenHashMap(int initialCapacity_) {
		this(initialCapacity_, 0.7);
	}

	public LongIntIntOpenHashMap(int initialCapacity_, double loadFactor){
		this(initialCapacity_, loadFactor, 1.5);
	}

	public LongIntIntOpenHashMap(int initialCapacity_, double loadFactor, double expand_factor) {
		int cap = Math.max(5, (int) (initialCapacity_ / loadFactor));
		MAX_LOAD_FACTOR = loadFactor;
		EXPAND_FACTOR = expand_factor;
		EMPTY_KEY = 0;
		values1 = new int[cap];
		values2 = new int[cap];
		
		Arrays.fill(values1, -1);
		Arrays.fill(values2, -1);


		keys = new long[cap];
		Arrays.fill(keys, EMPTY_KEY);
	}

	public int getHashCode(long key){
		//hashfunc here without modding
		int hashval = (int)(key ^ (key >>> 32)) * 3875239;
		return hashval;
	}

	/**
	 * @param k
	 * @param keyArray
	 * @return
	 */
	private int getInitialPos(long k, long[] keyArray) {
		int hash = getHashCode(k);
		int pos = hash % keyArray.length;
		if (pos < 0) {
			pos += keyArray.length;
		}
		// 	pos += keyArray.length;
		return pos;
	}

	private boolean putHelp(long k, int v, long[] keyArray, int[] valueArray1, int[] valueArray2, boolean which) {
			int pos = getInitialPos(k, keyArray);
			long curr = keyArray[pos];
			while (curr != EMPTY_KEY && curr != k) {
				pos++;
				if (pos == keyArray.length) pos = 0;
				curr = keyArray[pos];
			}
			if(which == ONE)
				valueArray1[pos] = v;
			else
				valueArray2[pos] = v;
			
			if (curr == EMPTY_KEY) {
				size++;
				
				keyArray[pos] = k;
				return true;
			}
			return false;
	}

	private void rehash() {
//        System.out.println("Load Factor Limit Reached, Resizing to "+size*EXPAND_FACTOR);
		long[] newKeys = new long[(int)(keys.length * EXPAND_FACTOR)]; //increase the size by 1.5x
		int[] newValues1 = new int[(int)(values1.length * EXPAND_FACTOR)];
		int[] newValues2 = new int[(int)(values2.length * EXPAND_FACTOR)];

		Arrays.fill(newValues1, -1);
		Arrays.fill(newValues2, -1);

		size = 0;
		for (int i = 0; i < keys.length; ++i) {
			long curr = keys[i];
			if (curr != EMPTY_KEY) {
			 	int val = values1[i];
				putHelp(curr, val, newKeys, newValues1, newValues2, ONE);
			 	val = values2[i];
				putHelp(curr, val, newKeys, newValues1, newValues2, !ONE);
			}
		}
		keys = newKeys;
		values1 = newValues1;
		values2 = newValues2;
	}

	public boolean put(long k, int v, boolean which) {
		if (size / (double) keys.length > MAX_LOAD_FACTOR) {
			rehash();
		}
		return putHelp(k, v, keys, values1, values2, which);

	}

	/**
	 * @param k
	 * @return
	 */
	private int find(long k) {
		int pos = getInitialPos(k, keys);
		long curr = keys[pos];
		while (curr != EMPTY_KEY && curr != k) {
			pos++;
			if (pos == keys.length) pos = 0;
			curr = keys[pos];
		}
		return pos;
	}

	public int get(long k, boolean which) {
		int pos = find(k);
		if(which == ONE)
			return values1[pos];
		return values2[pos];
	}

	public void increment(long k, int c, boolean which) {
		int pos = find(k);
		long currKey = keys[pos];
		if (currKey == EMPTY_KEY) {
			put(k, c, which);	
		}
		else{
			if(which == ONE)
			values1[pos]++;
			else values2[pos]++;
		}
	}

    public int size()
    {
        return size;
    }

    // public void computeLogs()
    // {
    // 	//compute the logs and store them in place of the raw values
    // 	for(int i = 0; i < values.length; i++)
    // 	{
    // 		if(values[i] > 0)
    // 			values[i] =  Math.log(values[i]);
    // 	}
    // }

//    public int getNumMatches(long matchval, long filter, int type)
//    {
//    	int n_matches = 0;
//    	for(int i = 0; i < keys.length; i++)
//    	{
//    		if(((keys[i] & filter) == matchval) && type == ((int)(keys[i] >>> 60)))
//    			n_matches++;
//    	}
//    	System.out.println("Getting number of matches:"+n_matches);
//
//    	return n_matches;
//    }
//
//    public int getMatchSum(long matchval, long filter, int type)
//    {
//    	int sum = 0;
//    	for(int i = 0; i < keys.length; i++)
//    	{
//			if(((keys[i] & filter) == matchval) && type == ((int)(keys[i] >>> 60)))
//				sum += values[i];
//    	}
//    	System.out.println("Getting the sum of fertilities:"+ sum);
//
//    	return sum;
//    }
	
    public int getMaxVal(boolean which)
    {
    	int max = 0;
    	for(int i = 0; i < keys.length; i++)
    	{
    		if(which)
    		max = max < values1[i] ? values1[i] : max;
    		
    		else max = max < values2[i] ? values2[i] : max;
    			
    	}
    	
    	return max;
    }
}
