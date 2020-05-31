package edu.berkeley.nlp.assignments.assign1.student;

import java.util.Arrays;
import java.util.Iterator;

//import edu.berkeley.nlp.util.CollectionUtils;
//import edu.berkeley.nlp.util.TIntOpenHashMap.Entry;
//import edu.berkeley.nlp.util.TIntOpenHashMap.EntryIterator;
//import edu.berkeley.nlp.util.TIntOpenHashMap.MapIterator;

public class LongIntOpenHashMap {
	
	private long[] keys;
	
	private int[] values;
	
	private int size = 0;

	private final long EMPTY_KEY;//since 0 will have all zeros, whereas 0 indexed unigram will have 0001 at start of the long

	private final double EXPAND_FACTOR;

	private final double MAX_LOAD_FACTOR;

	public LongIntOpenHashMap() {
		this(10);
	}

	public LongIntOpenHashMap(int initialCapacity_) {
		this(initialCapacity_, 0.7);
	}

	public LongIntOpenHashMap(int initialCapacity_, double loadFactor){
		this(initialCapacity_, loadFactor, 1.5);
	}

	public LongIntOpenHashMap(int initialCapacity_, double loadFactor, double expand_factor) {
		int cap = Math.max(5, (int) (initialCapacity_ / loadFactor));
		MAX_LOAD_FACTOR = loadFactor;
		EXPAND_FACTOR = expand_factor;
		EMPTY_KEY = 0;
		values = new int[cap];
		Arrays.fill(values, -1);

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

	private boolean putHelp(long k, int v, long[] keyArray, int[] valueArray) {
			int pos = getInitialPos(k, keyArray);
			long curr = keyArray[pos];
			while (curr != EMPTY_KEY && curr != k) {
				pos++;
				if (pos == keyArray.length) pos = 0;
				curr = keyArray[pos];
			}
            valueArray[pos] = v;
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
		int[] newValues = new int[(int)(values.length * EXPAND_FACTOR)];
		Arrays.fill(newValues, -1);
		size = 0;
		for (int i = 0; i < keys.length; ++i) {
			long curr = keys[i];
			if (curr != EMPTY_KEY) {
			 	int val = values[i];
				putHelp(curr, val, newKeys, newValues);
			}
		}
		keys = newKeys;
		values = newValues;
	}

	public boolean put(long k, int v) {
		if (size / (double) keys.length > MAX_LOAD_FACTOR) {
			rehash();
		}
		return putHelp(k, v, keys, values);

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

	public int get(long k) {
		int pos = find(k);
		return values[pos];
	}

	public void increment(long k, int c) {
		int pos = find(k);
		long currKey = keys[pos];
		if (currKey == EMPTY_KEY) {
			put(k, c);
		} else
			values[pos]++;
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

    public int getNumMatches(long matchval, long filter, int type)
    {
    	int n_matches = 0;
    	for(int i = 0; i < keys.length; i++)
    	{
    		if(((keys[i] & filter) == matchval) && type == ((int)(keys[i] >>> 60)))
    			n_matches++;
    	}
    	System.out.println("Getting number of matches:"+n_matches);

    	return n_matches;
    }

    public int getMatchSum(long matchval, long filter, int type)
    {
    	int sum = 0;
    	for(int i = 0; i < keys.length; i++)
    	{
			if(((keys[i] & filter) == matchval) && type == ((int)(keys[i] >>> 60)))
				sum += values[i];
    	}
    	System.out.println("Getting the sum of fertilities:"+ sum);

    	return sum;
    }
	
    public int getMaxVal()
    {
    	int max = 0;
    	for(int i = 0; i < values.length; i++)
    	{
    		max = max < values[i] ? values[i] : max;
    	}
    	
    	return max;
    }
}
