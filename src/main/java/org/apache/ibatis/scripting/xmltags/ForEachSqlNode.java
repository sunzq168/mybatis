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

import org.apache.ibatis.parsing.GenericTokenParser;
import org.apache.ibatis.parsing.TokenHandler;
import org.apache.ibatis.session.Configuration;

/**
 * @author Clinton Begin
 */
public class ForEachSqlNode implements SqlNode {
  public static final String ITEM_PREFIX = "__frch_";
  /**
   * 用于判断循环的终止条件，ForeachSqlNode 构造方法中会创建该对象
   */
  private final ExpressionEvaluator evaluator;
  /**
   * 迭代的集合表达式
   */
  private final String collectionExpression;
  /**
   * 记录了该 ForeachSqlNode 节点的子节点
   */
  private final SqlNode contents;
  /**
   * 在循环开始前要添加的字符串
   */
  private final String open;
  /**
   * 在循环结束后要添加的字符串
   */
  private final String close;
  /**
   * 循环过程中，每项之间的分隔符
   */
  private final String separator;
  /**
   * 本次选代的元素,若迭代集合是 Map，则 index 是键， item 是值
   */
  private final String item;
  /**
   * 当前迭代的次数，
   */
  private final String index;
  private final Configuration configuration;

  public ForEachSqlNode(Configuration configuration, SqlNode contents, String collectionExpression, String index, String item, String open, String close, String separator) {
    this.evaluator = new ExpressionEvaluator();
    this.collectionExpression = collectionExpression;
    this.contents = contents;
    this.open = open;
    this.close = close;
    this.separator = separator;
    this.index = index;
    this.item = item;
    this.configuration = configuration;
  }

  @Override
  public boolean apply(DynamicContext context) {
    //获取参数信息
    Map<String, Object> bindings = context.getBindings();
    //步骤 l:解析集合表达式对应的实际参数
    final Iterable<?> iterable = evaluator.evaluateIterable(collectionExpression, bindings);
    if (!iterable.iterator().hasNext()) {
      return true;
    }
    boolean first = true;
    //步骤2:在循环开始之前，调用 DynamicContext.appendSql()方法添加open指定的字符串
    applyOpen(context);
    int i = 0;
    for (Object o : iterable) {
      // 记录当前 DynamicContext 对象
      DynamicContext oldContext = context;
      //步骤 3:创建 PrefixedContext ，并让 context 指向该 PrefixedContext 对象
      if (first || separator == null) {
        // 如果是集合的第一项或未指定分隔符，则将 PrefixedContext.prefix 初始化为空字符串
        context = new PrefixedContext(context, "");
      } else {
        //如果指定了分隔符，则 PrefixedContext.prefix初始化为指定分隔符
        context = new PrefixedContext(context, separator);
      }
      // uniqueNumber 从 0 开始，每次递增 l，用于转换生成新的“# {}”占位符名称
      int uniqueNumber = context.getUniqueNumber();
      // Issue #709 
      if (o instanceof Map.Entry) {
        //如果集合是Map 类型，将集合中 key 和 value 添加到 DynamicContext.bindings 集合中保存
        Map.Entry<Object, Object> mapEntry = (Map.Entry<Object, Object>) o;
        applyIndex(context, mapEntry.getKey(), uniqueNumber);
        applyItem(context, mapEntry.getValue(), uniqueNumber);
      } else {
        //将集合 中的 索引和元素添加到 DynamicContext.bindings 集合中保存
        applyIndex(context, i, uniqueNumber);
        applyItem(context, o, uniqueNumber);
      }
      //步骤 6: 调用子节点的 apply ()方法进行处理，注意 ，这里使用的 FilteredDynamicContext 对象
      contents.apply(new FilteredDynamicContext(configuration, context, index, item, uniqueNumber));
      if (first) {
        first = !((PrefixedContext) context).isPrefixApplied();
      }
      context = oldContext;
      i++;
    }
    //步骤 7:循环结束后，调用 DynamicContext.appendSql()方法添加 close 指定的字符串
    applyClose(context);
    context.getBindings().remove(item);
    context.getBindings().remove(index);
    return true;
  }

  private void applyIndex(DynamicContext context, Object o, int i) {
    if (index != null) {
      //key 为index, value 是集合元素
      context.bind(index, o);
      // 为 index添加前缀和后缀形成新的 key
      context.bind(itemizeItem(index, i), o);
    }
  }

  private void applyItem(DynamicContext context, Object o, int i) {
    if (item != null) {
      context.bind(item, o);
      context.bind(itemizeItem(item, i), o);
    }
  }

  private void applyOpen(DynamicContext context) {
    if (open != null) {
      context.appendSql(open);
    }
  }

  private void applyClose(DynamicContext context) {
    if (close != null) {
      context.appendSql(close);
    }
  }

  private static String itemizeItem(String item, int i) {
    //添加” frch ”前级和i后缀
    return new StringBuilder(ITEM_PREFIX).append(item).append("_").append(i).toString();
  }

  /**
   * FilteredDynamicContext 负责处理“#{}”占位符 ，但它并未完全解析“#{}”占位符
   */
  private static class FilteredDynamicContext extends DynamicContext {
    private final DynamicContext delegate;
    //对应集合项在集合中的索引位置
    private final int index;
    //对应集合项的index，参见对 ForeachSqlNode.index 字段的介绍
    private final String itemIndex;
    //对应集合项的 item，参见 ForeachSqlNode.item 字段的介绍
    private final String item;

    public FilteredDynamicContext(Configuration configuration,DynamicContext delegate, String itemIndex, String item, int i) {
      super(configuration, null);
      this.delegate = delegate;
      this.index = i;
      this.itemIndex = itemIndex;
      this.item = item;
    }

    @Override
    public Map<String, Object> getBindings() {
      return delegate.getBindings();
    }

    @Override
    public void bind(String name, Object value) {
      delegate.bind(name, value);
    }

    @Override
    public String getSql() {
      return delegate.getSql();
    }

    /**
     * appendSql()方法会将“#{item}”占位符转换成“#{_frch_item_l }” 的格式，其中“ _frch_ ”是固定的前缀，“item”与处理前的占位符一样，未发生改变，
     * l则是 FilteredDynamicContext 产生的单调递增值;还会将“# {itemlndex}”占位符转换成“#{_frch_itemlndex_ l}”的格式
     * @param sql
     */
    @Override
    public void appendSql(String sql) {
      GenericTokenParser parser = new GenericTokenParser("#{", "}", new TokenHandler() {
        @Override
        public String handleToken(String content) {
          String newContent = content.replaceFirst("^\\s*" + item + "(?![^.,:\\s])", itemizeItem(item, index));
          if (itemIndex != null && newContent.equals(content)) {
            newContent = content.replaceFirst("^\\s*" + itemIndex + "(?![^.,:\\s])", itemizeItem(itemIndex, index));
          }
          return new StringBuilder("#{").append(newContent).append("}").toString();
        }
      });

      delegate.appendSql(parser.parse(sql));
    }

    @Override
    public int getUniqueNumber() {
      return delegate.getUniqueNumber();
    }

  }


  private class PrefixedContext extends DynamicContext {
    private final DynamicContext delegate;
    private final String prefix;
    //是否已经处理过前缀
    private boolean prefixApplied;

    public PrefixedContext(DynamicContext delegate, String prefix) {
      super(configuration, null);
      this.delegate = delegate;
      this.prefix = prefix;
      this.prefixApplied = false;
    }

    public boolean isPrefixApplied() {
      return prefixApplied;
    }

    @Override
    public Map<String, Object> getBindings() {
      return delegate.getBindings();
    }

    @Override
    public void bind(String name, Object value) {
      delegate.bind(name, value);
    }

    @Override
    public void appendSql(String sql) {
      if (!prefixApplied && sql != null && sql.trim().length() > 0) {
        //追加前缀
        delegate.appendSql(prefix);
        prefixApplied = true;
      }
      delegate.appendSql(sql);
    }

    @Override
    public String getSql() {
      return delegate.getSql();
    }

    @Override
    public int getUniqueNumber() {
      return delegate.getUniqueNumber();
    }
  }

}
