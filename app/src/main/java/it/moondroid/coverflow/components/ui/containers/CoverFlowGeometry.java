package it.moondroid.coverflow.components.ui.containers;

/** Coordinates shared by right-to-left filling and left-to-right relayout. */
public final class CoverFlowGeometry {
    private CoverFlowGeometry() {}

    public static int precedingRight(int right, int width, float spacing) {
        return right - (int) (width * spacing);
    }
}
