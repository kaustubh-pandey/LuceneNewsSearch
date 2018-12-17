package lucene_read_file;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.SmallFloat;


public class BM25FSimilarity extends Similarity {
    private final float k1;
    private final float b;

    
    public BM25FSimilarity(float k1, float b) {
        if ((Float.isFinite(k1) == false) || (k1 < 0)) {
            throw new IllegalArgumentException("illegal k1 value: " + k1 + ", must be a non-negative finite value");
        }
        if (Float.isNaN(b) || b < 0 || b > 1) {
            throw new IllegalArgumentException("illegal b value: " + b + ", must be between 0 and 1");
        }
        this.k1 = k1;
        this.b  = b;
    }

    
    public BM25FSimilarity() {
        this(1.2f, 0.75f);
    }

   
    protected float idf(long docFreq, long docCount) {
        return (float) Math.log(1 + (docCount - docFreq + 0.5D)/(docFreq + 0.5D));
    }

    
    
    
    
    
    protected float sloppyFreq(int distance) {
        return 1.0f / (distance + 1);
    }

    
    protected float scorePayload(int doc, int start, int end, BytesRef payload) {
        return 1;
    }

    
    
    
    
    protected float avgFieldLength(CollectionStatistics collectionStats) {
        final long sumTotalTermFreq = collectionStats.sumTotalTermFreq();
        if (sumTotalTermFreq <= 0) {
            return 1f;       
        } else {
            final long docCount = collectionStats.docCount() == -1 ? collectionStats.maxDoc() : collectionStats.docCount();
            return (float) (sumTotalTermFreq / (double) docCount);
        }
    }

    
    protected byte encodeNormValue(float boost, int fieldLength) {
        return SmallFloat.floatToByte315(boost / (float) Math.sqrt(fieldLength));
    }

    
    protected float decodeNormValue(byte b) {
        return NORM_TABLE[b & 0xFF];
    }

    
    protected boolean discountOverlaps = true;

    
    public void setDiscountOverlaps(boolean v) {
        discountOverlaps = v;
    }

    
    public boolean getDiscountOverlaps() {
        return discountOverlaps;
    }

   
    private static final float[] NORM_TABLE = new float[256];

    static {
        for (int i = 1; i < 256; i++) {
            float f = SmallFloat.byte315ToFloat((byte)i);
            NORM_TABLE[i] = 1.0f / (f*f);
        }
        NORM_TABLE[0] = 1.0f / NORM_TABLE[255]; // otherwise inf
    }


    @Override
    public final long computeNorm(FieldInvertState state) {
        final int numTerms = discountOverlaps ? state.getLength() - state.getNumOverlap() : state.getLength();
        return encodeNormValue(state.getBoost(), numTerms);
    }

    
    public Explanation idfExplain(CollectionStatistics collectionStats, TermStatistics termStats) {
        final long df = termStats.docFreq();
        final long docCount = collectionStats.docCount() == -1 ? collectionStats.maxDoc() : collectionStats.docCount();
        final float idf = idf(df, docCount);
        return Explanation.match(idf, "idf(docFreq=" + df + ", docCount=" + docCount + ")");
    }

    
    public Explanation idfExplain(CollectionStatistics collectionStats, TermStatistics termStats[]) {
        final long docCount = collectionStats.docCount() == -1 ? collectionStats.maxDoc() : collectionStats.docCount();
        float idf = 0.0f;
        List<Explanation> details = new ArrayList<>();
        for (final TermStatistics stat : termStats ) {
            final long df = stat.docFreq();
            final float termIdf = idf(df, docCount);
            details.add(Explanation.match(termIdf, "idf(docFreq=" + df + ", docCount=" + docCount + ")"));
            idf += termIdf;
        }
        return Explanation.match(idf, "idf(), sum of:", details);
    }

    @Override
    public final SimWeight computeWeight(CollectionStatistics collectionStats, TermStatistics... termStats) {
        Explanation idf = termStats.length == 1 ? idfExplain(collectionStats, termStats[0]) : idfExplain(collectionStats, termStats);

        float avgdl = avgFieldLength(collectionStats);

        // compute freq-independent part of bm25 equation across all norm values
        float cache[] = new float[256];
        for (int i = 0; i < cache.length; i++) {
            cache[i] = k1 * ((1 - b) + b * decodeNormValue((byte)i) / avgdl);
        }
        return new BM25Stats(collectionStats.field(), idf, avgdl, cache);
    }

    @Override
    public final SimScorer simScorer(SimWeight stats, LeafReaderContext context) throws IOException {
        BM25Stats bm25stats = (BM25Stats) stats;
        return new BM25DocScorer(bm25stats, context.reader().getNormValues(bm25stats.field));
    }

    private class BM25DocScorer extends SimScorer {
        private final BM25Stats stats;
        private final float weightValue; // boost * idf * (k1 + 1)
        private final NumericDocValues norms;
        private final float[] cache;

        BM25DocScorer(BM25Stats stats, NumericDocValues norms) throws IOException {
            this.stats = stats;
            this.weightValue = stats.weight * (k1 + 1) * stats.boost;
            this.cache = stats.cache;
            this.norms = norms;
        }

        @Override
        public float score(int doc, float freq) {
            // if there are no norms, we act as if b=0
            float norm = norms == null ? k1 : cache[(byte)norms.get(doc) & 0xFF];
            return weightValue * freq / (freq + norm);
        }

        @Override
        public Explanation explain(int doc, Explanation freq) {
            return explainScore(doc, freq, stats, norms);
        }

        @Override
        public float computeSlopFactor(int distance) {
            return sloppyFreq(distance);
        }

        @Override
        public float computePayloadFactor(int doc, int start, int end, BytesRef payload) {
            return scorePayload(doc, start, end, payload);
        }
    }

    /** Collection statistics for the BM25 model. */
    private static class BM25Stats extends SimWeight {
        /** BM25's idf */
        private final Explanation idf;
        /** The average document length. */
        private final float avgdl;
        /** query boost */
        private float boost;
        /** weight (idf * boost) */
        private float weight;
        /** field name, for pulling norms */
        private final String field;
        /** precomputed norm[256] with k1 * ((1 - b) + b * dl / avgdl) */
        private final float cache[];

        BM25Stats(String field, Explanation idf, float avgdl, float cache[]) {
            this.field = field;
            this.idf = idf;
            this.avgdl = avgdl;
            this.cache = cache;
            normalize(1f, 1f);
        }

        @Override
        public float getValueForNormalization() {
            // we return a TF-IDF like normalization to be nice, but we don't actually normalize ourselves.
            return weight * weight;
        }

        @Override
        public void normalize(float queryNorm, float boost) {
            // we don't normalize with queryNorm at all, we just capture the top-level boost
            this.weight = idf.getValue();
        }
    }

    private Explanation explainTFNorm(int doc, Explanation freq, BM25Stats stats, NumericDocValues norms) {
        List<Explanation> subs = new ArrayList<>();
        subs.add(freq);
        subs.add(Explanation.match(k1, "parameter k1"));
        if (norms == null) {
            subs.add(Explanation.match(0, "parameter b (norms omitted for field)"));
            return Explanation.match(
                    (freq.getValue() * (k1 + 1)) / (freq.getValue() + k1),
                    "tfNorm, computed from:", subs);
        } else {
            float doclen = decodeNormValue((byte)norms.get(doc));
            subs.add(Explanation.match(b, "parameter b"));
            subs.add(Explanation.match(stats.avgdl, "avgFieldLength"));
            subs.add(Explanation.match(doclen, "fieldLength"));
            return Explanation.match(
                    (freq.getValue() * (k1 + 1)) / (freq.getValue() + k1 * (1 - b + b * doclen/stats.avgdl)),
                    "tfNorm, computed from:", subs);
        }
    }

    private Explanation explainScore(int doc, Explanation freq, BM25Stats stats, NumericDocValues norms) {
        Explanation boostExpl = Explanation.match(stats.boost, "boost");
        List<Explanation> subs = new ArrayList<>();
        if (boostExpl.getValue() != 1.0f)
            subs.add(boostExpl);
        subs.add(stats.idf);
        Explanation tfNormExpl = explainTFNorm(doc, freq, stats, norms);
        subs.add(tfNormExpl);
        return Explanation.match(
                boostExpl.getValue() * stats.idf.getValue() * tfNormExpl.getValue(),
                "score(doc="+doc+",freq="+freq+"), product of:", subs);
    }

    @Override
    public String toString() {
        return "BM25(k1=" + k1 + ",b=" + b + ")";
    }

   
    public final float getK1() {
        return k1;
    }

    
    public final float getB() {
        return b;
    }
}
