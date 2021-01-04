package com.lokamc.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PagedList<T> {
    private final List<T> list;
    private final int itemsPerPage;

    public PagedList(List<T> list, int itemsPerPage) {
        this.list = new ArrayList<>(list);
        this.itemsPerPage = itemsPerPage;
    }

    public PagedList(Collection<T> list, int itemsPerPage) {
        this.list = new ArrayList<>(list);
        this.itemsPerPage = itemsPerPage;
    }

    public List<T> getList() {
        return list;
    }

    public int getTotalPages() {
        double ceil = Math.ceil((float) list.size() / (float) itemsPerPage);
        double max = Math.max(1f, ceil);
        return (int) max;
    }

    public List<T> getPage(int page) {
        if (itemsPerPage <= 0 || page <= 0) page = 1;

        int fromIndex = (page - 1) * itemsPerPage;
        if (list.size() < fromIndex) {
            return Collections.emptyList();
        }

        // toIndex exclusive
        return list.subList(fromIndex, Math.min(fromIndex + itemsPerPage, list.size()));
    }
}
