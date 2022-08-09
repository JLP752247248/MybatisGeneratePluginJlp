package com.jlp.mybatis.plugin.mapper;

import com.jlp.mybatis.plugin.service.AbstractServicePlugin;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @description:
 * @DateTime: 2021-07-28 15:55
 * @author: 蒙利幸
 * @version: 1.0
 **/
public abstract class AbstractMapperPlugin extends PluginAdapter {

    protected String foreignKeysFormat = "([A-Za-z_][A-Za-z0-9_]{0,})(,[A-Za-z_][A-Za-z0-9_]{0,}){0,}";

    protected String uniqueKeysFormat = "(([A-Za-z_][A-Za-z0-9_]{0,})(,[A-Za-z_][A-Za-z0-9_]{0,}){0,})(;([A-Za-z_][A-Za-z0-9_]{0,})(,[A-Za-z_][A-Za-z0-9_]{0,}){0,}){0,}";

    protected MapperPropertyConfig mapperPropertyConfig;

    @Override
    public boolean validate(List<String> warnings) {
        String deleteInsertSelective = properties.getProperty("deleteInsertSelective", "true");
        String deleteUpdateByPrimaryKey = properties.getProperty("deleteUpdateByPrimaryKey", "true");
        String updateBaseColumnList = properties.getProperty("updateBaseColumnList", "true");
        String addRelevanceSelectColumnList = properties.getProperty("addRelevanceSelectColumnList", "true");

        mapperPropertyConfig= new MapperPropertyConfig();

        mapperPropertyConfig.setDeleteInsertSelective("true".equals(deleteInsertSelective));
        mapperPropertyConfig.setDeleteUpdateByPrimaryKey("true".equals(deleteUpdateByPrimaryKey));
        mapperPropertyConfig.setUpdateBaseColumnList("true".equals(updateBaseColumnList));
        mapperPropertyConfig.setAddRelevanceSelectColumnList("true".equals(addRelevanceSelectColumnList));

        return true;
    }

    public abstract void addSQLXMLElements(XmlElement parentElement, IntrospectedTable introspectedTable);

    public abstract  void addMapperElements(Interface interfaze, IntrospectedTable introspectedTable);

    // 添加xml元素
    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {

        XmlElement rootElement = document.getRootElement();

        if (mapperPropertyConfig.getAddRelevanceSelectColumnList()){
            System.out.println("11111111111111");
            xmlAddRelevanceSelectColumnList(rootElement, introspectedTable);
        }

        addSQLXMLElements(rootElement, introspectedTable);

        rootElement.addElement(new TextElement(""));
        rootElement.addElement(new TextElement("<!-- 以上代码由MbgCode自动生成" + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date()) + " -->\n\n\n"));
        rootElement.addElement(new TextElement("<!-- 以下为您的代码 -->"));
        rootElement.addElement(new TextElement(""));

        return super.sqlMapDocumentGenerated(document, introspectedTable);

    }



    // 添加mapper方法
    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        addMapperElements(interfaze, introspectedTable);
        return super.clientGenerated(interfaze, topLevelClass, introspectedTable);
    }

    // --------------------不生成insertSelective与updateByPrimaryKey---------------------

    @Override
    public boolean sqlMapInsertSelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return !mapperPropertyConfig.getDeleteInsertSelective();
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return !mapperPropertyConfig.getDeleteUpdateByPrimaryKey();
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return !mapperPropertyConfig.getDeleteUpdateByPrimaryKey();
    }

    @Override
    public boolean clientInsertSelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return !mapperPropertyConfig.getDeleteInsertSelective();
    }

    @Override
    public boolean clientInsertSelectiveMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return !mapperPropertyConfig.getDeleteInsertSelective();
    }

    @Override
    public boolean clientUpdateByPrimaryKeyWithBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return !mapperPropertyConfig.getDeleteUpdateByPrimaryKey();
    }

    @Override
    public boolean clientUpdateByPrimaryKeyWithBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return !mapperPropertyConfig.getDeleteUpdateByPrimaryKey();
    }

    @Override
    public boolean clientUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return !mapperPropertyConfig.getDeleteUpdateByPrimaryKey();
    }

    @Override
    public boolean clientUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return !mapperPropertyConfig.getDeleteUpdateByPrimaryKey();
    }


    // -------------------修改Base_Column_List与insert--------------------

    @Override
    public boolean sqlMapBaseColumnListElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        if (mapperPropertyConfig.getUpdateBaseColumnList()){
            List<Element> elements = element.getElements();
            elements.clear();

            List<IntrospectedColumn> columnList = introspectedTable.getAllColumns();

            // --------------
            XmlElement trimColumn = new XmlElement("trim");
            trimColumn.addAttribute(new Attribute("suffixOverrides", ","));

            for(IntrospectedColumn column : columnList) {
                String actualColumnName = column.getActualColumnName();
                TextElement text = new TextElement(actualColumnName + ",");
                trimColumn.addElement(text);
            }

            element.addElement(trimColumn);
        }
        return true;
    }

    @Override
    public boolean sqlMapInsertElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {

        if (mapperPropertyConfig.getUpdateBaseColumnList()){
            // 清除所有元素
            List<Element> elements = element.getElements();
            elements.clear();

            // 获取所有字段
            List<IntrospectedColumn> columnList = introspectedTable.getAllColumns();

            TextElement text1 = new TextElement("insert into " + introspectedTable.getFullyQualifiedTableNameAtRuntime());

            XmlElement trimColumn = new XmlElement("trim");
            trimColumn.addAttribute(new Attribute("prefix", "("));
            trimColumn.addAttribute(new Attribute("suffix", ")"));
            trimColumn.addAttribute(new Attribute("suffixOverrides", ","));

            for(IntrospectedColumn column : columnList) {
                String actualColumnName = column.getActualColumnName();
                TextElement text = new TextElement(actualColumnName + ",");
                trimColumn.addElement(text);
            }

            TextElement text2 = new TextElement("values");

            XmlElement trimData = new XmlElement("trim");
            trimData.addAttribute(new Attribute("prefix", "("));
            trimData.addAttribute(new Attribute("suffix", ")"));
            trimData.addAttribute(new Attribute("suffixOverrides", ","));

            for(IntrospectedColumn column : columnList) {
                String javaProperty = column.getJavaProperty();
                String jdbcTypeName = column.getJdbcTypeName();
                TextElement text = new TextElement("#{" + javaProperty + ",jdbcType=" + jdbcTypeName + "},");
                trimData.addElement(text);
            }

            element.addElement(text1);
            element.addElement(trimColumn);
            element.addElement(text2);
            element.addElement(trimData);
        }

        return true;
    }


    private void xmlAddRelevanceSelectColumnList(XmlElement parentElement, IntrospectedTable introspectedTable){
        String property = introspectedTable.getTableConfigurationProperty("sqlBase");
        if (property == null){
            return;
        }
        if ("".equals(property)){
            System.out.println("请正确填写sqlBase属性");
            return;
        }
        property = property.toLowerCase();
        String substring0 = property.substring(0, 1);
        String substring1 = property.substring(1);
        String propertyUp0 = substring0.toUpperCase() + substring1;

        //---------------

        XmlElement resultMap = new XmlElement("resultMap");
        resultMap.addAttribute(new Attribute("id", propertyUp0 + "ResultMap"));
        resultMap.addAttribute(new Attribute("type", introspectedTable.getBaseRecordType()));

        List<IntrospectedColumn> keyColumns = introspectedTable.getPrimaryKeyColumns();
        List<IntrospectedColumn> nonPrimaryKeyColumns = introspectedTable.getNonPrimaryKeyColumns();
        for(IntrospectedColumn column : keyColumns) {
            String actualColumnName = column.getActualColumnName();
            String javaProperty = column.getJavaProperty();
            String jdbcTypeName = column.getJdbcTypeName();
            XmlElement id = new XmlElement("id");
            id.addAttribute(new Attribute("column", property + "_" + actualColumnName));
            id.addAttribute(new Attribute("jdbcType", jdbcTypeName));
            id.addAttribute(new Attribute("property", javaProperty));
            resultMap.addElement(id);
        }
        for(IntrospectedColumn column : nonPrimaryKeyColumns) {
            String actualColumnName = column.getActualColumnName();
            String javaProperty = column.getJavaProperty();
            String jdbcTypeName = column.getJdbcTypeName();
            XmlElement result = new XmlElement("result");
            result.addAttribute(new Attribute("column", property + "_" + actualColumnName));
            result.addAttribute(new Attribute("jdbcType", jdbcTypeName));
            result.addAttribute(new Attribute("property", javaProperty));
            resultMap.addElement(result);
        }

        // --------------
        XmlElement sql = new XmlElement("sql");
        sql.addAttribute(new Attribute("id", propertyUp0 + "_" + propertyUp0 + "_Column_List"));

        List<IntrospectedColumn> columnList = introspectedTable.getAllColumns();

        XmlElement trimColumn = new XmlElement("trim");
        trimColumn.addAttribute(new Attribute("suffixOverrides", ","));

        for(IntrospectedColumn column : columnList) {
            String actualColumnName = column.getActualColumnName();
            TextElement text = new TextElement(property + "." + actualColumnName + " as " + property + "_" + actualColumnName + ",");
            trimColumn.addElement(text);
        }

        sql.addElement(trimColumn);

        // --------------

        XmlElement sql1 = new XmlElement("sql");
        sql1.addAttribute(new Attribute("id", propertyUp0 + "_Base_Column_List"));

        XmlElement trimColumn1 = new XmlElement("trim");
        trimColumn1.addAttribute(new Attribute("suffixOverrides", ","));

        for(IntrospectedColumn column : columnList) {
            String actualColumnName = column.getActualColumnName();
            TextElement text = new TextElement(property + "." + actualColumnName + " as " + actualColumnName + ",");
            trimColumn1.addElement(text);
        }

        sql1.addElement(trimColumn1);

        // --------------

        parentElement.addElement(2, new TextElement("<!-- 表联查用的结果集映射 -->"));
        parentElement.addElement(3, resultMap);
        parentElement.addElement(4, new TextElement(""));

        parentElement.addElement(5, new TextElement("<!-- 表联查用的SQL属性列表，as关键字左右两边都有配置文件中配置的前缀，与【表联查用的结果集映射】配合使用 -->"));
        parentElement.addElement(6, sql);
        parentElement.addElement(7, new TextElement(""));

        parentElement.addElement(8, new TextElement("<!-- 表联查用的SQL属性列表，as关键字左边有配置文件中配置的前缀，与【BaseResultMap】配合使用 -->"));
        parentElement.addElement(9, sql1);
        parentElement.addElement(10, new TextElement(""));
    }



    protected static class MapperPropertyConfig{

        private Boolean deleteInsertSelective;

        private Boolean deleteUpdateByPrimaryKey;

        private Boolean updateBaseColumnList;

        private Boolean addRelevanceSelectColumnList;

        public MapperPropertyConfig() {
        }

        public Boolean getDeleteInsertSelective() {
            return deleteInsertSelective;
        }

        public void setDeleteInsertSelective(Boolean deleteInsertSelective) {
            this.deleteInsertSelective = deleteInsertSelective;
        }

        public Boolean getDeleteUpdateByPrimaryKey() {
            return deleteUpdateByPrimaryKey;
        }

        public void setDeleteUpdateByPrimaryKey(Boolean deleteUpdateByPrimaryKey) {
            this.deleteUpdateByPrimaryKey = deleteUpdateByPrimaryKey;
        }

        public Boolean getUpdateBaseColumnList() {
            return updateBaseColumnList;
        }

        public void setUpdateBaseColumnList(Boolean updateBaseColumnList) {
            this.updateBaseColumnList = updateBaseColumnList;
        }

        public Boolean getAddRelevanceSelectColumnList() {
            return addRelevanceSelectColumnList;
        }

        public void setAddRelevanceSelectColumnList(Boolean addRelevanceSelectColumnList) {
            this.addRelevanceSelectColumnList = addRelevanceSelectColumnList;
        }
    }

}
