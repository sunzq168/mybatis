package com.sun.plugin ;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.Properties;

@Intercepts({@Signature(type = Executor.class,
            method = "query",
            args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
}
)
public class PageInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 从 Invocation 对象中获取被拦截的方法的参数列表
        Object[] args = invocation.getArgs();
        // 获取 Executor.query()方法中的参数获取MappedStatement 对象
        final MappedStatement mappedStatement = (MappedStatement) args[0];
        // 获取用户传入的实参对象
        final Object parameter = args[1];
        // 获取 RowBounds 对象
        final RowBounds rowBounds = (RowBounds) args[2];
        //获取 RowBounds 对象中记录的offset值，也就是查询的起始位置
        int offset = rowBounds.getOffset();
        // 获取 RowBounds 对象 中记录的 limit 值，也就是查询返回的记录条数
        int limit = rowBounds.getLimit();
        //获取 BoundSql 对象 ，其中记录了包含” ?”占位符的 SQL 语句
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        //获取 BoundSql 中记录的 SQL 语句
        String sql = getPagingSql(boundSql.getSql(), offset, limit);

        args[2] = new RowBounds(RowBounds.NO_ROW_OFFSET, RowBounds.NO_ROW_LIMIT);
        //args[1] = createMappedStatement(mappedStatement, boundSql, sql);

        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }

//    private MappedStatement createMappedStatement(MappedStatement mappedStatement, BoundSql boundSql, String sql) {
//        //为处理后的 SQL 语句创建新 的 BoundSql 对象，其中会复制原有 BoundSql 对象的parameterMappings 等集合的信息
//        BoundSql newBoundSql = createBoundSql(mappedStatement, boundSql, sql);
//        //为处理后的 SQL 语句创建新的 MappedStatement 对象，其中封装的 BoundSql 走上面新建的BoundSql 对象，其他的字段直接复制原有 MappedStatement 对象
//        return createMappedStatement(mappedStatement, new BoundSqlSqlSource(newBoundSql));
//    }

    public String getPagingSql(String sql, int offset, int limit) {
        sql = sql. trim() ;
        //记录当前 select 语句是否包含 "for update"子句，该子句会对数据行加锁
        boolean hasForUpdate = false;
        String forUpdatePart ="for update";
        if (sql.toLowerCase().endsWith(forUpdatePart) ) {
            // 将当前 SQLt吾句的”for update”片段删除
            sql = sql.substring(0, sql.length() - forUpdatePart.length());
            hasForUpdate = true; // 将 hasForUpdate标识设置为 true
        }

        //result 用 于记录添加分页支持之后的 SQL 语句，这里预先将 StringBuffer 扩 充到合理的位
        StringBuffer result = new StringBuffer (sql. length() + 100) ;
        result.append(sql).append ("limit") ;
        //根据 offset 值拼接支持分页 的 SQL 语句
        if (offset > 0) {
            result.append(offset).append(",").append(limit);
        } else {
            result.append(limit);
        }
        //根据前面 记录的 hasForUpdate 标志，决定是 否复原” for update，，子句
        if (hasForUpdate) {
            result.append("for update");
        }

        return result.toString();
    }

}
