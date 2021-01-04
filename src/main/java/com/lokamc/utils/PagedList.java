package com.lokamc.utils;

import java.util.*;

public class PagedList<T> implements List<T> {
    private final int itemsPerPage;

    public PagedList(List<T> list, int itemsPerPage) {
        addAll(list);
        this.itemsPerPage = itemsPerPage;
    }

    public PagedList(Collection<T> list, int itemsPerPage) {
        addAll(list);
        this.itemsPerPage = itemsPerPage;
    }

    public int getTotalPages() {
        double ceil = Math.ceil((float) size() / (float) itemsPerPage);
        double max = Math.max(1f, ceil);
        return (int) max;
    }

    public List<T> getPage(int page) {
        if (itemsPerPage <= 0 || page <= 0) page = 1;

        int fromIndex = (page - 1) * itemsPerPage;
        if (size() < fromIndex) {
            return Collections.emptyList();
        }

        // toIndex exclusive
        return subList(fromIndex, Math.min(fromIndex + itemsPerPage, size()));
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public Iterator<T> iterator() {
        return null;
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return null;
    }

    @Override
    public boolean add(T t) {
        return false;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return false;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {

    }

    @Override
    public T get(int index) {
        return null;
    }

    @Override
    public T set(int index, T element) {
        return null;
    }

    @Override
    public void add(int index, T element) {

    }

    @Override
    public T remove(int index) {
        return null;
    }

    @Override
    public int indexOf(Object o) {
        return 0;
    }

    @Override
    public int lastIndexOf(Object o) {
        return 0;
    }

    @Override
    public ListIterator<T> listIterator() {
        return null;
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return null;
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return null;
    }
}
