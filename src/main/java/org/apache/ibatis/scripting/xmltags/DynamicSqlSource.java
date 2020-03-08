/**
 *    Copyright 2009-2019 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.scripting.xmltags;

import java.util.Map;

import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;

/**
 * DynamicSqlSource负责处理动态 SQL语句，最终会将处理后的 SQL 语句封装成 StaticSqlSource 返回。
 * DynamicSqlSource 与 StaticSqlSource 的主要区别是 : StaticSqlSource 中记录的SQL语句中可能含有“?”占位符，但是可以直接提交给数据库执行;
 * DynamicSqlSource 中封装的 SQL 语句还需要进行一系列解析，才会最终形成数据库可执行的 SQL 语句
 * @author Clinton Begin
 */
public class DynamicSqlSource implements SqlSource {

  private final Configuration configuration;
  private final SqlNode rootSqlNode;

  public DynamicSqlSource(Configuration configuration, SqlNode rootSqlNode) {
    this.configuration = configuration;
    this.rootSqlNode = rootSqlNode;
  }

  @Override
  public BoundSql getBoundSql(Object parameterObject) {
    // 创建 DynamicContext 对象， parameterObject 是用户传入的实参
    DynamicContext context = new DynamicContext(configuration, parameterObject);
    //通过调用 rootSqlNode.apply()方法调用整个树形结构中全部 SqlNode.apply()方法，读者可以体会一下组合设计模式的好处 。
    // 每个 SqlNode 的 apply()方法都将解析得到的 SQL 语句片段追加到context中， 最终通过context.getSql()得到完整的SQL语句
    rootSqlNode.apply(context);
    //创建 SqlSourceBuilder，解析参数属性，并将 SQL语句中的”#{ )”占位符替换成”?”占位符
    SqlSourceBuilder sqlSourceParser = new SqlSourceBuilder(configuration);
    Class<?> parameterType = parameterObject == null ? Object.class : parameterObject.getClass();
    SqlSource sqlSource = sqlSourceParser.parse(context.getSql(), parameterType, context.getBindings());
    //创建 BoundSql 对象 ，并将 DynamicContext.bindings 中的参数信息复制到其additionalParameters 集合 中保存
    BoundSql boundSql = sqlSource.getBoundSql(parameterObject);
    for (Map.Entry<String, Object> entry : context.getBindings().entrySet()) {
      boundSql.setAdditionalParameter(entry.getKey(), entry.getValue());
    }
    return boundSql;
  }

}
