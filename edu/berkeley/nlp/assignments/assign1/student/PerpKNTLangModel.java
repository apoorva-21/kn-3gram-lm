package edu.berkeley.nlp.assignments.assign1.student;

import java.util.ArrayList;
import java.util.List;

import edu.berkeley.nlp.langmodel.NgramLanguageModel;
import edu.berkeley.nlp.langmodel.EnglishWordIndexer;
import edu.berkeley.nlp.util.CollectionUtils;
import edu.berkeley.nlp.math.SloppyMath;

//CACHING of the LOG RATIOS for unigram fertility / unique_bigrams
/**
 * 
 * @author apoorva89
 *
 */
public class PerpKNTLangModel implements NgramLanguageModel
{

	static final String STOP = NgramLanguageModel.STOP;
	
	static final String START = NgramLanguageModel.START;

	static final double discount3 = 0.9;//0.85;
	static final double discount2 = 0.9;//0.81;
	double l_discount3;
	double l_discount2;
//	static final double discount = 0.81;// BLEU 24.955
	
	double TRI_ALPHA_NUM;//used for indicating p mass to be distributed among the bigrams
	
	double BI_ALPHA_NUM;// used for indicating p mass to be distributed among the unigrams
	
	double DEFAULT_PROB = -1e3; //default value to return in case of unknown words
	long unique_words = 0;
	long unique_bigrams = 0;
	long unique_trigrams = 0;
		
	long total = 0;
	
	double l_unique_bigrams;
	
	boolean COUNT, FERT, MWORDFC, MWORDC;
	
	boolean is_perp;
	LongIntOpenHashMap ngram2count;//store unigram and trigram counts
	LongIntIntOpenHashMap bigramcounts;
	LongIntOpenHashMap ngram2fert;//store unigram fertilities
	LongIntIntOpenHashMap mwordcounts;
	LongIntOpenHashMap ntrigramsw1w2;
	/*
	 * ngram2count = 42196282
	bigramcounts = 8374231
	ngram2fert = 495172
	mword2fert = 495171
	ntrigramsw1w2 = 8339343
	 */
	
	public PerpKNTLangModel(Iterable<List<String>> sentenceCollection) {
		
		COUNT = LongIntIntOpenHashMap.ONE;
		FERT = !COUNT;
		
		MWORDC = LongIntIntOpenHashMap.ONE; //number of unique trigrams with mword
		MWORDFC = !MWORDC;//fertility of the bigram starting with mword
		
		ngram2count = new LongIntOpenHashMap(42196282, 0.9);
		ngram2fert = new LongIntOpenHashMap(495172, 0.9);
		
		bigramcounts = new LongIntIntOpenHashMap(8374231, 0.9);
		
		mwordcounts = new LongIntIntOpenHashMap(495171, 0.9);
		ntrigramsw1w2 = new LongIntOpenHashMap(8339343, 0.9);
//		
//		ngram2count = new LongIntOpenHashMap(10, 0.7);
//		ngram2fert = new LongIntOpenHashMap(10, 0.7);
//		
//		bigramcounts = new LongIntIntOpenHashMap(10, 0.7);
//		
//		mwordcounts = new LongIntIntOpenHashMap(10, 0.7);
////		mword2fert_count = new LongIntOpenHashMap();
//		ntrigramsw1w2 = new LongIntOpenHashMap(10, 0.7);
		
		l_discount3 = Math.log(discount3);

		l_discount2 = Math.log(discount2);
		//modify the code to only take 10000 sentences from the collection taken as input
		
		System.out.println("Building KNTrigramLanguageModel v4 . . .");
		
		int sent = 0;
		for (List<String> sentence : sentenceCollection) {
			
			List<String> stoppedSentence = new ArrayList<String>(sentence);
			
			stoppedSentence.add(0, START);
			stoppedSentence.add(0,START);
			stoppedSentence.add(STOP);
			
			int[] idx_sent = new int[stoppedSentence.size()];
			
			int idxctr = 0;
			for (String word : stoppedSentence) {
				//add to the word-index mapping
				idx_sent[idxctr++] = EnglishWordIndexer.getIndexer().addAndGetIndex(word);
			}
			
			sent++;
			if (sent % 1000000 == 0)
				System.out.println("On sentence " + sent);
			
			//For Perplexity Calculation, but gives better BLEU Score 25.017 on d2 = d3 = 0.9
			//But Fails Count checks
//			if(sent == 8990000)
//				break;

	
			for(int k = 3; k > 0; k --)
			{//window size
				
				for(int i = 0; i <= idx_sent.length - k; i ++)
				{
					if(k == 1) {
						//ignore the stop and start words for unigram model building: 0, 1, length-1 th indices
						if(i <= 1 || i == idx_sent.length - 1)
							continue;
					}
									
					int[] idx_ngram = new int[k];
					for(int l = 0; l < k; l++)
					{
						idx_ngram[l] = idx_sent[i+l];
					}

					
					long encoded_ngram = encodeNgram(idx_ngram);


					if((k != 2 && ngram2count.get(encoded_ngram) == -1) || (k==2 && bigramcounts.get(encoded_ngram, COUNT) == -1))
					{
						//does not exist in the map, increment number of unique n grams
						switch(k)
						{
							case 3:
								unique_trigrams ++; //to be used in the trigram fertility based P's alpha
								//increment number of unique contexts for the bigram formed by last two words

//								long enc_bigram = encodeNgram();
								fertilityUpdate(new int[]{idx_ngram[1], idx_ngram[2]});
								
								ntrigramsw1w2.increment(encodeNgram(new int[]{idx_ngram[0], idx_ngram[1]}), 1);
								
								mwordcounts.increment(encodeNgram(new int[] {idx_ngram[1]}), 1, MWORDFC);
								
								ngram2count.put(encoded_ngram, 1);
								break;
								
							case 2:
								unique_bigrams ++; //to be used in the unigram fertility based P's denominatr
								
								//increment number of unique contexts for the unigram formed by the last word
								// String ug = ngram.substring(ngram.indexOf(' ') + 1);
//								long enc_unigram = encodeNgram();
								fertilityUpdate(new int[]{idx_ngram[1]});
								bigramcounts.put(encoded_ngram, 1, COUNT);
								break;
								
							case 1 :
								ngram2count.put(encoded_ngram, 1);
								unique_words ++;
						}
						

					}
					else {
						if(idx_ngram.length == 3) ngram2count.increment(encoded_ngram, 1);
						
						else if(idx_ngram.length == 2) bigramcounts.increment(encoded_ngram, 1, COUNT);
						
						else //if(idx_ngram[0] == EnglishWordIndexer.getIndexer().indexOf("the"))
							ngram2count.increment(encoded_ngram, 1);
					}
				}
			}
			
		}
		
		System.out.println("Done building KNTrigramLanguageModel");
		System.out.println("Size of indexer = "+ EnglishWordIndexer.getIndexer().size());


		l_unique_bigrams = Math.log(unique_bigrams);
			
		System.out.println("Sizes : ");
		System.out.println("ngram2count = "+ ngram2count.size());
		System.out.println("bigramcounts = "+ bigramcounts.size());
		System.out.println("ngram2fert = " +ngram2fert.size());
		System.out.println("mword2fert = " +mwordcounts.size());
		System.out.println("ntrigramsw1w2 = " +ntrigramsw1w2.size());
		
		int subset_size = 10000;
//		is_perp = true;
//		System.out.println("Perplexity on a subset of the training data = " + getPerplexity(sentenceCollection, subset_size));
		is_perp = false;
//		System.exit(1);
	}

	public double getPerplexity(Iterable<List<String>> sentenceCollection, int subset_size) {
		
		int sent = 0;
		double perp = 0;
		double final_sum = 0;
		double n_words = 0;
		
		for(List<String> sentence : sentenceCollection) {
			
			sent++;
			
			if(sent >= 8990000)
			{
			double sent_sum = 0;
			
			List<String> stoppedSentence = new ArrayList<String>(sentence);
		
			stoppedSentence.add(0, START);
			stoppedSentence.add(0,START);
			stoppedSentence.add(STOP);
		
			n_words += stoppedSentence.size();
			int[] idx_sent = new int[stoppedSentence.size()];
		
			int idxctr = 0;
			for (String word : stoppedSentence) {
				idx_sent[idxctr++] = EnglishWordIndexer.getIndexer().addAndGetIndex(word);
			}

				int k = 3;
				for(int i = 0; i <= idx_sent.length - k; i ++)
				{			
					int[] idx_ngram = new int[k];
					for(int l = 0; l < k; l++)
					{
						idx_ngram[l] = idx_sent[i+l];
					}

				
					sent_sum += getNgramLogProbability(idx_ngram, 0, idx_ngram.length);

	
				}
				
				final_sum += sent_sum;
		
			}
		}
		double pow_val = (-1.0 * final_sum) / (Math.log(2.0) * n_words);
		System.out.println(final_sum);
		System.out.println(n_words);
		System.out.println(pow_val);
		perp = Math.pow(2, pow_val);
		
		return perp;
	}
	
	
	public int getOrder() {
		return 3;
	}
	

	public long encodeNgram(int[] ngram)
	{
		//64bits : 4info-20w1-20w2-20w3

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

	public void fertilityUpdate(int[] ngram)
	{
		int order = ngram.length;
		//increment the fertility of the ngram
		long encoded_ngram = encodeNgram(ngram);
		switch(order)
		{
			case 1:
				ngram2fert.increment(encoded_ngram,  1);
				break;
			case 2:
				if(bigramcounts.get(encoded_ngram, FERT) == -1)
				{
					bigramcounts.put(encoded_ngram, 1, FERT);
					mwordcounts.increment(encodeNgram(new int[] {ngram[0]}),  1, MWORDC);
				}
				else bigramcounts.increment(encoded_ngram, 1, FERT);
				break;
		}
	}

	public long getCount(int[] ngram) {

		long encoded_ngram = encodeNgram(ngram);
		int order = ngram.length;
		long ret_count;
		if(order != 2)
			ret_count = ngram2count.get(encoded_ngram);
		else
			ret_count = bigramcounts.get(encoded_ngram, COUNT);
		if (ret_count == -1)
			return 0;
		return ret_count;
	}


	public int getFertility(int[] ngram)
	{//fertility of unigrams and bigrams
		int order = ngram.length;
		long encoded_ngram = encodeNgram(ngram);
		int ret_count;
		if(order != 2)
			ret_count = ngram2fert.get(encoded_ngram);
		else
			ret_count = bigramcounts.get(encoded_ngram, FERT);
		if (ret_count == -1)
			return 0;
		return ret_count;
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
		to = to > ngram.length ? ngram.length : to;
		
		int[] sub_ngram = new int[to - from];
		
		for(int i = from; i < to; i++)
		{
			sub_ngram[i - from] = ngram[i];
		}
		//precompute and store the values for unigrams : these are undiscounted ratio of fertilities
		long unigram_fert = getFertility(new int[] {sub_ngram[sub_ngram.length - 1]});

		if(unigram_fert > 0) {
			p_unigram = Math.log(unigram_fert) - l_unique_bigrams;//NOT DISCOUNTED
			retval = p_unigram;
		}
		else {
			//System.out.println(sub_ngram[sub_ngram.length - 1]);
			retval = DEFAULT_PROB;
			if(is_perp) retval = 0;
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
			double numerator = getFertility(sub_bigram);//fertility of w2, w3

			numerator = numerator - discount2 > 0 ? numerator - discount2 : numerator;
			//denominator is sum of fertilities of all bigrams with first word w2 i.e sumv(c'(w2,v))
			
			
			
			int denominator = mwordcounts.get(encodeNgram(new int[]{sub_bigram[0]}), MWORDFC);//getSumFertW2C(sub_ngram);
			if(denominator > 0) {
				//p_bigram = (numerator > discount ? numerator - discount : numerator);
				
				
				// BI_ALPHA_NUM = l_discount + Math.log(mword2fert_count.get(EnglishWordIndexer.getIndexer().get(sub_bigram[0])));
				BI_ALPHA_NUM = l_discount2 + Math.log(mwordcounts.get(encodeNgram(new int[] {sub_bigram[0]}), MWORDC));//getNTrigramsAW2C(sub_ngram));
				p_bigram = SloppyMath.logAdd(Math.log(numerator), (BI_ALPHA_NUM + p_unigram)) - Math.log(denominator);
			}
			else {
				p_bigram = p_unigram;
			}
			
			retval = p_bigram;
		}
		
		if(to - from >= 3)//trigram
		{			
			
			
			long trigram_count = getCount(sub_ngram);
			
			double numerator = 1.0 * trigram_count;
			numerator = numerator - discount3 > 0 ? numerator - discount3 : numerator;
			
			//get the bigram context of the trigram, to compute the denominator
			int [] cxt_bg = new int[2];
			
			cxt_bg[0] = sub_ngram[0];
			cxt_bg[1] = sub_ngram[1];//w1,w2
			
			//common denominator for both count and alpha terms
			long denominator = getCount(new int[] {sub_ngram[0], sub_ngram[1]});

			if(denominator > 0) {//only if the denominator is zero then alpha = 1
				
				long n_trigrams_w1w2 = ntrigramsw1w2.get(encodeNgram(cxt_bg));//number of trigrams starting with w1w2
				TRI_ALPHA_NUM = l_discount3 + Math.log(n_trigrams_w1w2);
				p_trigram = SloppyMath.logAdd(Math.log(numerator), (TRI_ALPHA_NUM + p_bigram)) - Math.log(denominator);
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

	
}
