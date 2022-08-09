package com.jlp.mybatis.plugin.mapper;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @description:
 * @DateTime: 2021-07-28 16:02
 * @author: 蒙利幸
 * @version: 1.0
 **/
public class MapperPlugin extends AbstractMapperPlugin {

    @Override
    public void addSQLXMLElements(XmlElement parentElement, IntrospectedTable introspectedTable) {

        xmlAddSelectByIdCollection(parentElement, introspectedTable);
        xmlAddDeleteByIdCollection(parentElement, introspectedTable);
        xmlAddListSelective(parentElement, introspectedTable);
        xmlAddInsertBatch(parentElement, introspectedTable);
        xmlAddUpdateBatch(parentElement, introspectedTable);
        xmlAddSelectForeignKey(parentElement, introspectedTable);
        xmlAddSelectUniqueKey(parentElement, introspectedTable);
        xmlAddSelectUniqueKeyCollection(parentElement, introspectedTable);
        xmlAddListQuery(parentElement, introspectedTable);
    }

    private void xmlAddListQuery(XmlElement parentElement, IntrospectedTable introspectedTable){

        XmlElement where = new XmlElement("where");
        where.addElement(new TextElement("and true"));

        XmlElement listQuery = new XmlElement("select");
        listQuery.addAttribute(new Attribute("id", "listQuery"));
        listQuery.addAttribute(new Attribute("resultMap", "BaseResultMap"));
        TextElement text3 = new TextElement("select");
        XmlElement baseInclude = new XmlElement("include");
        baseInclude.addAttribute(new Attribute("refid", "Base_Column_List"));
        TextElement text4 = new TextElement("from " + introspectedTable.getFullyQualifiedTableNameAtRuntime());
        listQuery.addElement(text3);
        listQuery.addElement(baseInclude);
        listQuery.addElement(text4);
        listQuery.addElement(where);

        parentElement.addElement(new TextElement("<!-- 未完成的列表查询，待完善 -->"));
        parentElement.addElement(listQuery);
        parentElement.addElement(new TextElement(""));
    }

    private void xmlAddSelectUniqueKeyCollection(XmlElement parentElement, IntrospectedTable introspectedTable){
        String tableName = introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime();
        String uniqueKeysString = introspectedTable.getTableConfigurationProperty("uniqueKeys");
        if (uniqueKeysString != null && uniqueKeysString.matches(uniqueKeysFormat)){
            String[] uniqueKeys = uniqueKeysString.split(";");
            for (String uniqueKey : uniqueKeys){
                String[] keys = uniqueKey.split(",");
                for (String key : keys){
                    IntrospectedColumn column = introspectedTable.getColumn(key);
                    if (column == null){
                        System.out.println(String.format("【%s】表生成【%s】唯一索引查询失败，原因是【%s】字段不存在", tableName, uniqueKey, key));
                        return;
                    }
                }
            }

            for (String uniqueKey : uniqueKeys){
                String[] keys = uniqueKey.split(",");
                List<IntrospectedColumn> keyColumnList = new ArrayList<IntrospectedColumn>();
                for (String key : keys){
                    IntrospectedColumn column = introspectedTable.getColumn(key);
                    keyColumnList.add(column);
                }

                IntrospectedColumn lastColumn = keyColumnList.remove(keyColumnList.size() - 1);
                String lastActualColumnName = lastColumn.getActualColumnName();
                String lastJdbcTypeName = lastColumn.getJdbcTypeName();
                String lastJavaProperty = lastColumn.getJavaProperty();
                String lastJavaPropertyUp = lastJavaProperty.substring(0, 1).toUpperCase() + lastJavaProperty.substring(1);
                String lastJavaPropertyCollection = lastJavaProperty + "Collection";

                XmlElement where = new XmlElement("where");

                XmlElement choose = new XmlElement("choose");

                XmlElement when = new XmlElement("when");
                when.addAttribute(new Attribute("test", lastJavaPropertyCollection + " == null or "+ lastJavaPropertyCollection + ".size == 0"));
                when.addElement(new TextElement("and false"));

                XmlElement otherwise = new XmlElement("otherwise");


                StringBuilder methodName = new StringBuilder("listBy");
                StringBuilder remarkItem = new StringBuilder();
                for (IntrospectedColumn column : keyColumnList){
                    String actualColumnName = column.getActualColumnName();
                    String jdbcTypeName = column.getJdbcTypeName();
                    String javaProperty = column.getJavaProperty();
                    remarkItem.append(javaProperty).append(",");

                    String substring0 = javaProperty.substring(0, 1);
                    String substring1 = javaProperty.substring(1);
                    String javaPropertyUp0 = substring0.toUpperCase() + substring1;
                    methodName.append(javaPropertyUp0);

                    otherwise.addElement(new TextElement("and " + actualColumnName + " = #{" + javaProperty + ",jdbcType=" + jdbcTypeName + "}"));
                }
                remarkItem.append(lastJavaPropertyCollection);
                methodName.append(lastJavaPropertyUp).append("Collection");

                otherwise.addElement(new TextElement("and " + lastActualColumnName + " in "));

                XmlElement foreach = new XmlElement("foreach");
                foreach.addAttribute(new Attribute("collection", lastJavaPropertyCollection));
                foreach.addAttribute(new Attribute("item", lastJavaProperty));
                foreach.addAttribute(new Attribute("open", "("));
                foreach.addAttribute(new Attribute("separator", ","));
                foreach.addAttribute(new Attribute("close", ")"));

                foreach.addElement(new TextElement("#{"+ lastJavaProperty +",jdbcType=" + lastJdbcTypeName + "}"));

                otherwise.addElement(foreach);

                choose.addElement(when);
                choose.addElement(otherwise);

                where.addElement(choose);

                XmlElement method = new XmlElement("select");
                TextElement text3 = new TextElement("select");
                method.addAttribute(new Attribute("id", methodName.toString()));
                method.addAttribute(new Attribute("resultMap", "BaseResultMap"));
                XmlElement baseInclude = new XmlElement("include");
                baseInclude.addAttribute(new Attribute("refid", "Base_Column_List"));
                TextElement text4 = new TextElement("from " + introspectedTable.getFullyQualifiedTableNameAtRuntime());
                method.addElement(text3);
                method.addElement(baseInclude);
                method.addElement(text4);
                method.addElement(where);

                parentElement.addElement(new TextElement("<!-- 唯一索引集合查询：通过" + remarkItem.toString() + "查询 -->"));
                parentElement.addElement(method);
                parentElement.addElement(new TextElement(""));
            }
        }
    }

    private void xmlAddSelectUniqueKey(XmlElement parentElement, IntrospectedTable introspectedTable){
        String tableName = introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime();
        String uniqueKeysString = introspectedTable.getTableConfigurationProperty("uniqueKeys");
        if (uniqueKeysString != null && uniqueKeysString.matches(uniqueKeysFormat)){
            String[] uniqueKeys = uniqueKeysString.split(";");
            for (String uniqueKey : uniqueKeys){
                String[] keys = uniqueKey.split(",");
                for (String key : keys){
                    IntrospectedColumn column = introspectedTable.getColumn(key);
                    if (column == null){
                        System.out.println(String.format("【%s】表生成【%s】唯一索引查询失败，原因是【%s】字段不存在", tableName, uniqueKey, key));
                        return;
                    }
                }
            }

            for (String uniqueKey : uniqueKeys){
                String[] keys = uniqueKey.split(",");
                List<IntrospectedColumn> keyColumnList = new ArrayList<IntrospectedColumn>();
                for (String key : keys){
                    IntrospectedColumn column = introspectedTable.getColumn(key);
                    keyColumnList.add(column);
                    System.out.println(column.getActualColumnName());
                }

                XmlElement where = new XmlElement("where");
                StringBuilder methodName = new StringBuilder("getBy");
                StringBuilder remarkItem = new StringBuilder();
                for (IntrospectedColumn column : keyColumnList){
                    String actualColumnName = column.getActualColumnName();
                    String jdbcTypeName = column.getJdbcTypeName();
                    String javaProperty = column.getJavaProperty();
                    remarkItem.append(javaProperty).append(",");

                    String substring0 = javaProperty.substring(0, 1);
                    String substring1 = javaProperty.substring(1);
                    String javaPropertyUp0 = substring0.toUpperCase() + substring1;
                    methodName.append(javaPropertyUp0);

                    TextElement text = new TextElement("and " + actualColumnName + " = #{" + javaProperty + ",jdbcType=" + jdbcTypeName + "}");
                    where.addElement(text);
                }
                remarkItem.deleteCharAt(remarkItem.length() - 1);

                XmlElement method = new XmlElement("select");
                method.addAttribute(new Attribute("id", methodName.toString()));
                method.addAttribute(new Attribute("resultMap", "BaseResultMap"));
                TextElement text3 = new TextElement("select");
                XmlElement baseInclude = new XmlElement("include");
                baseInclude.addAttribute(new Attribute("refid", "Base_Column_List"));
                TextElement text4 = new TextElement("from " + introspectedTable.getFullyQualifiedTableNameAtRuntime());
                method.addElement(text3);
                method.addElement(baseInclude);
                method.addElement(text4);
                method.addElement(where);

                parentElement.addElement(new TextElement("<!-- 唯一索引查询：通过" + remarkItem.toString() + "查询 -->"));
                parentElement.addElement(method);
                parentElement.addElement(new TextElement(""));

            }
        }
    }

    private void xmlAddSelectForeignKey(XmlElement parentElement, IntrospectedTable introspectedTable){
        String foreignKeyString = introspectedTable.getTableConfigurationProperty("foreignKeys");
        if (foreignKeyString != null && foreignKeyString.matches(foreignKeysFormat)){
            String[] foreignKeys = foreignKeyString.split(",");
            for (String foreignKey : foreignKeys){
                IntrospectedColumn introspectedColumn = introspectedTable.getColumn(foreignKey);
                if (introspectedColumn == null){
                    continue;
                }
                String actualColumnName = introspectedColumn.getActualColumnName();
                String jdbcTypeName = introspectedColumn.getJdbcTypeName();
                String javaProperty = introspectedColumn.getJavaProperty();

                String substring0 = javaProperty.substring(0, 1);
                String substring1 = javaProperty.substring(1);
                String javaPropertyUp0 = substring0.toUpperCase() + substring1;

                String javaPropertyCollection = javaProperty + "Collection";

                // 增加base_query
                XmlElement where = new XmlElement("where");
                XmlElement whereChoose = new XmlElement("choose");
                XmlElement whereChooseWhen = new XmlElement("when");
                whereChooseWhen.addAttribute(new Attribute("test", javaPropertyCollection + " == null or "+ javaPropertyCollection + ".size == 0"));
                TextElement whereChooseWhenText = new TextElement("and false");
                XmlElement whereChooseOtherwise = new XmlElement("otherwise");
                TextElement text1 = new TextElement("and " + actualColumnName + " in ");
                whereChooseOtherwise.addElement(text1);
                XmlElement whereChooseOtherwiseForeach = new XmlElement("foreach");
                whereChooseOtherwiseForeach.addAttribute(new Attribute("collection", javaPropertyCollection));
                whereChooseOtherwiseForeach.addAttribute(new Attribute("item", javaProperty));
                whereChooseOtherwiseForeach.addAttribute(new Attribute("open", "("));
                whereChooseOtherwiseForeach.addAttribute(new Attribute("separator", ","));
                whereChooseOtherwiseForeach.addAttribute(new Attribute("close", ")"));


                TextElement text2 = new TextElement("#{"+ javaProperty +",jdbcType=" + jdbcTypeName + "}");
                whereChooseOtherwiseForeach.addElement(text2);

                whereChooseOtherwise.addElement(whereChooseOtherwiseForeach);
                whereChooseWhen.addElement(whereChooseWhenText);
                whereChoose.addElement(whereChooseWhen);
                whereChoose.addElement(whereChooseOtherwise);
                where.addElement(whereChoose);

                String methodName = "listBy" + javaPropertyUp0 + "Collection";
                XmlElement method = new XmlElement("select");
                method.addAttribute(new Attribute("id", methodName));
                method.addAttribute(new Attribute("resultMap", "BaseResultMap"));
                XmlElement baseInclude = new XmlElement("include");
                baseInclude.addAttribute(new Attribute("refid", "Base_Column_List"));
                TextElement text3 = new TextElement("select");
                TextElement text4 = new TextElement("from " + introspectedTable.getFullyQualifiedTableNameAtRuntime());
                method.addElement(text3);
                method.addElement(baseInclude);
                method.addElement(text4);
                method.addElement(where);

                parentElement.addElement(new TextElement("<!-- 外键集合查询：通过" + javaProperty + "集合查询 -->"));
                parentElement.addElement(method);
                parentElement.addElement(new TextElement(""));

            }
        }
    }


    private void xmlAddUpdateBatch(XmlElement parentElement, IntrospectedTable introspectedTable){
        List<IntrospectedColumn> columnList = introspectedTable.getNonPrimaryKeyColumns();
        List<IntrospectedColumn> keyColumnList = introspectedTable.getPrimaryKeyColumns();

        XmlElement foreach = new XmlElement("foreach");
        foreach.addAttribute(new Attribute("collection", "dataCollection"));
        foreach.addAttribute(new Attribute("item", "data"));
        foreach.addAttribute(new Attribute("separator", ";"));

        TextElement text1 = new TextElement(  "update " + introspectedTable.getFullyQualifiedTableNameAtRuntime());
        XmlElement set = new XmlElement("set");
        for(IntrospectedColumn column : columnList) {
            String actualColumnName = column.getActualColumnName();
            String javaProperty = column.getJavaProperty();
            String jdbcTypeName = column.getJdbcTypeName();

            XmlElement setIf = new XmlElement("if");
            setIf.addAttribute(new Attribute("test",  "data." +javaProperty + " != null"));

            TextElement text = new TextElement(actualColumnName + " = " + "#{data." + javaProperty + ",jdbcType=" + jdbcTypeName + "},");
            setIf.addElement(text);
            set.addElement(setIf);
        }

        StringBuilder stringBuilder = new StringBuilder("where ");
        int size = keyColumnList.size();
        int maxIndex = size - 1;
        for (int i = 0; i < size ; i++){
            IntrospectedColumn column = keyColumnList.get(0);
            String actualColumnName = column.getActualColumnName();
            String javaProperty = column.getJavaProperty();
            String jdbcTypeName = column.getJdbcTypeName();

            if (i != maxIndex){
                stringBuilder.append(actualColumnName).append(" = ").append("#{data.").append(javaProperty).append(",jdbcType=").append(jdbcTypeName).append("} and ");
            }else {
                stringBuilder.append(actualColumnName).append(" = ").append("#{data.").append(javaProperty).append(",jdbcType=").append(jdbcTypeName).append("}");
            }
        }
        TextElement text2 = new TextElement(stringBuilder.toString());

        foreach.addElement(text1);
        foreach.addElement(set);
        foreach.addElement(text2);

        XmlElement updateBatch = new XmlElement("update");
        updateBatch.addAttribute(new Attribute("id", "updateBatch"));

        updateBatch.addElement(foreach);


        parentElement.addElement(new TextElement("<!-- 批量修改 -->"));
        parentElement.addElement(updateBatch);
        parentElement.addElement(new TextElement(""));
    }

    private void xmlAddInsertBatch(XmlElement parentElement, IntrospectedTable introspectedTable){

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

        XmlElement foreach = new XmlElement("foreach");
        foreach.addAttribute(new Attribute("collection", "dataCollection"));
        foreach.addAttribute(new Attribute("item", "data"));
        foreach.addAttribute(new Attribute("separator", ","));

        XmlElement trimData = new XmlElement("trim");
        trimData.addAttribute(new Attribute("prefix", "("));
        trimData.addAttribute(new Attribute("suffix", ")"));
        trimData.addAttribute(new Attribute("suffixOverrides", ","));

        for(IntrospectedColumn column : columnList) {
            String javaProperty = column.getJavaProperty();
            String jdbcTypeName = column.getJdbcTypeName();
            TextElement text = new TextElement("#{data." + javaProperty + ",jdbcType=" + jdbcTypeName + "},");
            trimData.addElement(text);
        }
        foreach.addElement(trimData);

        XmlElement insertBatch = new XmlElement("insert");
        insertBatch.addAttribute(new Attribute("id", "insertBatch"));

        insertBatch.addElement(text1);
        insertBatch.addElement(trimColumn);
        insertBatch.addElement(text2);
        insertBatch.addElement(foreach);

        parentElement.addElement(new TextElement("<!-- 批量插入 -->"));
        parentElement.addElement(insertBatch);
        parentElement.addElement(new TextElement(""));
    }

    private void xmlAddListSelective(XmlElement parentElement, IntrospectedTable introspectedTable){
        XmlElement where = new XmlElement("where");
        List<IntrospectedColumn> columnList = introspectedTable.getAllColumns();
        for(IntrospectedColumn column : columnList) {
            String javaProperty = column.getJavaProperty();
            String actualColumnName = column.getActualColumnName();
            String jdbcTypeName = column.getJdbcTypeName();
            XmlElement whereIf = new XmlElement("if"); //$NON-NLS-1$
            whereIf.addAttribute(new Attribute("test",  javaProperty + " != null"));
            TextElement text = new TextElement("and " + actualColumnName + " = #{" + javaProperty + ",jdbcType=" + jdbcTypeName + "}");
            whereIf.addElement(text);
            where.addElement(whereIf);
        }

        XmlElement listSelective = new XmlElement("select");
        listSelective.addAttribute(new Attribute("id", "listSelective"));
        listSelective.addAttribute(new Attribute("resultMap", "BaseResultMap"));
        TextElement text3 = new TextElement("select");
        XmlElement baseInclude = new XmlElement("include");
        baseInclude.addAttribute(new Attribute("refid", "Base_Column_List"));
        TextElement text4 = new TextElement("from " + introspectedTable.getFullyQualifiedTableNameAtRuntime());
        listSelective.addElement(text3);
        listSelective.addElement(baseInclude);
        listSelective.addElement(text4);
        listSelective.addElement(where);

        parentElement.addElement(new TextElement("<!-- 属性动态查询 -->"));
        parentElement.addElement(listSelective);
        parentElement.addElement(new TextElement(""));
    }

    private void xmlAddDeleteByIdCollection(XmlElement parentElement, IntrospectedTable introspectedTable){
        List<IntrospectedColumn> primaryKeyColumnList = introspectedTable.getPrimaryKeyColumns();
        if (primaryKeyColumnList.size() == 1){
            IntrospectedColumn introspectedColumn = primaryKeyColumnList.get(0);
            String actualColumnName = introspectedColumn.getActualColumnName();
            String jdbcTypeName = introspectedColumn.getJdbcTypeName();
            // 增加base_query
            XmlElement where = new XmlElement("where");
            XmlElement whereChoose = new XmlElement("choose");
            XmlElement whereChooseWhen = new XmlElement("when");
            whereChooseWhen.addAttribute(new Attribute("test", "idCollection == null or idCollection.size == 0"));
            TextElement whereChooseWhenText = new TextElement("and false");
            XmlElement whereChooseOtherwise = new XmlElement("otherwise");
            TextElement text1 = new TextElement("and " + actualColumnName + " in ");
            whereChooseOtherwise.addElement(text1);
            XmlElement whereChooseOtherwiseForeach = new XmlElement("foreach");
            whereChooseOtherwiseForeach.addAttribute(new Attribute("collection", "idCollection"));
            whereChooseOtherwiseForeach.addAttribute(new Attribute("item", "id"));
            whereChooseOtherwiseForeach.addAttribute(new Attribute("open", "("));
            whereChooseOtherwiseForeach.addAttribute(new Attribute("separator", ","));
            whereChooseOtherwiseForeach.addAttribute(new Attribute("close", ")"));


            TextElement text2 = new TextElement("#{id,jdbcType=" + jdbcTypeName + "}");
            whereChooseOtherwiseForeach.addElement(text2);

            whereChooseOtherwise.addElement(whereChooseOtherwiseForeach);
            whereChooseWhen.addElement(whereChooseWhenText);
            whereChoose.addElement(whereChooseWhen);
            whereChoose.addElement(whereChooseOtherwise);
            where.addElement(whereChoose);

            XmlElement deleteByIdCollection = new XmlElement("delete");
            deleteByIdCollection.addAttribute(new Attribute("id", "deleteByIdCollection"));
            TextElement text4 = new TextElement("delete from " + introspectedTable.getFullyQualifiedTableNameAtRuntime());
            deleteByIdCollection.addElement(text4);
            deleteByIdCollection.addElement(where);

            parentElement.addElement(new TextElement("<!-- 通过id集合删除 -->"));
            parentElement.addElement(deleteByIdCollection);
            parentElement.addElement(new TextElement(""));
        }
    }

    private void xmlAddSelectByIdCollection(XmlElement parentElement, IntrospectedTable introspectedTable){
        List<IntrospectedColumn> primaryKeyColumnList = introspectedTable.getPrimaryKeyColumns();
        if (primaryKeyColumnList.size() == 1){
            IntrospectedColumn introspectedColumn = primaryKeyColumnList.get(0);
            String actualColumnName = introspectedColumn.getActualColumnName();
            String jdbcTypeName = introspectedColumn.getJdbcTypeName();
            // 增加base_query
            XmlElement where = new XmlElement("where");
            XmlElement whereChoose = new XmlElement("choose");
            XmlElement whereChooseWhen = new XmlElement("when");
            whereChooseWhen.addAttribute(new Attribute("test", "idCollection == null or idCollection.size == 0"));
            TextElement whereChooseWhenText = new TextElement("and false");
            XmlElement whereChooseOtherwise = new XmlElement("otherwise");
            TextElement text1 = new TextElement("and " + actualColumnName + " in ");
            whereChooseOtherwise.addElement(text1);
            XmlElement whereChooseOtherwiseForeach = new XmlElement("foreach");
            whereChooseOtherwiseForeach.addAttribute(new Attribute("collection", "idCollection"));
            whereChooseOtherwiseForeach.addAttribute(new Attribute("item", "id"));
            whereChooseOtherwiseForeach.addAttribute(new Attribute("open", "("));
            whereChooseOtherwiseForeach.addAttribute(new Attribute("separator", ","));
            whereChooseOtherwiseForeach.addAttribute(new Attribute("close", ")"));


            TextElement text2 = new TextElement("#{id,jdbcType=" + jdbcTypeName + "}");
            whereChooseOtherwiseForeach.addElement(text2);

            whereChooseOtherwise.addElement(whereChooseOtherwiseForeach);
            whereChooseWhen.addElement(whereChooseWhenText);
            whereChoose.addElement(whereChooseWhen);
            whereChoose.addElement(whereChooseOtherwise);
            where.addElement(whereChoose);

            XmlElement selectByIdCollection = new XmlElement("select");
            selectByIdCollection.addAttribute(new Attribute("id", "selectByIdCollection"));
            selectByIdCollection.addAttribute(new Attribute("resultMap", "BaseResultMap"));
            XmlElement baseInclude = new XmlElement("include");
            baseInclude.addAttribute(new Attribute("refid", "Base_Column_List"));
            TextElement text3 = new TextElement("select");
            TextElement text4 = new TextElement("from " + introspectedTable.getFullyQualifiedTableNameAtRuntime());
            selectByIdCollection.addElement(text3);
            selectByIdCollection.addElement(baseInclude);
            selectByIdCollection.addElement(text4);
            selectByIdCollection.addElement(where);

            parentElement.addElement(new TextElement("<!-- 通过id集合查询 -->"));
            parentElement.addElement(selectByIdCollection);
            parentElement.addElement(new TextElement(""));
        }
    }

    @Override
    public void addMapperElements(Interface interfaze, IntrospectedTable introspectedTable) {

        mapperAddSelectByIdCollection(interfaze, introspectedTable);
        mapperAddDeleteByIdCollection(interfaze, introspectedTable);
        mapperAddListSelective(interfaze, introspectedTable);
        mapperAddInsertBatch(interfaze, introspectedTable);
        mapperAddUpdateBatch(interfaze, introspectedTable);
        mapperAddMapperAnnotation(interfaze, introspectedTable);
        mapperAddSelectForeignKey(interfaze, introspectedTable);
        mapperAddSelectUniqueKey(interfaze, introspectedTable);
        mapperAddSelectUniqueKeyCollection(interfaze, introspectedTable);

        mapperAddListQuery(interfaze, introspectedTable);
    }

    private void mapperAddListQuery(Interface interfaze, IntrospectedTable introspectedTable){

        FullyQualifiedJavaType modelType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());

        // 创建import对象：mapper接口的导入包或者导入类型
        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        // 导入实体类
        importedTypes.add(modelType);
        // 导入List
        importedTypes.add(new FullyQualifiedJavaType("java.util.List"));

        // 访问类型：public
        JavaVisibility methodVisibility = JavaVisibility.PUBLIC;

        // 返回类型：List
        FullyQualifiedJavaType returnType = new FullyQualifiedJavaType("List");
        // 设置泛型参数
        returnType.addTypeArgument(modelType);

        // 方法名称
        String methodName = "listQuery";


        // 创建方法对象
        Method method = new Method();
        // 设置访问类型
        method.setVisibility(methodVisibility);
        // 设置返回类型对象
        method.setReturnType(returnType);
        // 设置方法名
        method.setName(methodName);

        // 设置备注
        method.addJavaDocLine("/**");
        method.addJavaDocLine(" *未完成的列表查询，待完善");
        method.addJavaDocLine(" */");

        interfaze.addImportedTypes(importedTypes);
        interfaze.addMethod(method);
    }

    private void mapperAddSelectUniqueKeyCollection(Interface interfaze, IntrospectedTable introspectedTable){
        String tableName = introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime();
        String uniqueKeysString = introspectedTable.getTableConfigurationProperty("uniqueKeys");
        if (uniqueKeysString == null){
            return;
        }
        if (!(uniqueKeysString.matches(uniqueKeysFormat))){
            System.out.println(String.format("【%s】表配置的【uniqueKeys】属性格式错误，插件不生成唯一索引的查询接口", tableName));
            return;
        }

        String[] uniqueKeys = uniqueKeysString.split(";");

        for (String uniqueKey : uniqueKeys){
            String[] keys = uniqueKey.split(",");
            for (String key : keys){
                IntrospectedColumn column = introspectedTable.getColumn(key);
                if (column == null){
                    System.out.println(String.format("【%s】表生成【%s】唯一索引查询失败，原因是【%s】字段不存在", tableName, uniqueKey, key));
                    return;
                }
            }
        }


        // 先创建import对象
        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        // 导入实体类类型
        importedTypes.add(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));
        // 导入List类型
        importedTypes.add(new FullyQualifiedJavaType("java.util.List"));
        // 导入@Param注解
        importedTypes.add(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Param"));

        for (String uniqueKey : uniqueKeys){
            String[] keys = uniqueKey.split(",");
            List<IntrospectedColumn> keyColumnList = new ArrayList<IntrospectedColumn>();
            for (String key : keys){
                IntrospectedColumn column = introspectedTable.getColumn(key);
                keyColumnList.add(column);
            }

            IntrospectedColumn lastColumn = keyColumnList.remove(keyColumnList.size() - 1);
            String lastActualColumnName = lastColumn.getActualColumnName();
            String lastJdbcTypeName = lastColumn.getJdbcTypeName();
            FullyQualifiedJavaType lastJavaType = lastColumn.getFullyQualifiedJavaType();
            String lastJavaProperty = lastColumn.getJavaProperty();
            String lastJavaPropertyUp = lastJavaProperty.substring(0, 1).toUpperCase() + lastJavaProperty.substring(1);
            String lastJavaPropertyCollection = lastJavaProperty + "Collection";

            JavaVisibility methodVisibility = JavaVisibility.PUBLIC;

            // 设置返回类型：实体类类型
            FullyQualifiedJavaType methodReturnType = new FullyQualifiedJavaType("List");
            methodReturnType.addTypeArgument(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));

            StringBuilder methodName = new StringBuilder("listBy");
            StringBuilder remarkItem = new StringBuilder();
            for (IntrospectedColumn column : keyColumnList){
                String javaProperty = column.getJavaProperty();
                remarkItem.append(javaProperty).append(",");

                String javaPropertyUp0 = javaProperty.substring(0, 1).toUpperCase() + javaProperty.substring(1);
                methodName.append(javaPropertyUp0);
            }
            remarkItem.append(lastJavaPropertyCollection);
            methodName.append(lastJavaPropertyUp).append("Collection");

            List<Parameter> methodParameterList = new ArrayList<Parameter>();
            for (IntrospectedColumn column : keyColumnList){

                String javaProperty = column.getJavaProperty();

                FullyQualifiedJavaType javaType = column.getFullyQualifiedJavaType();

                // 参数类型
                FullyQualifiedJavaType methodParameterType = new FullyQualifiedJavaType("@Param(\"" + javaProperty + "\") " + javaType.getShortName());

                // 方法参数
                Parameter methodParameter = new Parameter(methodParameterType, javaProperty);

                methodParameterList.add(methodParameter);

                // 导入参数类型
                importedTypes.add(javaType);
            }
            FullyQualifiedJavaType lastParameterType = new FullyQualifiedJavaType("@Param(\"" + lastJavaPropertyCollection + "\") Collection");
            lastParameterType.addTypeArgument(lastJavaType);
            methodParameterList.add(new Parameter(lastParameterType, lastJavaPropertyCollection));

            Method method = new Method();
            method.setVisibility(methodVisibility);
            method.setReturnType(methodReturnType);
            method.setName(methodName.toString());
            for (Parameter parameter : methodParameterList){
                method.addParameter(parameter);
            }
            method.addJavaDocLine("/**");
            method.addJavaDocLine(" *唯一索引集合查询：通过" + remarkItem.toString() + "查询");
            method.addJavaDocLine(" */");

            interfaze.addImportedTypes(importedTypes);
            interfaze.addMethod(method);
        }
    }

    private void mapperAddSelectUniqueKey(Interface interfaze, IntrospectedTable introspectedTable){
        String tableName = introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime();
        String uniqueKeysString = introspectedTable.getTableConfigurationProperty("uniqueKeys");
        if (uniqueKeysString == null){
            return;
        }
        if (!(uniqueKeysString.matches(uniqueKeysFormat))){
            System.out.println(String.format("【%s】表配置的【uniqueKeys】属性格式错误，插件不生成唯一索引的查询接口", tableName));
            return;
        }

        String[] uniqueKeys = uniqueKeysString.split(";");

        for (String uniqueKey : uniqueKeys){
            String[] keys = uniqueKey.split(",");
            for (String key : keys){
                IntrospectedColumn column = introspectedTable.getColumn(key);
                if (column == null){
                    System.out.println(String.format("【%s】表生成【%s】唯一索引查询失败，原因是【%s】字段不存在", tableName, uniqueKey, key));
                    return;
                }
            }
        }

        // 先创建import对象
        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        // 导入实体类类型
        importedTypes.add(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));
        // 导入List类型
        importedTypes.add(new FullyQualifiedJavaType("java.util.List"));
        // 导入@Param注解
        importedTypes.add(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Param"));

        for (String uniqueKey : uniqueKeys){
            String[] keys = uniqueKey.split(",");
            List<IntrospectedColumn> keyColumnList = new ArrayList<IntrospectedColumn>();
            for (String key : keys){
                IntrospectedColumn column = introspectedTable.getColumn(key);
                keyColumnList.add(column);
            }

            JavaVisibility methodVisibility = JavaVisibility.PUBLIC;

            // 设置返回类型：实体类类型
            FullyQualifiedJavaType methodReturnType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());

            StringBuilder methodName = new StringBuilder("getBy");
            StringBuilder remarkItem = new StringBuilder();
            for (IntrospectedColumn column : keyColumnList){

                String javaProperty = column.getJavaProperty();
                remarkItem.append(javaProperty).append(",");

                String substring0 = javaProperty.substring(0, 1);
                String substring1 = javaProperty.substring(1);
                String javaPropertyUp0 = substring0.toUpperCase() + substring1;
                methodName.append(javaPropertyUp0);
            }
            remarkItem.deleteCharAt(remarkItem.length() - 1);

            List<Parameter> methodParameterList = new ArrayList<Parameter>();
            for (IntrospectedColumn column : keyColumnList){

                String javaProperty = column.getJavaProperty();

                FullyQualifiedJavaType javaType = column.getFullyQualifiedJavaType();

                // 参数类型
                FullyQualifiedJavaType methodParameterType = new FullyQualifiedJavaType("@Param(\"" + javaProperty + "\") " + javaType.getShortName());

                // 方法参数
                Parameter methodParameter = new Parameter(methodParameterType, javaProperty);

                methodParameterList.add(methodParameter);

                // 导入参数类型
                importedTypes.add(javaType);
            }

            Method method = new Method();
            method.setVisibility(methodVisibility);
            method.setReturnType(methodReturnType);
            method.setName(methodName.toString());
            for (Parameter parameter : methodParameterList){
                method.addParameter(parameter);
            }
            method.addJavaDocLine("/**");
            method.addJavaDocLine(" *唯一索引查询：通过" + remarkItem.toString() + "查询");
            method.addJavaDocLine(" */");

            interfaze.addImportedTypes(importedTypes);
            interfaze.addMethod(method);
        }
    }

    private void mapperAddSelectForeignKey(Interface interfaze, IntrospectedTable introspectedTable){

        String foreignKeyString = introspectedTable.getTableConfigurationProperty("foreignKeys");
        if (foreignKeyString == null){
            return;
        }
        if (!(foreignKeyString.matches(foreignKeysFormat))){
            System.out.println(String.format("【%s表】配置foreignKeys属性格式错误：请保证外键字段与数据库字段完全一致，如果有多个字段，不同字段用‘,’隔开", introspectedTable.getFullyQualifiedTableNameAtRuntime()));
            return;
        }

        String[] foreignKeys = foreignKeyString.split(",");

        // 先创建import对象
        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        // 添加Lsit的包
        importedTypes.add(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));
        importedTypes.add(new FullyQualifiedJavaType("java.util.Collection"));
        importedTypes.add(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Param"));

        for (String foreignKey : foreignKeys){
            IntrospectedColumn introspectedColumn = introspectedTable.getColumn(foreignKey);
            if (introspectedColumn == null){
                continue;
            }

            // 添加字段类型
            importedTypes.add(introspectedColumn.getFullyQualifiedJavaType());

            String javaProperty = introspectedColumn.getJavaProperty();

            String substring0 = javaProperty.substring(0, 1);
            String substring1 = javaProperty.substring(1);
            String javaPropertyUp0 = substring0.toUpperCase() + substring1;
            String javaPropertyCollection = javaProperty + "Collection";

            JavaVisibility methodVisibility = JavaVisibility.PUBLIC;

            // 设置返回类型是List
            FullyQualifiedJavaType methodReturnType = new FullyQualifiedJavaType("List");
            methodReturnType.addTypeArgument(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));

            String methodName = "listBy" + javaPropertyUp0 + "Collection";

            // 设置参数类型
            FullyQualifiedJavaType methodParameterType = new FullyQualifiedJavaType("@Param(\"" + javaPropertyCollection + "\") " + "Collection");
            //给参数类型补充泛型
            methodParameterType.addTypeArgument(introspectedColumn.getFullyQualifiedJavaType());
            // 方法参数
            Parameter methodParameter = new Parameter(methodParameterType, javaPropertyCollection);

            Method method = new Method();
            method.setVisibility(methodVisibility);
            method.setReturnType(methodReturnType);
            method.setName(methodName);
            method.addParameter(methodParameter);
            // 设置备注
            method.addJavaDocLine("/**");
            method.addJavaDocLine(" *外键查询：通过" + javaProperty + "集合查询");
            method.addJavaDocLine(" */");

            interfaze.addImportedTypes(importedTypes);
            interfaze.addMethod(method);
        }
    }

    private void mapperAddMapperAnnotation(Interface interfaze, IntrospectedTable introspectedTable){
        // 先创建import对象
        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        importedTypes.add(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Mapper"));
        interfaze.addImportedTypes(importedTypes);
        interfaze.addAnnotation("@Mapper");
    }

    private void mapperAddUpdateBatch(Interface interfaze, IntrospectedTable introspectedTable){
        // 先创建import对象
        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        // 添加Lsit的包
        importedTypes.add(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));
        importedTypes.add(new FullyQualifiedJavaType("java.util.Collection"));
        importedTypes.add(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Param"));

        JavaVisibility methodVisibility = JavaVisibility.PUBLIC;

        // 设置返回类型是List
        FullyQualifiedJavaType methodReturnType = new FullyQualifiedJavaType("int");

        String methodName = "updateBatch";

        // 设置参数类型
        FullyQualifiedJavaType methodParameterType = new FullyQualifiedJavaType("@Param(\"dataCollection\") " + "Collection");
        //给参数类型补充泛型
        methodParameterType.addTypeArgument(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));
        // 方法参数
        Parameter methodParameter = new Parameter(methodParameterType, "dataCollection");

        Method method = new Method();
        method.setVisibility(methodVisibility);
        method.setReturnType(methodReturnType);
        method.setName(methodName);
        method.addParameter(methodParameter);
        // 设置备注
        method.addJavaDocLine("/**");
        method.addJavaDocLine(" *批量修改：数据为空会报错，使用时请判断是否空");
        method.addJavaDocLine(" */");

        interfaze.addImportedTypes(importedTypes);
        interfaze.addMethod(method);
    }

    private void mapperAddInsertBatch(Interface interfaze, IntrospectedTable introspectedTable){
        // 先创建import对象
        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        // 添加Lsit的包
        importedTypes.add(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));
        importedTypes.add(new FullyQualifiedJavaType("java.util.Collection"));
        importedTypes.add(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Param"));

        JavaVisibility methodVisibility = JavaVisibility.PUBLIC;

        // 设置返回类型是List
        FullyQualifiedJavaType methodReturnType = new FullyQualifiedJavaType("int");

        String methodName = "insertBatch";

        // 设置参数类型
        FullyQualifiedJavaType methodParameterType = new FullyQualifiedJavaType("@Param(\"dataCollection\") " + "Collection");
        //给参数类型补充泛型
        methodParameterType.addTypeArgument(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));
        // 方法参数
        Parameter methodParameter = new Parameter(methodParameterType, "dataCollection");

        Method method = new Method();
        method.setVisibility(methodVisibility);
        method.setReturnType(methodReturnType);
        method.setName(methodName);
        method.addParameter(methodParameter);
        // 设置备注
        method.addJavaDocLine("/**");
        method.addJavaDocLine(" *批量插入；数据集合为空会报错，使用时请判断是否空");
        method.addJavaDocLine(" */");

        interfaze.addImportedTypes(importedTypes);
        interfaze.addMethod(method);
    }

    private void mapperAddListSelective(Interface interfaze, IntrospectedTable introspectedTable){

        FullyQualifiedJavaType modelType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());

        // 创建import对象：mapper接口的导入包或者导入类型
        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        // 导入实体类
        importedTypes.add(modelType);
        // 导入List
        importedTypes.add(new FullyQualifiedJavaType("java.util.List"));

        // 访问类型：public
        JavaVisibility methodVisibility = JavaVisibility.PUBLIC;

        // 返回类型：List
        FullyQualifiedJavaType returnType = new FullyQualifiedJavaType("List");
        // 设置泛型参数
        returnType.addTypeArgument(modelType);

        // 方法名称
        String methodName = "listSelective";

        // 设置参数类型是对象
        FullyQualifiedJavaType parameterType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        Parameter parameter = new Parameter(parameterType, "query");

        // 创建方法对象
        Method method = new Method();
        // 设置访问类型
        method.setVisibility(methodVisibility);
        // 设置返回类型对象
        method.setReturnType(returnType);
        // 设置方法名
        method.setName(methodName);
        // 设置方法参数
        method.addParameter(parameter);
        // 设置备注
        method.addJavaDocLine("/**");
        method.addJavaDocLine(" *属性动态查询");
        method.addJavaDocLine(" */");

        interfaze.addImportedTypes(importedTypes);
        interfaze.addMethod(method);
    }

    private void mapperAddDeleteByIdCollection(Interface interfaze, IntrospectedTable introspectedTable) {
        List<IntrospectedColumn> primaryKeyColumnList = introspectedTable.getPrimaryKeyColumns();
        if (primaryKeyColumnList == null || primaryKeyColumnList.size() != 1){
            System.out.println(String.format("【%s表】无法生成deleteByIdCollection方法，原因是CustomPlugin插件只生成单主键表的deleteByIdCollection方法", introspectedTable.getFullyQualifiedTableNameAtRuntime()));
            return;
        }

        FullyQualifiedJavaType primaryKeyType = primaryKeyColumnList.get(0).getFullyQualifiedJavaType();
        FullyQualifiedJavaType modelType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());

        // 创建import对象：mapper接口的导入包或者导入类型
        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        // 导入数据表对应实体类类型
        importedTypes.add(modelType);
        // 导入主键类型
        importedTypes.add(primaryKeyType);
        // 导入Collection
        importedTypes.add(new FullyQualifiedJavaType("java.util.Collection"));
        // 导入@Param注解
        importedTypes.add(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Param"));

        // 访问类型：public
        JavaVisibility methodVisibility = JavaVisibility.PUBLIC;

        // 返回类型：int
        FullyQualifiedJavaType returnType = new FullyQualifiedJavaType("int");

        // 方法名称
        String methodName = "deleteByIdCollection";

        // 参数类型
        FullyQualifiedJavaType parameterType = new FullyQualifiedJavaType("@Param(\"idCollection\") " + "Collection");
        parameterType.addTypeArgument(primaryKeyType);
        Parameter parameter = new Parameter(parameterType, "idCollection");

        // 创建方法对象
        Method method = new Method();
        // 设置访问类型
        method.setVisibility(methodVisibility);
        // 设置返回类型对象
        method.setReturnType(returnType);
        // 设置方法名
        method.setName(methodName);
        // 设置方法参数
        method.addParameter(parameter);
        // 设置备注
        method.addJavaDocLine("/**");
        method.addJavaDocLine(" *通过id集合删除");
        method.addJavaDocLine(" */");

        interfaze.addImportedTypes(importedTypes);
        interfaze.addMethod(method);
    }

    private void mapperAddSelectByIdCollection(Interface interfaze, IntrospectedTable introspectedTable) {
        List<IntrospectedColumn> primaryKeyColumnList = introspectedTable.getPrimaryKeyColumns();
        if (primaryKeyColumnList == null || primaryKeyColumnList.size() != 1){
            System.out.println(String.format("【%s表】无法生成selectByIdCollection方法，原因是CustomPlugin插件只生成单主键表的selectByIdCollection方法", introspectedTable.getFullyQualifiedTableNameAtRuntime()));
            return;
        }

        FullyQualifiedJavaType primaryKeyType = primaryKeyColumnList.get(0).getFullyQualifiedJavaType();

        // 创建import对象：mapper接口的导入包或者导入类型
        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();

        //导入数据表对应实体类类型
        importedTypes.add(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));
        //导入主键类型
        importedTypes.add(primaryKeyType);
        //导入List类型
        importedTypes.add(FullyQualifiedJavaType.getNewListInstance());
        //导入Collection
        importedTypes.add(new FullyQualifiedJavaType("java.util.Collection"));
        //导入@Param注解
        importedTypes.add(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Param"));

        // 方法访问类型为public
        JavaVisibility methodVisibility = JavaVisibility.PUBLIC;

        // 创建返回类型：List
        FullyQualifiedJavaType returnType = FullyQualifiedJavaType.getNewListInstance();
        // 设置返回类型（List）的泛型参数
        returnType.addTypeArgument(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));

        // 方法名称
        String methodName = "selectByIdCollection";

        // 创建参数类型
        FullyQualifiedJavaType parameterType = new FullyQualifiedJavaType("@Param(\"idCollection\") " + "Collection");
        // 为参数类型添加泛型
        parameterType.addTypeArgument(primaryKeyType);
        Parameter parameter = new Parameter(parameterType, "idCollection");

        // 创建方法对象
        Method method = new Method();
        // 设置访问类型
        method.setVisibility(methodVisibility);
        // 设置返回类型对象
        method.setReturnType(returnType);
        // 设置方法名
        method.setName(methodName);
        // 设置方法参数
        method.addParameter(parameter);
        // 设置备注
        method.addJavaDocLine("/**");
        method.addJavaDocLine(" *通过id集合查询");
        method.addJavaDocLine(" */");

        interfaze.addImportedTypes(importedTypes);
        interfaze.addMethod(method);
    }
}
