import java.util.ArrayList;
import java.util.List;

class MeasureSpec {
    public static final int UNSPECIFIED = 0;
    public static final int EXACTLY = 1;
    public static final int AT_MOST = 2;

    public static int makeMeasureSpec(int size, int mode) {
        if (mode != UNSPECIFIED && mode != EXACTLY && mode != AT_MOST) {
            throw new IllegalArgumentException("Mode must be UNSPECIFIED, EXACTLY, or AT_MOST");
        }
        return (mode << 30) | (size & 0x3FFFFFFF);
    }

    public static int getMode(int measureSpec) {
        return (measureSpec >> 30) & 0x3;
    }

    public static int getSize(int measureSpec) {
        return measureSpec & 0x3FFFFFFF;
    }

    public static String toString(int measureSpec) {
        int mode = getMode(measureSpec);
        int size = getSize(measureSpec);
        String modeStr = switch (mode) {
            case UNSPECIFIED -> "UNSPECIFIED";
            case EXACTLY -> "EXACTLY";
            case AT_MOST -> "AT_MOST";
            default -> "UNKNOWN_MODE";
        };
        return "MeasureSpec: [" + size + ", " + modeStr + "]";
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

    // 假设基础 View 的 WRAP_CONTENT 尺寸为0，或者可以设置为最小尺寸
    protected int mDefaultMinWidth = 0;
    protected int mDefaultMinHeight = 0;


    public View() {
        this.name = "View"; // Default name if not specified
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
        // System.out.println(name + " received measure specs: Width=" + MeasureSpec.toString(widthMeasureSpec) + ", Height=" + MeasureSpec.toString(heightMeasureSpec));
        onMeasure(widthMeasureSpec, heightMeasureSpec);
        // System.out.println(name + " after onMeasure: " + mMeasuredWidth + "x" + mMeasuredHeight);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 确保 LayoutParams 存在，如果不存在，则使用默认的 WRAP_CONTENT
        // 这在实际 Android 中通常由 ViewGroup 的 generateDefaultLayoutParams() 保证
        if (mLayoutParams == null) {
            mLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        }

        int measuredWidth = getResolvedSize(mDefaultMinWidth, mLayoutParams.width, widthMeasureSpec);
        int measuredHeight = getResolvedSize(mDefaultMinHeight, mLayoutParams.height, heightMeasureSpec);

        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    /**
     * 辅助方法，根据 LayoutParams 和 MeasureSpec 决定最终尺寸。
     *
     * @param defaultContentSize 当 LayoutParams 为 WRAP_CONTENT 时的期望内容尺寸
     * @param lpDimension        LayoutParams 中指定的尺寸 (width 或 height)
     * @param measureSpec        来自父容器的 MeasureSpec
     * @return 计算得到的最终尺寸
     */
    protected int getResolvedSize(int defaultContentSize, int lpDimension, int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        int resultSize;

        switch (lpDimension) {
            case LayoutParams.MATCH_PARENT:
                if (specMode == MeasureSpec.UNSPECIFIED) {
                    // 父容器没有指定大小，MATCH_PARENT 表现得像 WRAP_CONTENT
                    // 对于基础 View，我们假设其内容大小为 defaultContentSize (通常是0或最小尺寸)
                    resultSize = defaultContentSize;
                } else {
                    // 父容器有确切大小或最大限制，MATCH_PARENT 就取这个大小
                    resultSize = specSize;
                }
                break;
            case LayoutParams.WRAP_CONTENT:
                if (specMode == MeasureSpec.EXACTLY) {
                    // 父容器要求固定大小，即使是 WRAP_CONTENT 也必须服从
                    resultSize = specSize;
                } else if (specMode == MeasureSpec.AT_MOST) {
                    // 父容器给出最大限制，WRAP_CONTENT 取内容大小和限制中的较小者
                    resultSize = Math.min(defaultContentSize, specSize);
                } else { // MeasureSpec.UNSPECIFIED
                    // 父容器不限制，WRAP_CONTENT 取内容大小
                    resultSize = defaultContentSize;
                }
                break;
            default: // 固定尺寸 (e.g., 100px)
                if (specMode == MeasureSpec.EXACTLY) {
                    // 父容器要求固定大小，即使 View 自己有固定尺寸，也必须服从父容器
                    resultSize = specSize;
                } else if (specMode == MeasureSpec.AT_MOST) {
                    // 父容器给出最大限制，View 取自己固定尺寸和限制中的较小者
                    resultSize = Math.min(lpDimension, specSize);
                } else { // MeasureSpec.UNSPECIFIED
                    // 父容器不限制，View 取自己的固定尺寸
                    resultSize = lpDimension;
                }
                break;
        }
        return resultSize;
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
        // Base View does nothing in onLayout
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
        String lpWidthStr = mLayoutParams != null ?
                (mLayoutParams.width == LayoutParams.MATCH_PARENT ? "MP" :
                        (mLayoutParams.width == LayoutParams.WRAP_CONTENT ? "WC" : String.valueOf(mLayoutParams.width)))
                : "N/A";
        String lpHeightStr = mLayoutParams != null ?
                (mLayoutParams.height == LayoutParams.MATCH_PARENT ? "MP" :
                        (mLayoutParams.height == LayoutParams.WRAP_CONTENT ? "WC" : String.valueOf(mLayoutParams.height)))
                : "N/A";

        return name + " (LP: " + lpWidthStr + "x" + lpHeightStr +
                ") measured: " + mMeasuredWidth + "x" + mMeasuredHeight +
                ", layout: [" + left + "," + top + "," + right + "," + bottom + "]";
    }
}

class ViewGroup extends View {
    private final List<View> children = new ArrayList<>();

    public ViewGroup() {
        super(); // Call View's default constructor
    }

    public ViewGroup(String name) {
        super(name);
    }

    public void addView(View child) {
        if (child.getLayoutParams() == null) {
            // System.out.println("Warning: Child " + child.name + " added to " + this.name + " without LayoutParams. Using default WCxWC.");
            child.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        }
        children.add(child);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // System.out.println(name + " (ViewGroup) received measure specs: Width=" + MeasureSpec.toString(widthMeasureSpec) + ", Height=" + MeasureSpec.toString(heightMeasureSpec));

        // ViewGroup 自身的 LayoutParams 由其父容器在 getChildMeasureSpec 时使用。
        // ViewGroup 的 onMeasure 目标是根据其子View和自身的 MeasureSpec 来确定自己的尺寸。

        int maxWidth = 0; // 对于垂直布局，这是子View中最宽的宽度
        int totalHeight = 0; // 对于垂直布局，这是所有子View高度的总和

        // 遍历所有子View，测量它们
        for (View child : children) {
            LayoutParams lp = child.getLayoutParams(); // addView 确保了 lp 不为 null

            int childWidthSpec = getChildMeasureSpec(widthMeasureSpec, lp.width);
            int childHeightSpec = getChildMeasureSpec(heightMeasureSpec, lp.height);

            child.measure(childWidthSpec, childHeightSpec);

            maxWidth = Math.max(maxWidth, child.getMeasuredWidth());
            totalHeight += child.getMeasuredHeight();
        }

        // 考虑 ViewGroup 自身的 padding (如果实现的话)
        // maxWidth += getPaddingLeft() + getPaddingRight();
        // totalHeight += getPaddingTop() + getPaddingBottom();

        // 根据子View的尺寸和ViewGroup自身的MeasureSpec来确定ViewGroup的最终尺寸
        // 对于ViewGroup，其“内容期望尺寸”就是maxWidth和totalHeight
        // 注意：这里 resolveSize 的 desiredSize 是基于子元素计算的，而不是 ViewGroup 自身的 LayoutParams
        setMeasuredDimension(
                resolveSizeInternal(maxWidth, widthMeasureSpec),
                resolveSizeInternal(totalHeight, heightMeasureSpec)
        );
        // System.out.println(name + " (ViewGroup) after onMeasure: " + mMeasuredWidth + "x" + mMeasuredHeight);
    }

    /**
     * 根据父容器的MeasureSpec和子View的LayoutParams属性，计算子View的MeasureSpec。
     */
    private int getChildMeasureSpec(int parentSpec, int childDimension) {
        int parentMode = MeasureSpec.getMode(parentSpec);
        int parentSize = MeasureSpec.getSize(parentSpec);

        int childSpecSize = 0;
        int childSpecMode = 0;

        switch (childDimension) {
            case LayoutParams.MATCH_PARENT:
                // 子View想要和父容器一样大
                if (parentMode == MeasureSpec.EXACTLY || parentMode == MeasureSpec.AT_MOST) {
                    // 父容器有确定的大小或最大限制，子View就取这个大小，并且模式为EXACTLY
                    childSpecSize = parentSize;
                    childSpecMode = MeasureSpec.EXACTLY;
                } else { // parentMode == MeasureSpec.UNSPECIFIED
                    // 父容器没有限制，子View也无法确定大小，所以也是UNSPECIFIED，大小通常为0或内容大小
                    childSpecSize = 0; // Or some default minimum size if MATCH_PARENT in UNSPECIFIED parent
                    childSpecMode = MeasureSpec.UNSPECIFIED;
                }
                break;
            case LayoutParams.WRAP_CONTENT:
                // 子View想要根据内容自适应大小
                if (parentMode == MeasureSpec.EXACTLY || parentMode == MeasureSpec.AT_MOST) {
                    // 父容器有确定的大小或最大限制，子View可以最大达到这个限制，模式为AT_MOST
                    childSpecSize = parentSize;
                    childSpecMode = MeasureSpec.AT_MOST;
                } else { // parentMode == MeasureSpec.UNSPECIFIED
                    // 父容器没有限制，子View也可以是任意大小，模式为UNSPECIFIED
                    childSpecSize = 0; // Or some very large number, as there's no upper bound from parent
                    childSpecMode = MeasureSpec.UNSPECIFIED;
                }
                break;
            default: // childDimension is a fixed size (e.g., 100px)
                // 子View想要一个固定的大小
                // 父容器总是优先，但子View的期望是EXACTLY这个固定大小
                childSpecSize = childDimension;
                childSpecMode = MeasureSpec.EXACTLY;
                break;
        }
        return MeasureSpec.makeMeasureSpec(childSpecSize, childSpecMode);
    }

    /**
     * ViewGroup 使用此方法根据其计算出的期望内容尺寸和父容器传入的 MeasureSpec 来确定自己的最终尺寸。
     */
    private int resolveSizeInternal(int desiredSize, int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        int result;

        switch (specMode) {
            case MeasureSpec.EXACTLY:
                result = specSize; // 父容器强制使用其指定的大小
                break;
            case MeasureSpec.AT_MOST:
                result = Math.min(desiredSize, specSize); // 不能超过父容器指定的最大值
                break;
            case MeasureSpec.UNSPECIFIED:
            default:
                result = desiredSize; // 父容器没有限制，使用期望的大小
                break;
        }
        return result;
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int currentTop = 0; // 假设是垂直线性布局, 忽略 padding
        // int currentTop = getPaddingTop();
        // int parentLeft = getPaddingLeft();

        for (View child : children) {
            // 对于简单的垂直线性布局
            // 假设子View水平居中或靠左，这里简单地靠左
            child.layout(0, currentTop, child.getMeasuredWidth(), currentTop + child.getMeasuredHeight());
            // child.layout(parentLeft, currentTop, parentLeft + child.getMeasuredWidth(), currentTop + child.getMeasuredHeight());
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
        // root 的 LayoutParams 只有在它被添加到另一个 ViewGroup 时才由那个父 ViewGroup 使用。
        // 对于顶层 View，它的尺寸由传递给 measure 方法的 MeasureSpec 决定。
        // 我们仍然可以设置它，但它不会直接影响 root.measure() 的结果，除非 measure spec 是 UNSPECIFIED
        // 并且 root 内部逻辑考虑了自身的 LayoutParams (通常不会直接这么做，而是由父级考虑)。
        root.setLayoutParams(new View.LayoutParams(View.LayoutParams.MATCH_PARENT, View.LayoutParams.MATCH_PARENT)); // 举例，实际影响不大

        View header = new View("Header");
        header.setLayoutParams(new View.LayoutParams(View.LayoutParams.MATCH_PARENT, 100));

        ViewGroup content = new ViewGroup("Content");
        content.setLayoutParams(new View.LayoutParams(View.LayoutParams.MATCH_PARENT, View.LayoutParams.WRAP_CONTENT));

        View item1 = new View("Item1");
        item1.setLayoutParams(new View.LayoutParams(View.LayoutParams.MATCH_PARENT, 150));
        // item1.mDefaultMinHeight = 150; // 如果希望 WRAP_CONTENT 时 item1 高度是 150

        View item2 = new View("Item2");
        item2.setLayoutParams(new View.LayoutParams(View.LayoutParams.MATCH_PARENT, 200));
        // item2.mDefaultMinHeight = 200;

        View footer = new View("Footer");
        footer.setLayoutParams(new View.LayoutParams(View.LayoutParams.MATCH_PARENT, 150));

        content.addView(item1);
        content.addView(item2);

        root.addView(header);
        root.addView(content);
        root.addView(footer);

        // 模拟一个屏幕尺寸
        int screenWidth = 1080;
        int screenHeight = 1920; // 一个较高的屏幕，允许内容滚动

        // 顶层 View 通常接收 EXACTLY 的 MeasureSpec，代表屏幕或可用区域的大小
        // 或者，如果根布局允许滚动，高度可以是 AT_MOST
        System.out.println("Measuring Root with EXACTLY width and AT_MOST height (simulating ScrollView parent):");
        root.measure(
                MeasureSpec.makeMeasureSpec(screenWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(screenHeight, MeasureSpec.AT_MOST) // 允许根视图高度小于屏幕高度 (如果内容少)
        );
        root.layout(0, 0, root.getMeasuredWidth(), root.getMeasuredHeight());
        root.printTree();

        System.out.println("\nMeasuring Root with EXACTLY width and EXACTLY height (simulating fixed size parent):");
        root.measure(
                MeasureSpec.makeMeasureSpec(screenWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(600, MeasureSpec.EXACTLY) // 强制根视图高度为600
        );
        root.layout(0, 0, root.getMeasuredWidth(), root.getMeasuredHeight());
        root.printTree();


        System.out.println("\nMeasuring Root with WRAP_CONTENT like behavior (UNSPECIFIED specs):");
        // 当父容器对子View的尺寸没有限制时 (UNSPECIFIED)，子View可以自由决定其大小
        // 这对于测试 MATCH_PARENT 在 UNSPECIFIED 父容器中的行为很有用
        // Root (ViewGroup) 会根据其子内容来决定大小
        root.measure(
                MeasureSpec.makeMeasureSpec(screenWidth, MeasureSpec.UNSPECIFIED), // 宽度不限 (但子View的MP会如何表现?)
                MeasureSpec.makeMeasureSpec(screenHeight, MeasureSpec.UNSPECIFIED) // 高度不限
        );
        // 对于顶层View，布局通常基于其测量尺寸
        root.layout(0, 0, root.getMeasuredWidth(), root.getMeasuredHeight());
        root.printTree();

        // 测试一个非常小的 EXACTLY 约束
        System.out.println("\nMeasuring Root with very small EXACTLY constraint:");
        root.measure(
                MeasureSpec.makeMeasureSpec(300, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(250, MeasureSpec.EXACTLY)
        );
        root.layout(0, 0, root.getMeasuredWidth(), root.getMeasuredHeight());
        root.printTree();
    }
}