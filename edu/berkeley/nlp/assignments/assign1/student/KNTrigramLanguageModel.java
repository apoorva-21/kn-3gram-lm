package edu.berkeley.nlp.assignments.assign1.student;

import java.util.ArrayList;
import java.util.List;


import edu.berkeley.nlp.langmodel.NgramLanguageModel;
import edu.berkeley.nlp.langmodel.EnglishWordIndexer;
import edu.berkeley.nlp.util.CollectionUtils;

import edu.berkeley.nlp.util.TIntOpenHashMap;

import edu.berkeley.nlp.math.SloppyMath;

/**
 * HashMap from string to counts directly, no indexing
 * store counts for unigram, bigram and trigrams separately.
 * 
 */


public class KNTrigramLanguageModel implements NgramLanguageModel
{

	static final String STOP = NgramLanguageModel.STOP;
	
	static final String START = NgramLanguageModel.START;

	static final double discount = 0.8;
//	Decoding took 2072.373s
//	BLEU score on test data was BLEU(24.186)
	
	double l_discount;
//	static final double discount = 0.81;// BLEU 24.955
	//best discount factor
	
	double TRI_ALPHA_NUM;//used for indicating p mass to be distributed among the bigrams
	
	double BI_ALPHA_NUM;// used for indicating p mass to be distributed among the unigrams
	
	double DEFAULT_PROB = -1e3;
	long unique_words = 0;
	long unique_bigrams = 0;
	double l_unique_bigrams;
	long unique_trigrams = 0;
	
	long n_3gtd = 0;
	
	long total = 0;
	
	TIntOpenHashMap<String> ngram2count = new TIntOpenHashMap<String>();
	TIntOpenHashMap<String> ngram2fert = new TIntOpenHashMap<String>(); //count the number of unique contexts to each bigram/unigram
	TIntOpenHashMap<String> mword2fert_count = new TIntOpenHashMap<String>();
	TIntOpenHashMap<String> mword2fert = new TIntOpenHashMap<String>();
	TIntOpenHashMap<String> n_trigrams_w1w2 = new TIntOpenHashMap<String>();
	
	public KNTrigramLanguageModel(Iterable<List<String>> sentenceCollection) {
				
		//modify the code to only take 10000 sentences from the collection taken as input

		
		System.out.println("LOL This Time Building KNTrigramLanguageModel . . .");
		l_discount = Math.log(discount);
		//read each sentence, count number of unique tokens for hashtable
		//create hashmap from long to int with open addressing
		
		int sent = 0;
		int max_idx = 0;
		for (List<String> sentence : sentenceCollection) {
			
			List<String> stoppedSentence = new ArrayList<String>(sentence);
			
			stoppedSentence.add(0, START);
			stoppedSentence.add(0,START);
			stoppedSentence.add(STOP);
			
			int[] idx_ngram = new int[stoppedSentence.size()];
			
			int idxctr = 0;
			for (String word : stoppedSentence) {
				//add to the word-index mapping
				int idx = EnglishWordIndexer.getIndexer().addAndGetIndex(word);
				idx_ngram[idxctr++] = idx;
				max_idx = max_idx < idx ? idx : max_idx;
			}			
			
//			for(int l = 0; l < idx_ngram.length; l++)
//				System.out.println(idx_ngram[l] + " : "+ EnglishWordIndexer.getIndexer().get(idx_ngram[l]));

			
			List<Integer> space_idx = new ArrayList<Integer>();
			space_idx.add(-1);//to get 0 as the first index later while extracting ngrams
			
			sent++;
			if (sent % 1000000 == 0) 
//				break;
				System.out.println("On sentence " + sent);
			
			String str_sentence = "";
			for(int i = 0; i < stoppedSentence.size(); i++)
			{
				str_sentence = str_sentence + " " + stoppedSentence.get(i);
			}
			
			//skip the first extra space
			String stop_sentence = str_sentence.substring(1);
			
			
//			str_sentence = "hello there there there there there world";
			
//			stop_sentence = START + " " + str_sentence + " " + STOP;
			
			
			//store the indices of the spaces in the sentence
//			System.out.println("Sentence after inserting stop starts = " + stop_sentence);
			
			
			for(int i = 0; i < stop_sentence.length(); i++)
			{
				if(stop_sentence.charAt(i) == ' ')
				{
					space_idx.add(i);
				}
			}
			
			space_idx.add(stop_sentence.length());
			
			for(int k = 3; k > 0; k --)
			{	
				for(int i = 0; i < space_idx.size() - k; i ++)
				{
					if(k == 1) {
						//ignore the stop and start words for unigram model building
						if(i <= 1 || i == space_idx.size() - 2)
							continue;
					}
						
					int f_idx = space_idx.get(i) + 1;
					int t_idx = space_idx.get(i + k); 

					String ngram = stop_sentence.substring(f_idx, t_idx);
					
//					int[] sub_idx_ngram = new int[k];
//					System.out.println(idx_ngram.length);
//					for(int l = 0; l < k; l++)
//					{
//						sub_idx_ngram[l] = idx_ngram[i+l];
//					}
//					System.out.println("Stop Sentence : " + stop_sentence);
//					
//					for(int l = 0; l < idx_ngram.length; l++)
//						System.out.println(idx_ngram[l] + " : "+ EnglishWordIndexer.getIndexer().get(idx_ngram[l]));
//					
//					//TODO : fill in the sub idx ngram here
//					//select specific entries from 
//					System.out.println("Encoding : " + ngram);
//					for(int l = 0; l < sub_idx_ngram.length; l++)
//						System.out.println(sub_idx_ngram[l] + " : "+ EnglishWordIndexer.getIndexer().get(sub_idx_ngram[l]));
//					
//					long encoded_ngram = encodeNgram(sub_idx_ngram);
//					
//					sub_idx_ngram = decodeNgram(encoded_ngram);
//					
//					System.out.println("Decoded");
//					
//					for(int l = 0; l < sub_idx_ngram.length; l++)
//						System.out.println(sub_idx_ngram[l] + " : "+ EnglishWordIndexer.getIndexer().get(sub_idx_ngram[l]));
//					System.exit(1);
					
					//TODO : get the index of the ngram using the encoding procedure (String to long).
					//compute the hash of that and use it to store the count in a long-to-int mapping hashmap
					//WRITE SEPARATE FUNCTION TO GET THE INDICES FROM WORDS, AND THEN A SEPARATE FUNCTION TO GET LONG FROM THE INDEX ARRAY
					//REUSE THE LONG ENCODING FUNCTION WHEN CALLING THE GETCOUNT AND GETNGRAMLOGPROB FUNCTIONS
					
					if(ngram2count.get(ngram) == -1)
					{
						//does not exist in the map, increment number of unique n grams
						switch(k)
						{
							case 3:
								unique_trigrams ++; //to be used in the biigram fertility based P's denominatr
								//increment number of unique contexts for the bigram formed by last two words
								
								
								String fbg = ngram.substring(0, ngram.lastIndexOf(' '));
								n_trigrams_w1w2.increment(fbg, 1);
								
								String bg = ngram.substring(ngram.indexOf(' ') + 1);//last two words
								fertilityUpdate(bg, 2);
								
								String mword = ngram.substring(ngram.indexOf(' ') + 1, ngram.lastIndexOf(' '));
								mwordFertilityUpdate(mword);
								
								break;
								
							case 2:
								unique_bigrams ++; //to be used in the unigram fertility based P's denominatr
								
								//increment number of unique contexts for the unigram formed by the last word
								String ug = ngram.substring(ngram.indexOf(' ') + 1);
								fertilityUpdate(ug, 1);
								break;
								
							case 1 :
								unique_words ++;
						}
						
						ngram2count.put(ngram, 1);
					}
					else {
						ngram2count.increment(ngram, 1);
					}
				}
			}
			
		}
		
		System.out.println("Done building KNTrigramLanguageModel.");
		System.out.println("Size of indexer = "+ EnglishWordIndexer.getIndexer().size());
		
		System.out.println("Unique Unigrams = " + unique_words);
		System.out.println("Unique Bigrams = " + unique_bigrams);
		System.out.println("Unique Trigrams = " + unique_trigrams);
		
		
		l_unique_bigrams = Math.log(unique_bigrams);
	}

	public int getOrder() {
		return 3;
	}
	
	public void fertilityUpdate(String ngram, int order)
	{
		//increment the fertility of the ngram
		if(ngram2fert.get(ngram) == -1)
		{
			//never seen the ngram before
			ngram2fert.put(ngram, 1);
			
			//if its a bigram, check if the fertility is more than d
			if(order == 2)
			{
				//increment the number of bigrams with fertility greater than d (but since d < 1) 
				String mword = ngram.substring(0, ngram.indexOf(' '));
				if(mword2fert_count.get(mword) == -1)
				{
					mword2fert_count.put(mword, 1);
				}
				else {
					
					mword2fert_count.increment(mword,  1);
				}
				
			}
		}
		else {
			
			ngram2fert.increment(ngram,  1);
		}
		
		
		
	}
	public void mwordFertilityUpdate(String mword)
	{
		//increment the fertility of the middle word of the bigram (denominator of the bigram level)
		if(mword2fert.get(mword) == -1)
		{
			mword2fert.put(mword, 1);
		}
		else {
			
			mword2fert.increment(mword,  1);
		}
		
	}

	public double getNgramLogProbability(int[] ngram, int from, int to) {
		//REMEMBER to'th index is excluded i.e. [from, to)
		double p_trigram = 0.001;
		double p_bigram = 0.001;
		double p_unigram = 0.001;
		double retval = 0;
		
		if (to - from > 3) {
			  System.out.println("WARNING: to - from > 3 for KNTrigramLanguageModel");
			}
		
		to = to > (ngram.length) ? (ngram.length) : to;
		
		int[] sub_ngram = new int[to - from];
		
		for(int i = from; i < to; i++)
		{
			sub_ngram[i - from] = ngram[i];
		}
		
//		long ngram_count = getCount(sub_ngram);
		
		//check if the last token is of an unseen word type : ie gt indexer size
		if(sub_ngram[sub_ngram.length - 1] > EnglishWordIndexer.getIndexer().size()) {
			System.out.println("LOL");
			return DEFAULT_PROB;
		}
		//precompute and store the values for unigrams : these are undiscounted ratio of fertilities
		long unigram_fert = getFertility(new int[] {sub_ngram[sub_ngram.length - 1]});
		
		if(unigram_fert > 0) {
			double l_unigram_fert = Math.log(unigram_fert);
//			p_unigram = 1.0 * unigram_fert / unique_bigrams;//NOT DISCOUNTED
			p_unigram = l_unigram_fert - l_unique_bigrams;
//			retval = Math.log(p_unigram);
			retval = p_unigram;
			
		}
		else {
//			System.out.println(sub_ngram[sub_ngram.length - 1]);
			retval = DEFAULT_PROB;
			return retval;
		}
		
		
		if(to - from >= 2)//bigram
		{
			//the last two words of the ngram here
			int[] sub_bigram = new int[2];
			int t = sub_ngram.length - 2;
			for(int i = t; i < t+2;i ++)
			{
				sub_bigram[i - t] = sub_ngram[i];
			}
			
			double numerator = 1.0 * getFertility(sub_bigram);//fertility of w2, w3
			numerator = numerator - discount > 0 ? numerator - discount : numerator;
			//denominator is sum of fertilities of all bigrams with first word w2 i.e sumv(c'(w2,v))
			
			long denominator = mword2fert.get(EnglishWordIndexer.getIndexer().get(sub_bigram[0]));
			
			if(denominator > 0) {
				//p_bigram = (numerator > discount ? numerator - discount : numerator);
				
				BI_ALPHA_NUM = l_discount + Math.log(mword2fert_count.get(EnglishWordIndexer.getIndexer().get(sub_bigram[0])));///////////////
//				p_bigram = 1.0 * (numerator + BI_ALPHA_NUM * p_unigram) / denominator;
				p_bigram = SloppyMath.logAdd(Math.log(numerator) , (BI_ALPHA_NUM + p_unigram)) - Math.log(denominator);

			}
			else {
				p_bigram = p_unigram;
			}
			
//			retval = Math.log(p_bigram);
			retval = p_bigram;
		}
		
		if(to - from >= 3)//trigram
		{			
			//TODO : Compute the trigram estimate based on empirical trigram counts, bigram backoff, 
			long trigram_count = getCount(sub_ngram);
			
			double numerator = 1.0 * trigram_count;
			numerator = numerator - discount > 0 ? numerator - discount : numerator;
			
			//get the bigram context of the trigram, to compute the denominator
			int [] cxt_bg = new int[2];
			
			cxt_bg[0] = sub_ngram[0];
			cxt_bg[1] = sub_ngram[1];//w1,w2
			
			//common denominator for both count and alpha terms
			long denominator = getCount(cxt_bg);

			if(denominator > 0) {//only if the denominator is zero then alpha = 1
				long n_trigrams_w1w2 = getNTrigrams(cxt_bg);//number of trigrams starting with w1w2
				TRI_ALPHA_NUM = l_discount * Math.log(n_trigrams_w1w2);//WRONG IMPLEMENTATION for d > 1
				p_trigram = SloppyMath.logAdd(Math.log(numerator) , (TRI_ALPHA_NUM + p_bigram)) - Math.log(denominator);
				
			}
			else{
				//in case denominator is zero, then numerator has to be zero
				// full backoff to lower order, i.e TRI_ALPHA_NUM = 1
				p_trigram = p_bigram;	
			}
			
			retval = p_trigram;
		}

		return retval;
		
	}

	public long getCount(int[] ngram) {
		//accepts the ngram in the form of indices per word.
		
		String str_ngram = "";
		for(int i = 0; i < ngram.length; i++)
		{
			str_ngram = str_ngram + " " + EnglishWordIndexer.getIndexer().get(ngram[i]);
		}
		str_ngram = str_ngram.substring(1);
//		System.out.println(str_ngram);
		long ret_count = ngram2count.get(str_ngram);
		if (ret_count == -1)
			return 0;
		return ret_count;
		
	}
	
	public long getNTrigrams(int[] ngram) {
		//accepts the ngram in the form of indices per word.
		
		String str_ngram = "";
		for(int i = 0; i < ngram.length; i++)
		{
			str_ngram = str_ngram + " " + EnglishWordIndexer.getIndexer().get(ngram[i]);
		}
		str_ngram = str_ngram.substring(1);
//		System.out.println(str_ngram);
		long ret_count = n_trigrams_w1w2.get(str_ngram);
		if (ret_count == -1)
			return 0;
		return ret_count;
		
	}
	
	public long getFertility(int[] ngram)
	{
		String str_ngram = "";
		for(int i = 0; i < ngram.length; i++)
		{
			str_ngram = str_ngram + " " + EnglishWordIndexer.getIndexer().get(ngram[i]);
		}
		str_ngram = str_ngram.substring(1);//remove extra space
		
		long ret_count = ngram2fert.get(str_ngram);
		
		if (ret_count == -1)
			return 0;
		return ret_count;
	}
	
	public long encodeNgram(int[] ngram)
	{
		//64bits : 20w1-20w2-20w3-4info

		long retval = 0;
		int order = ngram.length; //1,2 or 3; store it in the first 4 bits
		retval = (long)order << 60;

		for(int i = order; i > 0; i --)
		{			
			retval = retval | (long)ngram[order - i] << (20 * (i - 1));
		}
		
		return retval;
	}
	
	public int[] decodeNgram(long encoded)
	{
		int order =(int) (encoded >>> 60);
		long filter = (1 << 20) - 1; 
		
		int[] retval = new int[(int)order];
		
		for(int i = order; i > 0; i--)
			retval[order - i] = (int)((encoded >>> (20 * (i - 1))) & filter);
		
		return retval;
	}
}
