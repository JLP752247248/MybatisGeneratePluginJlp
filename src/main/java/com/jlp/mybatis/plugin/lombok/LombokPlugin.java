package com.jlp.mybatis.plugin.lombok;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.TableConfiguration;

import java.util.List;

/**
 * @description:
 * @DateTime: 2021-06-04 17:30
 * @author: 蒙利幸
 * @version: 1.0
 **/
public class LombokPlugin extends PluginAdapter {

    private final FullyQualifiedJavaType dataAnnotationType = new FullyQualifiedJavaType("lombok.Data");

    private final FullyQualifiedJavaType noArgsConstructorAnnotationType = new FullyQualifiedJavaType("lombok.NoArgsConstructor");

    private final FullyQualifiedJavaType allArgsConstructorAnnotationType = new FullyQualifiedJavaType("lombok.AllArgsConstructor");

    protected PropertyConfig pluginPropertyConfig;

    public boolean validate(List<String> warnings) {
        String addData = properties.getProperty("addData", "true");
        String addNoArgs = properties.getProperty("addNoArgs", "true");
        String addAllArgs = properties.getProperty("addAllArgs", "false");
        pluginPropertyConfig = new PropertyConfig();
        pluginPropertyConfig.setAddData("true".equals(addData));
        pluginPropertyConfig.setAddNoArgs("true".equals(addNoArgs));
        pluginPropertyConfig.setAddAllArgs("true".equals(addAllArgs));
        return true;
    }

    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.addDataAnnotation(topLevelClass, introspectedTable);
        return true;
    }

    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.addDataAnnotation(topLevelClass, introspectedTable);
        return true;
    }

    public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.addDataAnnotation(topLevelClass, introspectedTable);
        return true;
    }

    protected void addDataAnnotation(TopLevelClass topLevelClass) {
        if (pluginPropertyConfig.getAddData()){
            topLevelClass.addImportedType(dataAnnotationType);
            topLevelClass.addAnnotation("@Data");
        }
        if (pluginPropertyConfig.getAddNoArgs()){
            topLevelClass.addImportedType(noArgsConstructorAnnotationType);
            topLevelClass.addAnnotation("@NoArgsConstructor");
        }
        if (pluginPropertyConfig.getAddAllArgs()){
            topLevelClass.addImportedType(allArgsConstructorAnnotationType);
            topLevelClass.addAnnotation("@AllArgsConstructor");
        }
    }

    private void addDataAnnotation(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        String addData = introspectedTable.getTableConfigurationProperty("addData");
        String addNoArgs = introspectedTable.getTableConfigurationProperty("addNoArgs");
        String addAllArgs = introspectedTable.getTableConfigurationProperty("addAllArgs");
        if (addData == null){
            if (pluginPropertyConfig.getAddData()){
                topLevelClass.addImportedType(dataAnnotationType);
                topLevelClass.addAnnotation("@Data");
            }
        }else {
            if ("true".equals(addData)){
                topLevelClass.addImportedType(dataAnnotationType);
                topLevelClass.addAnnotation("@Data");
            }
        }
        if (addNoArgs == null){
            if (pluginPropertyConfig.getAddNoArgs()){
                topLevelClass.addImportedType(noArgsConstructorAnnotationType);
                topLevelClass.addAnnotation("@NoArgsConstructor");
            }
        }else {
            if ("true".equals(addNoArgs)){
                topLevelClass.addImportedType(noArgsConstructorAnnotationType);
                topLevelClass.addAnnotation("@NoArgsConstructor");
            }
        }
        if (addAllArgs == null){
            if (pluginPropertyConfig.getAddAllArgs()){
                topLevelClass.addImportedType(allArgsConstructorAnnotationType);
                topLevelClass.addAnnotation("@AllArgsConstructor");
            }
        }else {
            if ("true".equals(addAllArgs)){
                topLevelClass.addImportedType(allArgsConstructorAnnotationType);
                topLevelClass.addAnnotation("@AllArgsConstructor");
            }
        }

    }


    public boolean modelGetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        String addData = introspectedTable.getTableConfigurationProperty("addData");
        return addData == null ? !pluginPropertyConfig.getAddData() : !("true".equals(addData));
    }

    public boolean modelSetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        String addData = introspectedTable.getTableConfigurationProperty("addData");
        return addData == null ? !pluginPropertyConfig.getAddData() : !("true".equals(addData));
    }

    protected static class PropertyConfig {

        private Boolean addData;

        private Boolean addNoArgs;

        private Boolean addAllArgs;

        public PropertyConfig() {
        }

        public Boolean getAddNoArgs() {
            return addNoArgs;
        }

        public void setAddNoArgs(Boolean addNoArgs) {
            this.addNoArgs = addNoArgs;
        }

        public Boolean getAddAllArgs() {
            return addAllArgs;
        }

        public void setAddAllArgs(Boolean addAllArgs) {
            this.addAllArgs = addAllArgs;
        }

        public Boolean getAddData() {
            return addData;
        }

        public void setAddData(Boolean addData) {
            this.addData = addData;
        }
    }
}
