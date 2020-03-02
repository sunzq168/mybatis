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
package org.apache.ibatis.reflection;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.binding.MapperMethod.ParamMap;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

public class ParamNameResolver {

  private static final String GENERIC_NAME_PREFIX = "param";

  /**
   * <p>
   * The key is the index and the value is the name of the parameter.<br />
   * The name is obtained from {@link Param} if specified. When {@link Param} is not specified,
   * the parameter index is used. Note that this index could be different from the actual index
   * when the method has special parameters (i.e. {@link RowBounds} or {@link ResultHandler}).
   * </p>
   * <ul>
   * <li>aMethod(@Param("M") int a, @Param("N") int b) -&gt; {{0, "M"}, {1, "N"}}</li>
   * <li>aMethod(int a, int b) -&gt; {{0, "0"}, {1, "1"}}</li>
   * <li>aMethod(int a, RowBounds rb, int b) -&gt; {{0, "0"}, {2, "1"}}</li>
   * </ul>
   * 参数在参数列表中的位置索引与参数名称之间的对应关系
   */
  private final SortedMap<Integer, String> names;
  /**
   * 记录对应方法的参数列表中是否使用了@Param 注解。
   */
  private boolean hasParamAnnotation;

  public ParamNameResolver(Configuration config, Method method) {
    //获取参数列表中每个参数的类型
    final Class<?>[] paramTypes = method.getParameterTypes();
    //获取参数列表上的注解
    final Annotation[][] paramAnnotations = method.getParameterAnnotations();
    //该集合用于记录参数索引与参数名称的对应关系
    final SortedMap<Integer, String> map = new TreeMap<Integer, String>();
    int paramCount = paramAnnotations.length;
    // get names from @Param annotations
    for (int paramIndex = 0; paramIndex < paramCount; paramIndex++) {
      if (isSpecialParameter(paramTypes[paramIndex])) {
        //如果参数是 RowBounds 类型或 ResultHandler 类型，则跳过对该参数的分析
        // skip special parameters
        continue;
      }
      String name = null;
      // 遍历该参数对应的注解集合
      for (Annotation annotation : paramAnnotations[paramIndex]) {
        if (annotation instanceof Param) {
          hasParamAnnotation = true;
          //获取@Param注解指定的参数名称
          name = ((Param) annotation).value();
          break;
        }
      }
      //这个 if 代码段解释了上面的示例中 names 集合项的 value 为什么是 ”0”和”1”
      if (name == null) {
        // @Param was not specified.
        //该参数没有对应的@Param 注解，则根据配置决定是否使用参数实际名称作为其名称
        if (config.isUseActualParamName()) {
          name = getActualParamName(method, paramIndex);
        }
        if (name == null) {
          // use the parameter index as the name ("0", "1", ...)
          // gcode issue #71
          //使用参数的索引作为其名称
          name = String.valueOf(map.size());
        }
      }
      map.put(paramIndex, name);
    }
    names = Collections.unmodifiableSortedMap(map);
  }

  private String getActualParamName(Method method, int paramIndex) {
    if (Jdk.parameterExists) {
      return ParamNameUtil.getParamNames(method).get(paramIndex);
    }
    return null;
  }

  private static boolean isSpecialParameter(Class<?> clazz) {
    return RowBounds.class.isAssignableFrom(clazz) || ResultHandler.class.isAssignableFrom(clazz);
  }

  /**
   * Returns parameter names referenced by SQL providers.
   */
  public String[] getNames() {
    return names.values().toArray(new String[0]);
  }

  /**
   * <p>
   * A single non-special parameter is returned without a name.<br />
   * Multiple parameters are named using the naming rule.<br />
   * In addition to the default names, this method also adds the generic names (param1, param2,
   * ...).
   * </p>
   * 该方法接收的参数是用户传入的实参列表，并将实参与其对应名称进行关联
   */
  public Object getNamedParams(Object[] args) {
    final int paramCount = names.size();
    if (args == null || paramCount == 0) {
      return null;
    } else if (!hasParamAnnotation && paramCount == 1) {// 未使用自Param且只有一个参数
      return args[names.firstKey()];
    } else {
      // param 这个 Map 中记录了参数名称与实参之间的对应关系.ParamMap 继承了 HashMap，如果向ParamMap 中添加已经存在的 key，会报错，其他行为与 HashMap相同
      final Map<String, Object> param = new ParamMap<Object>();
      int i = 0;
      for (Map.Entry<Integer, String> entry : names.entrySet()) {
        //将参数名与实参对应关系记录到 param 中
        param.put(entry.getValue(), args[entry.getKey()]);
        // add generic param names (param1, param2, ...)
        //下面是为参数创建”param+索引”格式的默认参数名称，例如: paraml, param2 等，并添加到param集合中
        final String genericParamName = GENERIC_NAME_PREFIX + String.valueOf(i + 1);
        // ensure not to overwrite parameter named with @Param
        if (!names.containsValue(genericParamName)) {
          param.put(genericParamName, args[entry.getKey()]);
        }
        i++;
      }
      return param;
    }
  }
}
