package net.novucs.ftop.util;

import java.util.AbstractSet;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * An order statistic tree backed by a splay set.
 *
 * @param <E> the type of elements maintained by this set
 */
public class SplaySet<E> extends AbstractSet<E> implements Set<E> {

    private final Comparator<E> comparator;
    private int size = 0;
    private Node<E> root;

    private SplaySet(Comparator<E> comparator) {
        this.comparator = comparator;
    }

    public static <E extends Comparable<E>> SplaySet<E> create() {
        return new SplaySet<E>(Comparator.naturalOrder());
    }

    public static <E> SplaySet<E> create(Comparator<E> comparator) {
        return new SplaySet<>(comparator);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public boolean add(E element) {
        Node<E> previous = null;
        Node<E> current = root;

        while (current != null) {
            current.size++;
            previous = current;

            int comparison = comparator.compare(current.element, element);

            if (comparison < 0) {
                current = current.right;
            } else if (comparison == 0 && element.equals(current.element)) {
                while (previous != null) {
                    previous.size--;
                    previous = previous.parent;
                }
                return false;
            } else {
                current = current.left;
            }
        }

        current = new Node<>(element);
        current.parent = previous;

        if (previous == null) {
            root = current;
        } else if (comparator.compare(previous.element, current.element) < 0) {
            previous.right = current;
        } else {
            previous.left = current;
        }

        splay(current);
        size++;
        return true;
    }

    @Override
    public boolean remove(Object o) {
        @SuppressWarnings("unchecked")
        Node<E> node = find((E) o);
        return node != null && removeNode(node);
    }

    private boolean removeNode(Node<E> node) {
        splay(node);

        Node<E> left = node.left;
        Node<E> right = node.right;
        Node<E> max = null;

        if (left != null) {
            left.parent = null;
            max = maximumSubtree(left);
            splay(max);
            max.size = node.size - 1;
            root = max;
        }

        if (right != null) {
            if (left != null) {
                max.right = right;
            } else {
                root = right;
            }
            right.parent = max;
        }

        if (right == null && left == null) {
            root = null;
        }

        size--;
        return true;
    }

    @Override
    public boolean contains(Object o) {
        @SuppressWarnings("unchecked")
        Node<E> node = find((E) o);
        return node != null;
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<>(this, minimumSubtree(root));
    }

    public Iterator<E> iterator(int index) {
        return new Iterator<>(this, nodeByIndex(index));
    }

    public E getMin() {
        return root == null ? null : minimumSubtree(root).element;
    }

    public E getMax() {
        return root == null ? null : maximumSubtree(root).element;
    }

    private Node<E> find(E element) {
        Node<E> node = root;

        while (node != null) {
            int comparison = comparator.compare(node.element, element);

            if (comparison > 0) {
                node = node.left;
            } else if (comparison == 0 && element.equals(node.element)) {
                return node;
            } else {
                node = node.right;
            }
        }

        return null;
    }

    public E byIndex(int index) {
        return nodeByIndex(index).element;
    }

    public int indexOf(E element) {
        Node<E> node = find(element);

        if (node == null) {
            return -1;
        }

        int rank = node.left == null ? 0 : node.left.size;

        while (node.parent != null) {
            if (node.parent.left != node) {
                rank += node.parent.left == null ? 1 : node.parent.left.size + 1;
            }
            node = node.parent;
        }

        return rank;
    }

    private Node<E> nodeByIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Size: " + size + "; Accessed: " + index);
        }

        Node<E> current = root;

        while (current != null) {
            int length = current.left == null ? 0 : current.left.size;

            if (index == length) {
                return current;
            }

            if (index < length) {
                current = current.left;
            } else {
                current = current.right;
                index -= length + 1;
            }
        }

        throw new IllegalStateException();
    }

    private void rotateLeft(Node<E> node) {
        Node<E> target = node.right;
        int laterSize = 0;

        if (target != null) {
            node.right = target.left;

            if (target.left != null) {
                target.left.parent = node;
                laterSize = target.left.size;
            }

            target.parent = node.parent;
        }

        if (node.parent == null) {
            root = target;
        } else if (node == node.parent.left) {
            node.parent.left = target;
        } else {
            node.parent.right = target;
        }

        if (target != null) {
            target.left = node;
            node.size -= target.size;
            target.size += node.size;
        }

        node.size += laterSize;
        node.parent = target;
    }

    private void rotateRight(Node<E> node) {
        Node<E> target = node.left;
        int laterSize = 0;

        if (target != null) {
            node.left = target.right;

            if (target.right != null) {
                target.right.parent = node;
                laterSize = target.right.size;
            }

            target.parent = node.parent;
        }

        if (node.parent == null) {
            root = target;
        } else if (node == node.parent.left) {
            node.parent.left = target;
        } else {
            node.parent.right = target;
        }

        if (target != null) {
            target.right = node;
            node.size -= target.size;
            target.size += node.size;
        }

        node.size += laterSize;
        node.parent = target;
    }

    private void splay(Node<E> node) {
        if (node == null) {
            return;
        }

        while (node.parent != null) {
            if (node.parent.parent == null) {
                if (node == node.parent.left) {
                    rotateRight(node.parent);
                } else {
                    rotateLeft(node.parent);
                }
                continue;
            }

            boolean nodeLeft = node == node.parent.left;
            boolean nodeRight = node == node.parent.right;
            boolean parentLeft = node.parent == node.parent.parent.left;
            boolean parentRight = node.parent == node.parent.parent.right;

            if (nodeLeft && parentLeft) {
                rotateRight(node.parent.parent);
                rotateRight(node.parent);
            } else if (nodeRight && parentRight) {
                rotateLeft(node.parent.parent);
                rotateLeft(node.parent);
            } else if (nodeLeft && parentRight) {
                rotateRight(node.parent);
                rotateLeft(node.parent);
            } else {
                rotateLeft(node.parent);
                rotateRight(node.parent);
            }
        }
    }

    private Node<E> minimumSubtree(Node<E> node) {
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }

    private Node<E> maximumSubtree(Node<E> node) {
        while (node.right != null) {
            node = node.right;
        }
        return node;
    }

    private static <E> Node<E> successor(Node<E> node) {
        if (node == null) {
            return null;
        }

        if (node.right != null) {
            Node<E> successor = node.right;

            while (successor.left != null) {
                successor = successor.left;
            }

            return successor;
        }

        Node<E> parent = node.parent;
        Node<E> child = node;

        while (parent != null && child == parent.right) {
            child = parent;
            parent = parent.parent;
        }

        return parent;
    }

    private static <E> Node<E> predecessor(Node<E> node) {
        if (node == null) {
            return null;
        }

        if (node.left != null) {
            Node<E> predecessor = node.left;

            while (predecessor.right != null) {
                predecessor = predecessor.right;
            }

            return predecessor;
        }

        Node<E> parent = node.parent;
        Node<E> child = node;

        while (parent != null && child == parent.left) {
            child = parent;
            parent = parent.parent;
        }

        return parent;
    }

    @Override
    public String toString() {
        return "SplaySet{" +
                "comparator=" + comparator +
                ", size=" + size +
                ", root=" + root +
                '}';
    }

    private static class Node<E> {
        private E element;
        private Node<E> parent;
        private Node<E> left;
        private Node<E> right;
        private int size = 1;

        private Node(E element) {
            this.element = element;
        }

        public E getElement() {
            return element;
        }

        public Node<E> getParent() {
            return parent;
        }

        public Node<E> getLeft() {
            return left;
        }

        public Node<E> getRight() {
            return right;
        }

        public int getSize() {
            return size;
        }

        @Override
        public String toString() {
            return "Node{" +
                    "element=" + element +
                    ", left=" + left +
                    ", right=" + right +
                    ", size=" + size +
                    '}';
        }
    }

    public static class Iterator<E> implements TreeIterator<E> {

        private final SplaySet<E> tree;
        private Node<E> next;
        private Node<E> prev;
        private Node<E> current;

        private Iterator(SplaySet<E> tree, Node<E> first) {
            this.tree = tree;
            this.next = first;
            this.prev = first;
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public boolean hasPrevious() {
            return prev != null;
        }

        @Override
        public E next() {
            current = next;

            if (current == null) {
                throw new NoSuchElementException();
            }

            prev = current;
            next = successor(current);
            return current.element;
        }

        @Override
        public E previous() {
            current = prev;

            if (current == null) {
                throw new NoSuchElementException();
            }

            next = current;
            prev = predecessor(current);
            return current.element;
        }

        @Override
        public void remove() {
            if (current == null) {
                throw new IllegalStateException("No element selected to remove");
            }

            tree.removeNode(current);
            current = null;
        }
    }
}
