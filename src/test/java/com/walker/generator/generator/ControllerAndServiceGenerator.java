package com.walker.generator.generator;

import com.google.common.base.CaseFormat;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.walker.generator.common.ProjectConstant.BASE_PACKAGE;
import static com.walker.generator.common.ProjectConstant.SERVICE_IMPL_PACKAGE;
import static com.walker.generator.common.ProjectConstant.SERVICE_PACKAGE;

/**
 * 生成 Controller and Service
 */
public class ControllerAndServiceGenerator {

    private static final String PROJECT_PATH = System.getProperty("user.dir");//项目在硬盘上的基础路径

    private static final String TEMPLATE_FILE_PATH = PROJECT_PATH + "/src/test/resources/template";//模板位置

    private static final String AUTHOR = "CodeGenerator";//@author

    private static final String DATE = new SimpleDateFormat("yyyy-MM-dd").format(new Date());//@date

    private static final String JAVA_PATH = "/src/main/java"; //java文件路径

    private static final String PACKAGE_PATH_SERVICE = packageConvertPath(SERVICE_PACKAGE);//生成的Service存放路径

    private static final String PACKAGE_PATH_SERVICE_IMPL = packageConvertPath(SERVICE_IMPL_PACKAGE);//生成的Service实现存放路径

    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerAndServiceGenerator.class);

    /**
     * freemarker 配置
     * @return
     * @throws IOException
     */
    private static Configuration getConfiguration() throws IOException {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
        configuration.setDirectoryForTemplateLoading(new File(TEMPLATE_FILE_PATH));
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.IGNORE_HANDLER);
        return configuration;
    }

    /**
     * 生成 Controller
     *
     * @param tableName
     * @param modelName
     */
    public static void generateController(String tableName, String modelName) {
        try {
            Configuration configuration = getConfiguration();
            Map<String, Object> params = assembleParamMap(tableName, modelName);
        } catch (Exception e) {
            throw new RuntimeException("生成Controller失败", e);
        }
    }

    /**
     * 生成 Service 和 ServiceImpl
     *
     * @param tableName
     * @param modelName
     */
    public static void generateService(String tableName, String modelName) {
        try {
            Configuration configuration = getConfiguration();
            String modelNameUpperCamel = StringUtils.isBlank(modelName) ? tableNameConvertUpperCamel(tableName) : modelName;
            Map<String, Object> params = assembleParamMap(tableName, modelName);
            File file = new File(PROJECT_PATH + JAVA_PATH + PACKAGE_PATH_SERVICE + modelNameUpperCamel + "Service.java");
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            configuration.getTemplate("service.ftl").process(params, new FileWriter(file));
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            File implFile = new File(PROJECT_PATH + JAVA_PATH + PACKAGE_PATH_SERVICE_IMPL + modelNameUpperCamel + "ServiceImpl.java");
            if (!implFile.getParentFile().exists()) {
                implFile.getParentFile().mkdirs();
            }
            configuration.getTemplate("service-impl.ftl").process(params, new FileWriter(implFile));
        } catch (Exception e) {
            LOGGER.error("");
            e.printStackTrace();
        }
    }

    private static Map<String, Object> assembleParamMap(String tableName, String modelName) {
        Map<String, Object> params = new HashMap<>();
        params.put("date", DATE);
        params.put("author", AUTHOR);
        String modelNameUpperCamel = StringUtils.isBlank(modelName) ? tableNameConvertUpperCamel(tableName) : modelName;
        params.put("baseRequestMapping", modelNameConvertMappingPath(modelNameUpperCamel));
        params.put("basePackage", BASE_PACKAGE);
        params.put("modelNameUpperCamel", modelNameUpperCamel);
        params.put("modelNameLowerCamel", "");
        return params;
    }

    private static String tableNameConvertUpperCamel(String tableName) {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, tableName.toLowerCase());
    }

    private static String tableNameConvertMappingPath(String tableName) {
        tableName = tableName.toLowerCase();//兼容使用大写的表名
        return "/" + (tableName.contains("_") ? tableName.replaceAll("_", "/") : tableName);
    }

    private static String modelNameConvertMappingPath(String modelName) {
        String tableName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, modelName);
        return tableNameConvertMappingPath(tableName);
    }

    private static String packageConvertPath(String packageName) {
        return String.format("/%s/", packageName.contains(".") ? packageName.replaceAll("\\.", "/") : packageName);
    }


    public static void main(String[] args) {
        System.out.println(tableNameConvertUpperCamel("t_user"));
        generateService("t_user", "User");
    }

}