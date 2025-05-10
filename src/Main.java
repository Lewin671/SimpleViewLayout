import java.util.ArrayList;
import java.util.List;

class MeasureSpec {
    public static final int UNSPECIFIED = 0;
    public static final int EXACTLY = 1;
    public static final int AT_MOST = 2;

    public static int makeMeasureSpec(int size, int mode) {
        return (mode << 30) | (size & 0x3FFFFFFF);
    }

    public static int getMode(int measureSpec) {
        return (measureSpec >> 30) & 0x3;
    }

    public static int getSize(int measureSpec) {
        return measureSpec & 0x3FFFFFFF;
    }
}

class View {
    public static class LayoutParams {
        public static final int MATCH_PARENT = -1;
        public static final int WRAP_CONTENT = -2;

        public int width;
        public int height;

        public LayoutParams(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }

    protected LayoutParams mLayoutParams;
    protected int mMeasuredWidth;
    protected int mMeasuredHeight;
    protected int left, top, right, bottom;
    protected String name = "View";

    public View() {
    }

    public View(String name) {
        this.name = name;
    }

    public void setLayoutParams(LayoutParams params) {
        this.mLayoutParams = params;
    }

    public LayoutParams getLayoutParams() {
        return mLayoutParams;
    }

    public void measure(int widthMeasureSpec, int heightMeasureSpec) {
        onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = 0, height = 0;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (mLayoutParams != null) {
            width = calculateSize(widthMode, widthSize, mLayoutParams.width);
            height = calculateSize(heightMode, heightSize, mLayoutParams.height);
        }

        setMeasuredDimension(width, height);
    }

    private int calculateSize(int mode, int size, int dimension) {
        if (dimension == LayoutParams.MATCH_PARENT) {
            return mode == MeasureSpec.UNSPECIFIED ? 0 : size;
        } else if (dimension == LayoutParams.WRAP_CONTENT) {
            return mode == MeasureSpec.AT_MOST ? Math.min(0, size) : 0;
        } else {
            return Math.min(dimension, mode == MeasureSpec.UNSPECIFIED ? Integer.MAX_VALUE : size);
        }
    }

    protected void setMeasuredDimension(int width, int height) {
        mMeasuredWidth = width;
        mMeasuredHeight = height;
    }

    public int getMeasuredWidth() {
        return mMeasuredWidth;
    }

    public int getMeasuredHeight() {
        return mMeasuredHeight;
    }

    public void layout(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        onLayout(false, left, top, right, bottom);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    }

    public void printTree() {
        printTree(0);
    }

    protected void printTree(int indent) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; i++) sb.append("  ");
        sb.append(toString());
        System.out.println(sb);
    }

    @Override
    public String toString() {
        return name + " measured: " + mMeasuredWidth + "x" + mMeasuredHeight +
                ", layout: [" + left + "," + top + "," + right + "," + bottom + "]";
    }
}

class ViewGroup extends View {
    private final List<View> children = new ArrayList<>();

    public ViewGroup() {
    }

    public ViewGroup(String name) {
        super(name);
    }

    public void addView(View child) {
        children.add(child);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int maxWidth = 0, totalHeight = 0;
        for (View child : children) {
            LayoutParams lp = child.getLayoutParams() != null ?
                    child.getLayoutParams() : new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

            int childWidthSpec = getChildMeasureSpec(widthMeasureSpec, lp.width);
            int childHeightSpec = getChildMeasureSpec(heightMeasureSpec, lp.height);
            child.measure(childWidthSpec, childHeightSpec);

            maxWidth = Math.max(maxWidth, child.getMeasuredWidth());
            totalHeight += child.getMeasuredHeight();
        }

        setMeasuredDimension(
                resolveSize(widthSize, widthMode, maxWidth),
                resolveSize(heightSize, heightMode, totalHeight)
        );
    }

    private int getChildMeasureSpec(int parentSpec, int childDimension) {
        int parentMode = MeasureSpec.getMode(parentSpec);
        int parentSize = MeasureSpec.getSize(parentSpec);

        if (childDimension == LayoutParams.MATCH_PARENT) {
            return MeasureSpec.makeMeasureSpec(parentSize, parentMode == MeasureSpec.UNSPECIFIED ?
                    MeasureSpec.UNSPECIFIED : MeasureSpec.EXACTLY);
        } else if (childDimension == LayoutParams.WRAP_CONTENT) {
            return MeasureSpec.makeMeasureSpec(parentSize, parentMode == MeasureSpec.UNSPECIFIED ?
                    MeasureSpec.UNSPECIFIED : MeasureSpec.AT_MOST);
        } else {
            return MeasureSpec.makeMeasureSpec(childDimension, MeasureSpec.EXACTLY);
        }
    }

    private int resolveSize(int parentSize, int mode, int desiredSize) {
        return switch (mode) {
            case MeasureSpec.EXACTLY -> parentSize;
            case MeasureSpec.AT_MOST -> Math.min(desiredSize, parentSize);
            default -> desiredSize;
        };
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int currentTop = 0;
        for (View child : children) {
            child.layout(0, currentTop, child.getMeasuredWidth(), currentTop + child.getMeasuredHeight());
            currentTop += child.getMeasuredHeight();
        }
    }

    @Override
    protected void printTree(int indent) {
        super.printTree(indent);
        for (View child : children) {
            child.printTree(indent + 1);
        }
    }
}

public class Main {
    public static void main(String[] args) {
        ViewGroup root = new ViewGroup("Root");
        root.setLayoutParams(new View.LayoutParams(400, 600));

        View header = new View("Header");
        header.setLayoutParams(new View.LayoutParams(View.LayoutParams.MATCH_PARENT, 100));

        ViewGroup content = new ViewGroup("Content");
        content.setLayoutParams(new View.LayoutParams(View.LayoutParams.MATCH_PARENT, View.LayoutParams.WRAP_CONTENT));

        View item1 = new View("Item1");
        item1.setLayoutParams(new View.LayoutParams(View.LayoutParams.MATCH_PARENT, 150));

        View item2 = new View("Item2");
        item2.setLayoutParams(new View.LayoutParams(View.LayoutParams.MATCH_PARENT, 200));

        View footer = new View("Footer");
        footer.setLayoutParams(new View.LayoutParams(View.LayoutParams.MATCH_PARENT, 150));

        content.addView(item1);
        content.addView(item2);

        root.addView(header);
        root.addView(content);
        root.addView(footer);

        root.measure(
                MeasureSpec.makeMeasureSpec(1080, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(3000, MeasureSpec.EXACTLY)
        );
        root.layout(0, 0, root.getMeasuredWidth(), root.getMeasuredHeight());
        root.printTree();
    }
}
