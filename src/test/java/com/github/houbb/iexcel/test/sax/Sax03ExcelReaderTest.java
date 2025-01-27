package com.github.houbb.iexcel.test.sax;

import com.github.houbb.heaven.util.nio.PathUtil;
import com.github.houbb.iexcel.core.reader.IExcelReader;
import com.github.houbb.iexcel.sax.Sax03ExcelReader;
import com.github.houbb.iexcel.test.model.ExcelFieldModel;
import org.junit.Test;

import java.io.File;
import java.util.List;

/**
 * @author binbin.hou
 * date 2018/11/19 15:50
 * @since 0.0.1
 */
public class Sax03ExcelReaderTest {

    @Test
    public void readAllTest() {
        final String path = PathUtil.getAppTestResourcesPath()+"/excelWriter03.xls";
        IExcelReader<ExcelFieldModel> excelReader = new Sax03ExcelReader<>(new File(path));
        List<ExcelFieldModel> excelFieldModelList = excelReader.readAll(ExcelFieldModel.class);
        System.out.println(excelFieldModelList);
    }

}
