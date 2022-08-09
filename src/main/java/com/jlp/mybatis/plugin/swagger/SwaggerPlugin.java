package com.jlp.mybatis.plugin.swagger;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaElement;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.util.List;

/**
 * @description:
 * @DateTime: 2021-06-04 16:16
 * @author: 蒙利幸
 * @version: 1.0
 **/
public class SwaggerPlugin extends PluginAdapter {

    private final FullyQualifiedJavaType apiModel = new FullyQualifiedJavaType("io.swagger.annotations.ApiModel");

    private final FullyQualifiedJavaType apiModelProperty = new FullyQualifiedJavaType("io.swagger.annotations.ApiModelProperty");

    public boolean validate(List<String> warnings) {
        return true;
    }

    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.addDataAnnotation(topLevelClass);
        return true;
    }

    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.addDataAnnotation(topLevelClass);
        return true;
    }

    public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.addDataAnnotation(topLevelClass);
        return true;
    }



    protected void addDataAnnotation(TopLevelClass topLevelClass) {
        topLevelClass.addImportedType(apiModel);
        topLevelClass.addImportedType(apiModelProperty);
        topLevelClass.addAnnotation("@ApiModel");
    }

    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        this.comment(field, introspectedTable, introspectedColumn);
        return true;
    }

    private void comment(JavaElement element, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) {
        String javaProperty = introspectedColumn.getJavaProperty();
        String remark = introspectedColumn.getRemarks();
        remark = remark == null ? "" : remark;
        StringBuilder stringBuilder = new StringBuilder("@ApiModelProperty(");
        stringBuilder.append("name=").append("\"").append(javaProperty).append("\"").append(", ");
        stringBuilder.append("notes=").append("\"").append(remark).append("\"");
        stringBuilder.append(")");
        element.addJavaDocLine(stringBuilder.toString());
    }
}
