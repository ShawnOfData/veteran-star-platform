package com.veteran.common;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PageResult<T> extends Result<T> {

    private long total;

    private long page;

    private long size;

    public static <T> PageResult<T> of(T data, long total, long page, long size) {
        PageResult<T> result = new PageResult<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        result.setTotal(total);
        result.setPage(page);
        result.setSize(size);
        return result;
    }
}