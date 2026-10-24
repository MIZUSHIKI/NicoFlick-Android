package it.moondroid.coverflow.components.ui.containers;

/** Stateless back-to-front permutation: left side, reversed right side, center last. */
public final class CoverFlowDrawingOrder {
    private CoverFlowDrawingOrder() {}

    public static int[] create(int childCount, int centerIndex) {
        if (childCount < 0) throw new IllegalArgumentException("Negative child count");
        int[] order = new int[childCount];
        if (childCount == 0) return order;
        int center = Math.max(0, Math.min(centerIndex, childCount - 1));
        for (int position = 0; position < childCount; position++) {
            order[position] = position < center ? position : childCount - 1 - (position - center);
        }
        return order;
    }
}
