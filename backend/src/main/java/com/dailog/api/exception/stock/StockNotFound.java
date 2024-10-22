package com.dailog.api.exception.stock;

import com.dailog.api.exception.DailogException;

/**
 * status -> 404
 */
public class StockNotFound extends DailogException {

    private static final String MESSAGE = "검색 결과가 없습니다.";

    public StockNotFound() {
        super(MESSAGE);
    }

    //public PostNotFound(Throwable cause) {
    //    super(MESSAGE, cause);
    //}


    @Override
    public int getStatusCode() {
        return 404;
    }
}
