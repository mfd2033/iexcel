package com.github.houbb.iexcel.test.core;

import com.github.houbb.iexcel.constant.enums.ExcelTypeEnum;
import com.github.houbb.iexcel.core.writer.IExcelWriter;
import com.github.houbb.iexcel.test.model.ExcelFieldModel;
import com.github.houbb.iexcel.util.excel.ExcelUtil;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * @author binbin.hou
 * @date 2018/11/14 21:01
 */
public class ExcelWriterTest {

    @Test
    public void onceWriteAndFlushTest() {
        final String path = "D:\\github\\iexcel\\src\\test\\java\\com\\github\\houbb\\iexcel\\test\\3.xlsx";
        List<ExcelFieldModel> modelList = new ArrayList();
        ExcelFieldModel indexModel = new ExcelFieldModel();
        indexModel.setName("你好");
        indexModel.setAge("10");
        indexModel.setAddress("地址");
        indexModel.setEmail("1@qq.com");
        modelList.add(indexModel);

        ExcelUtil.onceWriteAndFlush(modelList, path);
    }

    @Test
    public void listBeanTest() throws FileNotFoundException {
        final String path = "D:\\github\\iexcel\\src\\test\\java\\com\\github\\houbb\\iexcel\\test\\1.xlsx";
        List<ExcelFieldModel> modelList = new ArrayList();
        ExcelFieldModel indexModel = new ExcelFieldModel();
        indexModel.setName("你好");
        indexModel.setAge("10");
        indexModel.setAddress("地址");
        indexModel.setEmail("1@qq.com");
        modelList.add(indexModel);

        try(OutputStream outputStream = new FileOutputStream(path);
            IExcelWriter writer = ExcelUtil.getExcelWriter(ExcelTypeEnum.XLSX);) {
            writer.write(modelList);
            writer.flush(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void mapListTest() {
        final String path = "D:\\github\\iexcel\\src\\test\\java\\com\\github\\houbb\\iexcel\\test\\2.xls";

        Map<String, Object> map = new HashMap<>();
        map.put("name", "名字");
        map.put("ADDRESS", "外滩38号");

        try(OutputStream outputStream = new FileOutputStream(path);
            IExcelWriter writer = ExcelUtil.getExcelWriter(ExcelTypeEnum.XLS);) {
            writer.write(Arrays.asList(map), ExcelFieldModel.class);
            writer.flush(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}