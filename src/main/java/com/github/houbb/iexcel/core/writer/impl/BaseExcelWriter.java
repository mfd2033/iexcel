package com.github.houbb.iexcel.core.writer.impl;


import com.github.houbb.iexcel.annotation.ExcelField;
import com.github.houbb.iexcel.constant.SheetConst;
import com.github.houbb.iexcel.core.writer.IExcelWriter;
import com.github.houbb.iexcel.exception.ExcelRuntimeException;
import com.github.houbb.iexcel.support.style.StyleSet;
import com.github.houbb.iexcel.util.BeanUtil;
import com.github.houbb.iexcel.util.StrUtil;
import com.github.houbb.iexcel.util.excel.ExcelCheckUtil;
import com.github.houbb.iexcel.util.excel.RowUtil;
import org.apache.commons.collections4.MapUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基础 excel writer 类
 * @author binbin.hou
 * @date 2018/11/14 13:56
 */
public abstract class BaseExcelWriter implements IExcelWriter {

    /**
     * 当前行
     */
    private AtomicInteger currentRow = new AtomicInteger(0);

    /**
     * 是否被关闭
     */
    private volatile boolean isClosed;

    /**
     * 表头别名信息 map
     */
    private Map<String, String> headerAliasMap;

    /**
     * 样式集，定义不同类型数据样式
     */
    private StyleSet styleSet;

    /**
     * 工作簿
     */
    private Workbook workbook;

    /**
     * Excel中对应的Sheet
     */
    private Sheet sheet;


    public BaseExcelWriter() {
        this(null);
    }

    public BaseExcelWriter(final String sheetName) {
        this.workbook = getWorkbook();
        final String realSheetName = StrUtil.isBlank(sheetName)
                ? SheetConst.DEFAULT_SHEET_NAME : sheetName;
        this.sheet = workbook.createSheet(realSheetName);
        this.styleSet = new StyleSet(workbook);
    }

    /**
     * 获取 workbook
     * @return workbook
     */
    protected abstract Workbook getWorkbook();

    /**
     * 获取最大行数限制
     * @return 最大行数限制
     */
    protected abstract int getMaxRowNumLimit();

    private void checkRowNum(int dataSize) {
        int limit = getMaxRowNumLimit();
        if(dataSize > limit) {
            throw new ExcelRuntimeException("超出最大行数限制");
        }
    }

    @Override
    public IExcelWriter write(Collection<?> data) {
        checkClosedStatus();
        checkRowNum(data.size());

        int index = 0;
        for (Object object : data) {
            if(index == 0) {
                initHeaderAlias(object);
                ExcelCheckUtil.checkColumnNum(headerAliasMap.size());
                writeHeadRow(headerAliasMap.values());
            }

            // 转换为 iter
            Iterable<?> values = buildRowValues(object);
            writeRow(values);
            index++;
        }
        return this;
    }

    @Override
    public IExcelWriter write(Collection<Map<String, Object>> mapList, Class<?> targetClass) {
        List<?> objectList = convertMap2List(mapList, targetClass);
        return write(objectList);
    }

    /**
     * map 转换为数据库查询结果列表
     *
     * @param results 查询结果
     * @param clazz   对象信息
     * @return 结果
     */
    private List<Object> convertMap2List(Iterable<Map<String, Object>> results, final Class<?> clazz) {
        try {
            Field[] fields = clazz.getDeclaredFields();
            List<Object> resultList = new ArrayList<>();

            for (Map<String, Object> result : results) {
                Object pojo = clazz.newInstance();
                for (Field field : fields) {
                    field.setAccessible(true);
                    String fieldName = getFieldName(field);
                    Object fieldValue = result.get(fieldName);
                    // 对于基本类型默认值的处理 避免报错
                    if(fieldValue != null) {
                        field.set(pojo, fieldValue);
                    }
                }
                resultList.add(pojo);
            }

            return resultList;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ExcelRuntimeException(e);
        }
    }

    /**
     * 获取字段名称
     *
     * @param field 字段
     * @return 字段名称
     */
    private String getFieldName(final Field field) {
        final String fieldName = field.getName();
        if (field.isAnnotationPresent(ExcelField.class)) {
            ExcelField column = field.getAnnotation(ExcelField.class);
            final String mapKey = column.mapKey();
            if (StrUtil.isNotBlank(mapKey)) {
                return mapKey;
            }
            return fieldName;
        }
        return fieldName;
    }

    /**
     * 根据表头的顺序构建对应信息。
     * @param object 对象信息
     * @return 构建后的列表信息
     */
    private Iterable<?> buildRowValues(final Object object) {
        Map beanMap = BeanUtil.beanToMap(object);
        List<Object> valueList = new ArrayList<>();
        for(String fieldName : headerAliasMap.keySet()) {
            Object value = beanMap.get(fieldName);
            valueList.add(value);
        }
        return valueList;
    }

    @Override
    public IExcelWriter flush(OutputStream outputStream) {
        try {
            this.workbook.write(outputStream);
            return this;
        } catch (IOException e) {
            throw new ExcelRuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        // 清空对象
        this.headerAliasMap = null;
        this.currentRow = null;
        this.isClosed = true;

        this.styleSet = null;
        this.workbook = null;
        this.sheet = null;
    }

    /**
     * 写 excel 表头
     * 获取 head 真正的信息
     * 如果没有，则视为没有字段需要写入。
     */
    private void writeHeadRow(final Iterable<?> headRowData) {
        RowUtil.writeRow(this.sheet.createRow(this.currentRow.getAndIncrement()),
                headRowData, this.styleSet,
                true);
    }

    /**
     * 写出一行数据<br>
     * 本方法只是将数据写入Workbook中的Sheet，并不写出到文件<br>
     *
     * @param rowData 一行的数据
     */
    private void writeRow(final Iterable<?> rowData) {
        RowUtil.writeRow(this.sheet.createRow(this.currentRow.getAndIncrement()),
                rowData, this.styleSet,
                false);
    }

    /**
     * 初始化标题别名
     */
    private void initHeaderAlias(final Object object) {
        if(!BeanUtil.isBean(object.getClass())) {
            throw new ExcelRuntimeException("列表必须为 java Bean 对象列表");
        }

        // 一定要指定为有序的 Map
        headerAliasMap = new LinkedHashMap<>();
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(ExcelField.class)) {
                ExcelField excel = field.getAnnotation(ExcelField.class);
                final String fieldName = field.getName();
                String headName = excel.headName();
                headName = StrUtil.isNotBlank(headName) ? headName : fieldName;
                if (excel.excelRequire()) {
                    headerAliasMap.put(fieldName, headName);
                }
            }
        }

        if(MapUtils.isEmpty(headerAliasMap)) {
            throw new ExcelRuntimeException("excel 表头信息为空");
        }
    }

    /**
     * 检查关闭的状态信息
     */
    private void checkClosedStatus() {
        if(this.isClosed) {
            throw new ExcelRuntimeException("ExcelWriter has been closed!");
        }
    }

}