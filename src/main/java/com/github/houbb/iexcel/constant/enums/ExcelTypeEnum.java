package com.github.houbb.iexcel.constant.enums;

/**
 * excel 类型常量
 *
 * @author houbinbin
 */
public enum ExcelTypeEnum {
    /**
     * 2003 excel
     */
    XLS(".xls"),

    /**
     * 2007 excel
     */
    XLSX(".xlsx"),
    ;

    /**
     * 值
     */
    private final String value;

    ExcelTypeEnum(String value) {
        this.value = value;
    }


}
