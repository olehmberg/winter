package de.metanome.algorithms.tane;

import it.unimi.dsi.fastutil.longs.LongBigArrayBigList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectBigArrayBigList;

public class StrippedPartition {
    private double error;
    private long elementCount;
    private ObjectBigArrayBigList<LongBigArrayBigList> strippedPartition = null;

    /**
     * Create a StrippedPartition with only one equivalence class with the definied number of elements. <br/>
     * Tuple ids start with 0 to numberOfElements-1
     *
     * @param numberTuples
     */
    public StrippedPartition(long numberTuples) {
        this.strippedPartition = new ObjectBigArrayBigList<LongBigArrayBigList>();
        this.elementCount = numberTuples;
        // StrippedPartition only contains partition with more than one elements.
        if (numberTuples > 1) {
            LongBigArrayBigList newEqClass = new LongBigArrayBigList();
            for (int i = 0; i < numberTuples; i++) {
                newEqClass.add(i);
            }
            this.strippedPartition.add(newEqClass);
        }
        this.calculateError();
    }

    /**
     * Create a StrippedPartition from a HashMap mapping the values to the tuple ids.
     *
     * @param partition
     */
    public StrippedPartition(Object2ObjectOpenHashMap<Object, LongBigArrayBigList> partition) {
        this.strippedPartition = new ObjectBigArrayBigList<LongBigArrayBigList>();
        this.elementCount = 0;

        //create stripped partitions -> only use equivalence classes with size > 1.
        for (LongBigArrayBigList eqClass : partition.values()) {
            if (eqClass.size64() > 1) {
                strippedPartition.add(eqClass);
                elementCount += eqClass.size64();
            }
        }
        this.calculateError();
    }

    public StrippedPartition(ObjectBigArrayBigList<LongBigArrayBigList> sp, long elementCount) {
        this.strippedPartition = sp;
        this.elementCount = elementCount;
        this.calculateError();

    }

    public double getError() {
        return error;
    }

    public ObjectBigArrayBigList<LongBigArrayBigList> getStrippedPartition() {
        return this.strippedPartition;
    }

    private void calculateError() {
        // calculating the error. Dividing by the number of entries
        // in the whole population is not necessary.
        this.error = this.elementCount - this.strippedPartition.size64();
    }

    public void empty() {
        this.strippedPartition = new ObjectBigArrayBigList<LongBigArrayBigList>();
        this.elementCount = 0;
        this.error = 0.0;
    }
}
