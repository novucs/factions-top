package net.novucs.ftop.util;

import java.util.Comparator;

public class SortedSplayTree<E> {

    private final Comparator<E> comparator;
    private int size = 0;
    private Node root;

    private SortedSplayTree(Comparator<E> comparator) {
        this.comparator = comparator;
    }

    public static <E extends Comparable<E>> SortedSplayTree<E> create() {
        return new SortedSplayTree<E>(Comparator.naturalOrder());
    }

    public static <E> SortedSplayTree<E> create(Comparator<E> comparator) {
        return new SortedSplayTree<>(comparator);
    }

    public boolean isEmpty() {
        return root == null;
    }

    public int size() {
        return size;
    }

    public E byIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Size: " + size + "; Accessed: " + index);
        }

        Node current = root;

        while (current != null) {
            int length = current.left == null ? 0 : current.left.size;

            if (index == length) {
                return current.element;
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

    public int indexOf(E element) {
        Node node = find(element);

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

    public void add(E element) {
        Node previous = null;
        Node current = root;

        while (current != null) {
            int comparison = comparator.compare(current.element, element);

            if (comparison == 0) {
                while (current.parent != null) {
                    current = current.parent;
                    current.size--;
                }
                return;
            }

            current.size++;
            previous = current;

            if (comparison < 0) {
                current = current.right;
            } else {
                current = current.left;
            }
        }

        current = new Node(element);
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
    }

    public boolean contains(E element) {
        return find(element) != null;
    }

    public boolean remove(E element) {
        Node node = find(element);

        if (node == null) {
            return false;
        }

        splay(node);

        Node left = node.left;
        Node right = node.right;
        Node max = null;

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

    public Node find(E element) {
        Node node = root;

        while (node != null) {
            if (comparator.compare(node.element, element) < 0) {
                node = node.right;
            } else if (comparator.compare(element, node.element) < 0) {
                node = node.left;
            } else {
                return node;
            }
        }

        return null;
    }

    private void rotateLeft(Node node) {
        Node target = node.right;
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

    private void rotateRight(Node node) {
        Node target = node.left;
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

    public void splay(Node node) {
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

    private Node minimumSubtree(Node node) {
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }

    private Node maximumSubtree(Node node) {
        while (node.right != null) {
            node = node.right;
        }
        return node;
    }

    public class Node {
        private E element;
        private Node parent;
        private Node left;
        private Node right;
        private int size = 1;

        private Node(E element) {
            this.element = element;
        }

        @Override
        public String toString() {
            return "Node{element=" + element + ", size=" + size + '}';
        }
    }
}
