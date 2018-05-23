class MyHashSet {
    private final double multiplier = 2;
    private final double loadFactor = 0.75;

    private int countBuckets = 1;
    private int size = 0;
    private MyLinkedList[] buckets = new MyLinkedList[countBuckets];

    MyHashSet() {
        buckets[0] = new MyLinkedList();
    }

    void add(Object object) {
        if (object != null) {
            if (!contains(object)) {
                buckets[object.hashCode() % countBuckets].add(object);
                size++;
                resize();
            }
        } else {
            System.err.println("Error: Null add");
            System.exit(101);
        }
    }

    boolean remove(Object object) {
        if (object != null) {
            int hash = object.hashCode();
            MyLinkedList currentBucket = buckets[hash % countBuckets];

            if (currentBucket.size() == 0) {
                return false;
            } else {
                if (currentBucket.remove(object)) {
                    size--;
                    return true;
                }
            }
        }
        return false;
    }

    int size() {
        return size;
    }

    void clear() {
        countBuckets = 1;
        size = 0;
        buckets = new MyLinkedList[countBuckets];
        buckets[0] = new MyLinkedList();
    }

    boolean isEmpty() {
        return size == 0;
    }

    boolean contains(Object object) {
        if (object != null) {
            int hash = object.hashCode();
            MyLinkedList currentBucket = buckets[hash % countBuckets];

            if (currentBucket.size() == 0) {
                return false;
            } else {
                return currentBucket.contains(object);
            }
        }
        return false;
    }

    /*@Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }*/

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("[");
        for (int i = 0; i < buckets.length - 1; i++) {
            sb.append(buckets[i].toString());
            sb.append(", ");
        }
        sb.append(buckets[buckets.length - 1].toString());
        sb.append("]");
        return sb.toString();
    }

    private void resize() {
        if (countKOverflow() > loadFactor) {
            countBuckets *= multiplier;
            MyLinkedList[] tmp = buckets.clone();
            buckets = new MyLinkedList[countBuckets];
            for (int i = 0; i < buckets.length; i++) {
                buckets[i] = new MyLinkedList();
            }
            for (MyLinkedList tm : tmp) {
                for (int i = 0; i < tm.size(); i++) {
                    Object obj = tm.get(i);

                    buckets[obj.hashCode() % countBuckets].add(obj);
                }
            }
        }
    }

    private double countKOverflow() {
        double kOverflow = 0;
        for (MyLinkedList bucket : buckets) {
            if (bucket.size() > 1) {
                kOverflow++;
            }
        }
        return kOverflow /= countBuckets;
    }
}