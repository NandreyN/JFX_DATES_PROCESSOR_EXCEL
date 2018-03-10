package classes;

public class Triplet<L, M, R> {
    private L left;
    private M middle;
    private R right;

    public Triplet(L left, M middle, R right) {
        this.left = left;
        this.middle = middle;
        this.right = right;
    }

    public R getRight() {
        return right;
    }

    public void setRight(R right) {
        this.right = right;
    }

    public M getMiddle() {
        return middle;
    }

    public void setMiddle(M middle) {
        this.middle = middle;
    }

    public L getLeft() {
        return left;
    }

    public void setLeft(L left) {
        this.left = left;
    }
}
